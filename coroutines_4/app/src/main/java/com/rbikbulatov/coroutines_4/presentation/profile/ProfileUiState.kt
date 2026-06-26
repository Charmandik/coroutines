package com.rbikbulatov.coroutines_4.presentation.profile

import com.rbikbulatov.coroutines_4.domain.UserProfile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
