package com.devsoft.desktop.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

/**
 * Every read/write the master station performs. It talks to the same Supabase tables and
 * stored procedures (process_sale, sync_delivery_completion, process_purchase, ...) that the
 * Android app uses, so business rules (FEFO, LIVRÉE commit point, credit ledger) stay in one place.
 */
class Repository {
    private val supabase = Backend.client
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }

    // ---------------------------------------------------------------- auth
    suspend fun login(email: String, password: String) = io {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun restoreSessionUserId(): String? = io {
        supabase.auth.awaitInitialization()
        val session = supabase.auth.currentSessionOrNull() ?: return@io null
        runCatching { supabase.auth.refreshCurrentSession() }
        supabase.auth.currentSessionOrNull()?.user?.id ?: session.user?.id
    }

    fun currentUserId(): String? = supabase.auth.currentSessionOrNull()?.user?.id

    suspend fun logout() = io { runCatching { supabase.auth.signOut() } }

    // ---------------------------------------------------------------- master / devices
    suspend fun registerThisStation(): DeviceRegistration = io {
        val res = supabase.postgrest.rpc("register_device", buildJsonObject {
            put("p_device_id", DeviceIdentity.deviceId)
            put("p_platform", "WINDOWS")
            put("p_device_name", DeviceIdentity.deviceName)
            put("p_app_version", BackendConfig.APP_VERSION)
        })
        json.decodeFromString(DeviceRegistration.serializer(), res.data)
    }

    suspend fun heartbeat() = io {
        supabase.postgrest.rpc("master_heartbeat", buildJsonObject { put("p_device_id", DeviceIdentity.deviceId) })
    }

    suspend fun claimMaster() = io {
        supabase.postgrest.rpc("claim_master", buildJsonObject { put("p_device_id", DeviceIdentity.deviceId) })
    }

    suspend fun getDevices(): List<Device> = io {
        supabase.from("devices").select { order("created_at", Order.DESCENDING) }.decodeList<Device>()
    }

    suspend fun setDeviceAccess(targetDeviceId: String, status: String, modules: List<String>) = io {
        supabase.postgrest.rpc("set_device_access", buildJsonObject {
            put("p_master_device_id", DeviceIdentity.deviceId)
            put("p_target_device_id", targetDeviceId)
            put("p_status", status)
            put("p_allowed_modules", JsonArray(modules.map { JsonPrimitive(it) }))
        })
    }

    suspend fun deleteDevice(targetDeviceId: String) = io {
        supabase.postgrest.rpc("delete_device", buildJsonObject {
            put("p_master_device_id", DeviceIdentity.deviceId)
            put("p_target_device_id", targetDeviceId)
        })
    }

    suspend fun setExtensionPolicy(autoApprove: Boolean, defaultModules: List<String>) = io {
        supabase.postgrest.rpc("set_extension_policy", buildJsonObject {
            put("p_master_device_id", DeviceIdentity.deviceId)
            put("p_auto_approve", if (autoApprove) 1 else 0)
            put("p_default_modules", JsonArray(defaultModules.map { JsonPrimitive(it) }))
        })
    }

    // ---------------------------------------------------------------- settings
    suspend fun getSettings(): AppSetting = io {
        supabase.from("app_settings").select().decodeList<AppSetting>().firstOrNull() ?: AppSetting()
    }

    suspend fun updateBrand(name: String, tagline: String) = io {
        supabase.from("app_settings").update({
            set("brand_name", name)
            set("brand_tagline", tagline)
        }) { filter { eq("id", 1) } }
    }

    suspend fun setAppEnabled(enabled: Boolean) = io {
        supabase.from("app_settings").update({ set("app_enabled", if (enabled) 1 else 0) }) { filter { eq("id", 1) } }
    }

    // ---------------------------------------------------------------- reference data
    suspend fun getProducts(): List<Product> = io {
        supabase.from("products").select { order("name", Order.ASCENDING) }.decodeList<Product>()
    }

    suspend fun getCustomers(): List<Customer> = io {
        supabase.from("customers").select { order("name", Order.ASCENDING) }.decodeList<Customer>()
    }

    suspend fun getProfiles(): List<Profile> = io {
        supabase.from("profiles").select().decodeList<Profile>()
    }

    suspend fun getProfile(id: String): Profile? = io {
        supabase.from("profiles").select { filter { eq("id", id) } }.decodeSingleOrNull<Profile>()
    }

    suspend fun getSales(): List<Sale> = io {
        supabase.from("sales").select { order("created_at", Order.DESCENDING) }.decodeList<Sale>()
    }

    // ---------------------------------------------------------------- products
    suspend fun saveProduct(p: Product, isNew: Boolean) = io {
        val body = buildJsonObject {
            put("name", p.name)
            put("barcode", p.barcode?.ifBlank { null })
            put("unit", p.unit)
            put("description", p.description)
            put("emoji", p.emoji)
            put("purchase_price", p.purchase_price)
            put("selling_price", p.selling_price)
            put("min_selling_price", p.min_selling_price)
            put("min_stock", p.min_stock)
            put("is_active", p.is_active)
        }
        if (isNew) {
            val withId = buildJsonObject {
                put("id", p.id)
                put("current_stock", 0)
                body.forEach { (k, v) -> put(k, v) }
            }
            supabase.from("products").insert(withId)
        } else {
            supabase.from("products").update(body) { filter { eq("id", p.id) } }
        }
    }

    suspend fun deleteProduct(id: String) = io {
        supabase.from("products").delete { filter { eq("id", id) } }
    }

    // ---------------------------------------------------------------- customers
    suspend fun saveCustomer(c: Customer, isNew: Boolean) = io {
        val body = buildJsonObject {
            put("name", c.name)
            put("phone", c.phone)
            put("address", c.address)
            put("wilaya", c.wilaya)
            put("commune", c.commune)
            put("credit_limit", c.credit_limit)
            put("customer_type", c.customer_type)
            put("notes", c.notes)
        }
        if (isNew) {
            supabase.from("customers").insert(buildJsonObject {
                put("id", c.id)
                body.forEach { (k, v) -> put(k, v) }
            })
        } else {
            supabase.from("customers").update(body) { filter { eq("id", c.id) } }
        }
    }

    suspend fun deleteCustomer(id: String) = io {
        supabase.from("customers").delete { filter { eq("id", id) } }
    }

    suspend fun getCreditHistory(customerId: String): List<CustomerCreditDetail> = io {
        supabase.from("customer_credit_details").select {
            filter { eq("customer_id", customerId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<CustomerCreditDetail>()
    }

    /** Same three writes as the Android CustomerRepositoryImpl.registerPayment. */
    suspend fun registerPayment(customerId: String, amount: Double) = io {
        val uid = currentUserId()
        supabase.from("payments").insert(buildJsonObject {
            put("customer_id", customerId)
            put("amount", amount)
            put("payment_method", "CASH")
            if (uid != null) put("user_id", uid)
        })
        supabase.from("credit_transactions").insert(buildJsonObject {
            put("customer_id", customerId)
            put("amount", amount)
            put("transaction_type", "PAYMENT")
            if (uid != null) put("user_id", uid)
        })
        supabase.from("customer_credit_details").insert(buildJsonObject {
            put("customer_id", customerId)
            put("items_summary", "Paiement de crédit / Credit Payment")
            put("total_amount", amount)
            put("credit_amount", amount)
            put("transaction_type", "PAYMENT")
        })
    }

    // ---------------------------------------------------------------- sales / orders
    /** Direct counter sale (createDelivery=false) or a draft delivery order dispatched to a driver. */
    suspend fun processSale(request: SaleRequest): String = io {
        val res = supabase.postgrest.rpc("process_sale", ProcessSaleParams(request))
        runCatching {
            json.parseToJsonElement(res.data).jsonObject["invoice_number"]?.jsonPrimitive?.content
        }.getOrNull() ?: "INV-${request.id.take(8).uppercase()}"
    }

    fun newSaleRequest(
        lines: List<Pair<Product, Int>>,
        paymentMethod: String,
        customerId: String?,
        createDelivery: Boolean,
        driverId: String?
    ): SaleRequest {
        val total = lines.sumOf { (p, q) -> p.selling_price * q }
        val isCredit = paymentMethod == "CREDIT"
        return SaleRequest(
            id = UUID.randomUUID().toString(),
            customer_id = customerId,
            user_id = currentUserId(),
            total_amount = total,
            paid_amount = if (isCredit) 0.0 else total,
            credit_amount = if (isCredit) total else 0.0,
            payment_method = paymentMethod,
            items = lines.map { (p, q) -> SaleItemRequest(UUID.randomUUID().toString(), p.id, q, p.selling_price) },
            create_delivery = createDelivery,
            delivery_employee_id = driverId
        )
    }

    // ---------------------------------------------------------------- deliveries
    suspend fun getDeliveries(): List<DeliveryOrderWithDetails> = io {
        val orders = supabase.from("delivery_orders").select { order("created_at", Order.DESCENDING) }.decodeList<DeliveryOrder>()
        val customers = supabase.from("customers").select().decodeList<Customer>().associateBy { it.id }
        val items = supabase.from("delivery_items").select().decodeList<DeliveryItem>().groupBy { it.delivery_order_id }
        val products = supabase.from("products").select().decodeList<Product>().associateBy { it.id }
        val profiles = runCatching { supabase.from("profiles").select().decodeList<Profile>() }.getOrDefault(emptyList()).associateBy { it.id }
        val saleIds = orders.mapNotNull { it.sale_id }.distinct()
        val sales = if (saleIds.isEmpty()) emptyMap() else
            supabase.from("sales").select { filter { isIn("id", saleIds) } }.decodeList<Sale>().associateBy { it.id }

        orders.map { o ->
            DeliveryOrderWithDetails(
                order = o,
                customer = o.customer_id?.let { customers[it] },
                driver = o.delivery_employee_id?.let { profiles[it] },
                items = (items[o.id] ?: emptyList()).map { DeliveryItemDetail(it, products[it.product_id]) },
                sale = o.sale_id?.let { sales[it] }
            )
        }
    }

    suspend fun assignDriver(orderId: String, driverId: String?) = io {
        supabase.from("delivery_orders").update({
            set("delivery_employee_id", driverId)
            set("status", if (driverId == null) "PENDING" else "ASSIGNED")
            set("updated_at", java.time.Instant.now().toString())
        }) { filter { eq("id", orderId) } }
    }

    suspend fun updateDeliveryStatus(orderId: String, status: String) = io {
        supabase.from("delivery_orders").update({
            set("status", status)
            set("updated_at", java.time.Instant.now().toString())
        }) { filter { eq("id", orderId) } }
    }

    /** LIVRÉE commit point - identical call to the Android driver app. */
    suspend fun completeDelivery(orderId: String, quantitiesByItemId: Map<String, Int>, paymentMethod: String) = io {
        supabase.postgrest.rpc(
            "sync_delivery_completion",
            SyncDeliveryParams(orderId, quantitiesByItemId, paymentMethod)
        )
    }

    suspend fun deleteDelivery(orderId: String) = io {
        supabase.from("delivery_orders").delete { filter { eq("id", orderId) } }
    }

    // ---------------------------------------------------------------- purchases
    suspend fun processPurchase(invoice: String?, items: List<PurchaseItem>): String = io {
        val id = UUID.randomUUID().toString()
        val req = PurchaseRequest(
            id = id,
            invoice_number = invoice?.ifBlank { null } ?: "ACH-${System.currentTimeMillis()}",
            user_id = currentUserId() ?: "00000000-0000-0000-0000-000000000000",
            total_amount = items.sumOf { it.quantity * it.purchase_price },
            items = items
        )
        supabase.postgrest.rpc("process_purchase", req)
        "PUR-${id.take(8).uppercase()}"
    }

    // ---------------------------------------------------------------- staff
    suspend fun createDriver(email: String, password: String, firstName: String, lastName: String): String = io {
        val adminUid = currentUserId() ?: error("Admin not logged in")
        val res = supabase.postgrest.rpc("create_delivery_user", buildJsonObject {
            put("admin_uid", adminUid)
            put("user_email", email)
            put("user_password", password)
            put("user_first_name", firstName)
            put("user_last_name", lastName)
        })
        runCatching { json.parseToJsonElement(res.data).jsonObject["user_id"]?.jsonPrimitive?.content }.getOrNull() ?: ""
    }

    suspend fun setProfileActive(id: String, active: Boolean) = io {
        supabase.from("profiles").update({ set("is_active", active) }) { filter { eq("id", id) } }
    }
}
