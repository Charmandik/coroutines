package ru.bikbulatov.coroutines.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.bikbulatov.coroutines.data.VacancyRepositoryImpl
import ru.bikbulatov.coroutines.domain.VacancyRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVacancyRepository(
        repositoryImpl: VacancyRepositoryImpl
    ): VacancyRepository
}