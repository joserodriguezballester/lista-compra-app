package com.jose.listacompra.domain.usecase.articulo

import com.jose.listacompra.data.local.dataseeder.InitialDataSeeder
import com.jose.listacompra.domain.repository.IArticuloRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SeedCatalogUseCase @Inject constructor(
    private val repository: IArticuloRepository
) {
    suspend operator fun invoke() {
        // La regla de negocio: Solo sembramos si no hay nada
        val currentArticulos = repository.getAllArticulos().first()
        if (currentArticulos.isEmpty()) {
            // Aquí es donde vive tu lista de 15 artículos (SeedArticulo)
            val initialData = InitialDataSeeder.getInitialItems()
            initialData.forEach { articulo ->
                repository.saveArticulo(articulo)
            }
        }
    }
}