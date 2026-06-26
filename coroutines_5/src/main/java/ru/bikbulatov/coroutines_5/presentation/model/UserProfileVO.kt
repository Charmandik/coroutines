package ru.bikbulatov.coroutines_5.presentation.model

import ru.bikbulatov.coroutines_5.domain.model.Post
import ru.bikbulatov.coroutines_5.domain.model.User
import ru.bikbulatov.coroutines_5.domain.model.UserProfile

data class UserVO(
    val id: Int,
    val name: String,
    val email: String
) {
    companion object {
        fun fromDomain(user: User): UserVO {
            return UserVO(
                id = user.id,
                name = user.name,
                email = user.email
            )
        }
    }
}

data class PostVO(
    val id: Int,
    val userId: Int,
    val title: String,
    val content: String
) {
    companion object {
        fun fromDomain(post: Post): PostVO {
            return PostVO(
                id = post.id,
                userId = post.userId,
                title = post.title,
                content = post.content
            )
        }
    }
}

data class UserProfileVO(
    val user: UserVO,
    val posts: List<PostVO>,
    val cachedData: String? = null
) {
    companion object {
        fun fromDomain(profile: UserProfile): UserProfileVO {
            return UserProfileVO(
                user = UserVO.fromDomain(profile.user),
                posts = profile.posts.map { PostVO.fromDomain(it) },
                cachedData = profile.cachedData
            )
        }
    }
}
