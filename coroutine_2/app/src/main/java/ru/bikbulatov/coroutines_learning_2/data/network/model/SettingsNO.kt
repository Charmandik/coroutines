package ru.bikbulatov.coroutines_learning_2.data.network.model

import ru.bikbulatov.coroutines_learning_2.domain.model.Settings

data class SettingsNO(
    val userId: Int,
    val theme: String,
    val notificationsEnabled: Boolean
) {
    fun toDomain(): Settings {
        return Settings(
            userId = userId,
            theme = theme,
            notificationsEnabled = notificationsEnabled
        )
    }
}
