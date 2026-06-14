package ru.bikbulatov.coroutines_3.presentation.model

data class BodyDataVO(
    val orders: List<OrderItemVO>,
    val settings: Map<String, String>
)
