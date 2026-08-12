package com.devsoft.freshfood.domain.model

data class DeliveryOrderWithDetails(
    val order: DeliveryOrder,
    val customer: Customer?,
    val items: List<DeliveryItemDetail>
)

data class DeliveryItemDetail(
    val item: DeliveryItem,
    val product: Product?
)
