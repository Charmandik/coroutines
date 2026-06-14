package ru.bikbulatov.coroutines_learning_2.presentation.model

import ru.bikbulatov.coroutines_learning_2.domain.model.Settings

data class SettingsVO(
    val userId: Int,
    val theme: String,
    val notificationsEnabled: Boolean
) {
    companion object {
        fun fromDomain(settings: Settings): SettingsVO {
            return SettingsVO(
                userId = settings.userId,
                theme = settings.theme,
                notificationsEnabled = settings.notificationsEnabled
            )
        }
    }
}
