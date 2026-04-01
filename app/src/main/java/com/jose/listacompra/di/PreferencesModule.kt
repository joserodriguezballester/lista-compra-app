package com.jose.listacompra.di

import android.content.Context
import com.jose.listacompra.data.preferences.ListPreferences
import com.jose.listacompra.data.preferences.ThemePreferences
import com.jose.listacompra.data.repository.ArticuloRepositoryImpl
import com.jose.listacompra.data.repository.ShoppingListRepositoryImpl
import com.jose.listacompra.domain.repository.IArticuloRepository
import com.jose.listacompra.domain.repository.IShoppingListRepository
import dagger.Binds
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

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShoppingListRepository(
        impl: ShoppingListRepositoryImpl
    ): IShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindArticuloRepository(
        articuloRepositoryImpl: ArticuloRepositoryImpl
    ): IArticuloRepository
}
