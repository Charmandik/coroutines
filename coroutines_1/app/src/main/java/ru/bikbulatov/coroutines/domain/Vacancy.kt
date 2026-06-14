package ru.bikbulatov.coroutines.domain

data class Vacancy(
    val id: String,
    val title: String,
    val company: String,
    val salary: String? = null,
    val description: String? = null,
    val location: String? = null,
    val url: String? = null
)
