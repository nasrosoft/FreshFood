package com.devsoft.desktop.data

import kotlinx.serialization.Serializable

// Mirrors of the Android domain models (com.devsoft.devsoft.domain.model) so both apps
// read and write exactly the same Supabase rows.

@Serializable
data class AppSetting(
    val id: Int = 1,
    val app_enabled: Int = 1,
    val brand_name: String? = "DevSoft",
    val brand_tagline: String? = "Stock & Sales Management",
    val master_device_id: String? = null,
    val master_heartbeat_at: String? = null,
    val auto_approve_extensions: Int = 0,
    val default_extension_modules: List<String> = listOf("deliveries")
)

@Serializable
data class Product(
    val id: String,
    val barcode: String? = null,
    val name: String,
    val category_id: String? = null,
    val brand_id: String? = null,
    val description: String? = null,
    val image_url: String? = null,
    val emoji: String? = null,
    val unit: String = "Unit",
    val purchase_price: Double = 0.0,
    val selling_price: Double = 0.0,
    val min_selling_price: Double = 0.0,
    val current_stock: Int = 0,
    val min_stock: Int = 0,
    val max_stock: Int? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val wilaya: String? = null,
    val commune: String? = null,
    val credit_limit: Double = 0.0,
    val current_credit: Double = 0.0,
    val customer_type: String? = null,
    val is_active: Boolean = true,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class CustomerCreditDetail(
    val id: String,
    val customer_id: String,
    val sale_id: String? = null,
    val invoice_number: String? = null,
    val items_summary: String? = null,
    val total_amount: Double = 0.0,
    val credit_amount: Double = 0.0,
    val transaction_type: String = "CREDIT_SALE",
    val created_at: String? = null
)

@Serializable
data class Sale(
    val id: String? = null,
    val invoice_number: String? = null,
    val customer_id: String? = null,
    val user_id: String? = null,
    val total_amount: Double = 0.0,
    val paid_amount: Double = 0.0,
    val credit_amount: Double = 0.0,
    val payment_method: String? = null,
    val status: String = "COMPLETED",
    val created_at: String? = null
)

@Serializable
data class SaleRequest(
    val id: String,
    val customer_id: String? = null,
    val user_id: String? = null,
    val total_amount: Double,
    val paid_amount: Double,
    val credit_amount: Double,
    val payment_method: String,
    val items: List<SaleItemRequest>,
    val create_delivery: Boolean = false,
    val delivery_employee_id: String? = null
)

@Serializable
data class SaleItemRequest(
    val id: String,
    val product_id: String,
    val quantity: Int,
    val unit_price: Double
)

@Serializable
data class ProcessSaleParams(val sale_data: SaleRequest)

@Serializable
data class DeliveryOrder(
    val id: String,
    val customer_id: String? = null,
    val sale_id: String? = null,
    val delivery_employee_id: String? = null,
    val status: String = "PENDING",
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class DeliveryItem(
    val id: String,
    val delivery_order_id: String,
    val product_id: String,
    val quantity: Int,
    val created_at: String? = null
)

data class DeliveryItemDetail(val item: DeliveryItem, val product: Product?)

data class DeliveryOrderWithDetails(
    val order: DeliveryOrder,
    val customer: Customer?,
    val driver: Profile?,
    val items: List<DeliveryItemDetail>,
    val sale: Sale?
) {
    val total: Double
        get() = sale?.total_amount ?: items.sumOf { it.item.quantity * (it.product?.selling_price ?: 0.0) }
}

@Serializable
data class SyncDeliveryParams(
    val p_delivery_id: String,
    val p_modified_quantities: Map<String, Int>,
    val p_final_payment_method: String
)

@Serializable
data class Profile(
    val id: String,
    val role: String,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
) {
    val displayName: String
        get() = listOfNotNull(first_name, last_name).joinToString(" ").trim().ifBlank { email ?: id.take(8) }
}

@Serializable
data class PurchaseItem(
    val product_id: String,
    val quantity: Int,
    val purchase_price: Double,
    val expiration_date: String
)

@Serializable
data class PurchaseRequest(
    val id: String,
    val supplier_id: String? = null,
    val invoice_number: String? = null,
    val user_id: String,
    val total_amount: Double,
    val items: List<PurchaseItem>
)

/** A registered install of the app (this PC or a phone). */
@Serializable
data class Device(
    val id: String,
    val device_id: String,
    val platform: String,
    val device_name: String? = null,
    val user_id: String? = null,
    val status: String = "PENDING",
    val allowed_modules: List<String> = emptyList(),
    val app_version: String? = null,
    val last_seen_at: String? = null,
    val approved_at: String? = null,
    val created_at: String? = null
)

/** Answer of the register_device RPC. */
@Serializable
data class DeviceRegistration(
    val device_id: String,
    val status: String,
    val allowed_modules: List<String> = emptyList(),
    val is_master: Boolean = false,
    val master_configured: Boolean = false,
    val master_heartbeat_at: String? = null,
    val master_online: Boolean = false,
    val app_enabled: Boolean = true
)

/** Modules an Android extension may be granted by the master. Master-only modules are excluded. */
enum class ExtensionModule(val key: String, val label: String) {
    DELIVERIES("deliveries", "Livraisons"),
    DASHBOARD("dashboard", "Tableau de bord"),
    POS("pos", "Ventes (POS)"),
    PRODUCTS("products", "Produits"),
    PURCHASES("purchases", "Entrées de stock"),
    CUSTOMERS("customers", "Clients & crédit"),
    INVENTORY("inventory", "Inventaire"),
    RETURNS("returns", "Retours")
}
