package com.jose.listacompra.di

import android.content.Context
import com.jose.listacompra.data.preferences.ListPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideListPreferences(@ApplicationContext context: Context): ListPreferences {
        return ListPreferences(context)
    }
}