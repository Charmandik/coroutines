package ru.bikbulatov.coroutines_learning_2.data.network.model

import ru.bikbulatov.coroutines_learning_2.domain.model.User

data class UserNO(
    val id: Int,
    val name: String,
    val email: String
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email
        )
    }
}
