package com.rbikbulatov.coroutines_4.di

import com.rbikbulatov.coroutines_4.data.ProfileRepositoryImpl
import com.rbikbulatov.coroutines_4.domain.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        repositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository
}
