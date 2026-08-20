package com.devsoft.devsoft.domain.model

data class DeliveryOrderWithDetails(
    val order: DeliveryOrder,
    val customer: Customer?,
    val driver: Profile? = null,
    val items: List<DeliveryItemDetail>,
    val sale: Sale? = null
)

data class DeliveryItemDetail(
    val item: DeliveryItem,
    val product: Product?
)
