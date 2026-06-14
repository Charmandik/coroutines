package ru.bikbulatov.coroutines.presentation.vacancies

sealed class VacancyListUiState {
    data object Loading : VacancyListUiState()
    data object Success : VacancyListUiState()
    data class Error(val message: String) : VacancyListUiState()
}