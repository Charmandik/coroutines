package ru.bikbulatov.coroutines_learning_2.data.network.model

import ru.bikbulatov.coroutines_learning_2.domain.model.Post

data class PostNO(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    fun toDomain(): Post {
        return Post(
            id = id,
            userId = userId,
            title = title,
            body = body
        )
    }
}
