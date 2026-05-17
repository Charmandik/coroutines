package ru.bikbulatov.coroutines.domain

import javax.inject.Inject

class LoadVacanciesUseCase @Inject constructor(
    private val repository: VacancyRepository
) {
    suspend operator fun invoke(): Result<List<Vacancy>> {
        return repository.loadVacancies()
    }
}
