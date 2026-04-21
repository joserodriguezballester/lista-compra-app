package com.jose.listacompra.di

import com.jose.listacompra.data.storage.MediaStoreArticuloPhotoStorage
import com.jose.listacompra.domain.storage.ArticuloPhotoStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindArticuloPhotoStorage(
        impl: MediaStoreArticuloPhotoStorage
    ): ArticuloPhotoStorage
}
