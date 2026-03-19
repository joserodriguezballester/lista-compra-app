package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.domain.model.Articulo
import com.jose.listacompra.domain.repository.IArticuloRepository

class SearchArticuloByVoiceUseCase(private val repository: IArticuloRepository) {
    suspend operator fun invoke(voiceText: String): List<Articulo> {
        if (voiceText.isBlank()) return emptyList()
        // Aquí podrías añadir lógica de limpieza del texto (quitar "un", "el", "de")
        val cleanText = voiceText.trim().lowercase()
        return repository.searchArticulos(cleanText)
    }
}