package com.devsoft.freshfood.data.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

data class ProductImageResult(
    val imageUrl: String,
    val source: String = "openfoodfacts",
    val title: String? = null
)

interface ProductImageProvider {
    suspend fun searchProductImage(query: String, barcode: String? = null): ProductImageResult?
}

class OpenFoodFactsImageProvider : ProductImageProvider {

    // Session in-memory cache to prevent redundant network calls
    private val cache = ConcurrentHashMap<String, ProductImageResult?>()

    override suspend fun searchProductImage(query: String, barcode: String?): ProductImageResult? {
        val cleanQuery = query.trim()
        val cleanBarcode = barcode?.trim()

        if (cleanQuery.isBlank() && cleanBarcode.isNullOrBlank()) {
            return null
        }

        val cacheKey = (cleanBarcode?.ifBlank { null } ?: cleanQuery).lowercase()
        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]
        }

        return withContext(Dispatchers.IO) {
            val result = try {
                // 1. Try Barcode lookup first if barcode is available
                if (!cleanBarcode.isNullOrBlank()) {
                    lookupByBarcode(cleanBarcode) ?: lookupByName(cleanQuery)
                } else {
                    lookupByName(cleanQuery)
                }
            } catch (e: Exception) {
                null
            }

            cache[cacheKey] = result
            result
        }
    }

    private fun lookupByBarcode(barcode: String): ProductImageResult? {
        try {
            val urlString = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
            val response = executeHttpGet(urlString) ?: return null
            val json = JSONObject(response)

            if (json.optInt("status", 0) == 1) {
                val product = json.optJSONObject("product") ?: return null
                val imageUrl = product.optString("image_front_url").takeIf { it.isNotBlank() }
                    ?: product.optString("image_url").takeIf { it.isNotBlank() }
                    ?: product.optString("image_small_url").takeIf { it.isNotBlank() }

                if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http")) {
                    return ProductImageResult(
                        imageUrl = imageUrl,
                        source = "openfoodfacts_barcode",
                        title = product.optString("product_name")
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore & fallback to name search
        }
        return null
    }

    private fun lookupByName(query: String): ProductImageResult? {
        if (query.isBlank()) return null
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=5"
            val response = executeHttpGet(urlString) ?: return null
            val json = JSONObject(response)
            val products = json.optJSONArray("products") ?: return null

            for (i in 0 until products.length()) {
                val item = products.optJSONObject(i) ?: continue
                val imageUrl = item.optString("image_front_url").takeIf { it.isNotBlank() }
                    ?: item.optString("image_url").takeIf { it.isNotBlank() }
                    ?: item.optString("image_small_url").takeIf { it.isNotBlank() }

                if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http")) {
                    return ProductImageResult(
                        imageUrl = imageUrl,
                        source = "openfoodfacts",
                        title = item.optString("product_name")
                    )
                }
            }
        } catch (e: Exception) {
            // Return null on failure
        }
        return null
    }

    private fun executeHttpGet(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("User-Agent", "FreshFoodApp/1.0 (Android; devsoft)")
            connection.setRequestProperty("Accept", "application/json")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
