package ru.bikbulatov.coroutines_learning_2.presentation.model

import ru.bikbulatov.coroutines_learning_2.domain.model.Post

data class PostVO(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    companion object {
        fun fromDomain(post: Post): PostVO {
            return PostVO(
                id = post.id,
                userId = post.userId,
                title = post.title,
                body = post.body
            )
        }
    }
}
