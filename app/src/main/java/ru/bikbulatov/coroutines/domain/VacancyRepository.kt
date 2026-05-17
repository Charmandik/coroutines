package ru.bikbulatov.coroutines.domain

interface VacancyRepository {
    suspend fun loadVacancies(): Result<List<Vacancy>>
}
