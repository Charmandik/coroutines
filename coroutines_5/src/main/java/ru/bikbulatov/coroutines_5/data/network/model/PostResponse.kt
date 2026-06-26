package ru.bikbulatov.coroutines_5.data.network.model

data class PostResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val content: String
)
