package com.jose.listacompra.domain.usecase

import com.jose.listacompra.data.local.dataseeder.InitialDataSeeder
import javax.inject.Inject

/**
 * UseCase que pobla TODAS las tablas de la base de datos
 * Orden: Supermercados → Categorías → Pasillos → Artículos
 */
class SeedAllDataUseCase @Inject constructor(
    private val initialDataSeeder: InitialDataSeeder
) {
    suspend operator fun invoke() {
        initialDataSeeder.seedAll()
    }
}
