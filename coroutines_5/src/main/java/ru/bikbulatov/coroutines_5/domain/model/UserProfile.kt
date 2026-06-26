package ru.bikbulatov.coroutines_5.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val content: String
)

data class UserProfile(
    val user: User,
    val posts: List<Post>,
    val cachedData: String? = null
)
