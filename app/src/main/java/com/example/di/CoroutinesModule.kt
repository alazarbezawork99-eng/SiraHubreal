package com.example.di

import com.example.data.auth.SessionTokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Marks the dispatcher used for blocking I/O (OkHttp, Room writes).
 *
 * Repositories take this explicitly instead of hardcoding `Dispatchers.IO`, so tests can swap in a
 * deterministic dispatcher without touching production code.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Returns the shared token store rather than letting Hilt build a second one.
     *
     * [com.example.data.auth.SupabaseAuthRepository] is not a Hilt component yet, so it writes to
     * the companion instance. Binding that same object here guarantees the writer and every reader
     * (the HTTP interceptor, the repositories) observe the same token.
     */
    @Provides
    @Singleton
    fun provideSessionTokenStore(): SessionTokenStore = SessionTokenStore.instance
}
