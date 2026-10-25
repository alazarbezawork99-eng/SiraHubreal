package com.example.di

import com.example.data.repository.GigsRepository
import com.example.data.repository.GigsRepositoryImpl
import com.example.data.repository.JobsRepository
import com.example.data.repository.JobsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each repository interface to its implementation.
 *
 * Repositories are singletons so that the in-memory session and the Room cache are shared across
 * every screen rather than re-created per ViewModel.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJobsRepository(impl: JobsRepositoryImpl): JobsRepository

    @Binds
    @Singleton
    abstract fun bindGigsRepository(impl: GigsRepositoryImpl): GigsRepository
}
