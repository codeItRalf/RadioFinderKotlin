package app.codeitralf.radiofinder.data.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.repository.RadioRepository
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRadioRepository(
    ): RadioRepository {
        return RadioRepository.getInstance()
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideServiceConnectionManager(
        @ApplicationContext context: Context
    ): ServiceConnectionManager {
        return ServiceConnectionManager(context)
    }
}
