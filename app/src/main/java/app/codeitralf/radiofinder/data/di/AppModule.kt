package app.codeitralf.radiofinder.data.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideServiceConnectionManager(
        @ApplicationContext context: Context
    ): ServiceConnectionManager {
        return ServiceConnectionManager(context)
    }

    @IoDispatcher
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    // Network related providers can stay in NetworkModule or be moved here if you prefer
    // I recommend keeping them in NetworkModule for better organization
}