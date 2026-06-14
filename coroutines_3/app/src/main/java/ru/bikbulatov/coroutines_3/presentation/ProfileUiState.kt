package ru.bikbulatov.coroutines_3.presentation

sealed interface ProfileUiState {
    object Loading : ProfileUiState

    data class Success(
        val headerData: String? = null,
        val bodyData: String? = null,
        val footerData: String? = null
    ) : ProfileUiState

    object Error : ProfileUiState
}
