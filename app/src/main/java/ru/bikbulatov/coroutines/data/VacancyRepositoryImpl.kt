package ru.bikbulatov.coroutines.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.bikbulatov.coroutines.domain.Vacancy
import ru.bikbulatov.coroutines.domain.VacancyRepository
import javax.inject.Inject
import javax.inject.Singleton



class VacancyRepositoryImpl @Inject constructor() : VacancyRepository {

    override suspend fun loadVacancies(): Result<List<Vacancy>> {
        // TODO: Implement actual data loading from API or database
        return Result.success(emptyList())
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVacancyRepository(
        repositoryImpl: VacancyRepositoryImpl
    ): VacancyRepository
}
