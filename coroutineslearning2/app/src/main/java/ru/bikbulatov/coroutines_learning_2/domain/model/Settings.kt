package ru.bikbulatov.coroutines_learning_2.domain.model

data class Settings(
    val userId: Int,
    val theme: String,
    val notificationsEnabled: Boolean
)
