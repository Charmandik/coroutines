package com.rbikbulatov.coroutines_4.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbikbulatov.coroutines_4.domain.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _uiState = MutableSharedFlow<ProfileUiState>()
    val uiState: SharedFlow<ProfileUiState> = _uiState.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.emit(ProfileUiState.Loading)
            try {
                val profile = getProfileUseCase()
                _uiState.emit(ProfileUiState.Success(profile))
            } catch (e: Exception) {
                _uiState.emit(ProfileUiState.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
