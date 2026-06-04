package ru.bikbulatov.coroutines_learning_2.presentation.model

import ru.bikbulatov.coroutines_learning_2.domain.model.User

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
