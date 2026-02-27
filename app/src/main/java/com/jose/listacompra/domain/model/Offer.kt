package com.jose.listacompra.domain.model

/**
 * Modelo de oferta/promoción para productos
 */
data class Offer(
    val id: Long = 0,
    val code: String,        // Código corto: "3x2", "2nd_50", "custom"
    val name: String,        // Nombre visible: "3x2", "2ª unidad -50%"
    val description: String, // Descripción larga
    val isDefault: Boolean,  // true = predefinida, false = custom del usuario
    val formula: String      // Fórmula de cálculo (para referencia/evaluación futura)
) {
    companion object {
        /**
         * Devuelve todas las ofertas predefinidas del sistema
         */
        fun getDefaultOffers(): List<Offer> = listOf(
            Offer(
                id = 1,
                code = "3x2",
                name = "3x2",
                description = "Lleva 3, paga 2",
                isDefault = true,
                formula = "(quantity / 3).toInt() * 2 + (quantity % 3) * unitPrice"
            ),
            Offer(
                id = 2,
                code = "2x1",
                name = "2x1",
                description = "Lleva 2, paga 1",
                isDefault = true,
                formula = "(quantity / 2).toInt() * 1 + (quantity % 2) * unitPrice"
            ),
            Offer(
                id = 3,
                code = "2nd_50",
                name = "2ª unidad -50%",
                description = "Segunda unidad al 50% de descuento",
                isDefault = true,
                formula = "(quantity / 2).toInt() * 1.5f + (quantity % 2) * unitPrice"
            ),
            Offer(
                id = 4,
                code = "2nd_70",
                name = "2ª unidad -70%",
                description = "Segunda unidad al 70% de descuento",
                isDefault = true,
                formula = "(quantity / 2).toInt() * 1.3f + (quantity % 2) * unitPrice"
            ),
            Offer(
                id = 5,
                code = "4x3",
                name = "4x3",
                description = "Lleva 4, paga 3",
                isDefault = true,
                formula = "(quantity / 4).toInt() * 3 + (quantity % 4) * unitPrice"
            )
        )
    }
}
