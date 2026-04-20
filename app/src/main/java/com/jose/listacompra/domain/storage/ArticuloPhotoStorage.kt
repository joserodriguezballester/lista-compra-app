package com.jose.listacompra.domain.storage

interface ArticuloPhotoStorage {
    suspend fun centralizeIfNeeded(photoUri: String, articuloName: String): String
}
