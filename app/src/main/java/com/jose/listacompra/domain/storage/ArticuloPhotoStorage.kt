package com.jose.listacompra.domain.storage

interface ArticuloPhotoStorage {
    suspend fun centralizeIfNeeded(photoUri: String): String
}
