package com.jose.listacompra.domain.model

/**
 * Backup lógico de datos de usuario.
 * Pensado para exportar/importar información útil entre versiones de la app
 * sin depender del fichero SQLite en bruto.
 *
 * v1:
 * - Incluye tickets y ticketLines (sin pdfPath)
 * - Incluye product_history
 * - Exporta bloques completos y coherentes de tablas (sin separar base/custom dentro del bloque)
 * - Importación en modo restaurar reemplazando (no merge)
 */
data class UserDataBackup(
    val metadata: BackupMetadata,
    val supermarkets: List<BackupSupermarket>,
    val categories: List<BackupCategory>,
    val offers: List<BackupOffer>,
    val aisles: List<BackupAisle>,
    val articulos: List<BackupArticulo>,
    val listas: List<BackupShoppingList>,
    val productos: List<BackupProduct>,
    val tickets: List<BackupTicket>,
    val ticketLines: List<BackupTicketLine>,
    val purchaseHistory: List<BackupPurchaseHistory>,
    val productPriceHistory: List<BackupProductPriceHistory>,
    val productFrequency: List<BackupProductFrequency>,
    val productHistory: List<BackupProductHistory>,
    val articuloSupermarketDefaults: List<BackupArticuloSupermarketDefault>,
    val categorySupermarketOrders: List<BackupCategorySupermarketOrder>
)

data class BackupMetadata(
    val formatVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersionName: String
)

data class BackupSupermarket(
    val id: Long,
    val name: String,
    val emoji: String,
    val isDefault: Boolean
)

data class BackupCategory(
    val id: Long,
    val name: String,
    val icon: String
)

data class BackupOffer(
    val id: Long,
    val code: String,
    val name: String,
    val description: String,
    val isDefault: Boolean,
    val formula: String
)

data class BackupAisle(
    val id: Long,
    val name: String,
    val emoji: String,
    val orderIndex: Int,
    val supermarketId: Long,
    val isDefault: Boolean
)

data class BackupArticulo(
    val id: Long,
    val name: String,
    val basePrice: Float?,
    val photoUri: String?,
    val ean: String?,
    val categoryId: Long,
    val size: Float,
    val unit: String
)

data class BackupShoppingList(
    val id: Long,
    val name: String,
    val supermarketId: Long?,
    val fechaCreacion: Long,
    val estado: String
)

data class BackupProduct(
    val id: Long,
    val name: String,
    val aisleId: Long,
    val shoppingListId: Long,
    val articuloId: Long?,
    val supermarketId: Long,
    val quantity: Float,
    val estimatedPrice: Float?,
    val offerId: Long?,
    val finalPrice: Float?,
    val isPurchased: Boolean,
    val notes: String,
    val orderIndex: Int,
    val photoUri: String?,
    val ean: String?
)

data class BackupTicket(
    val id: Long,
    val fecha: Long,
    val supermarketId: Long?,
    val supermarketName: String?,
    val total: Float,
    val subtotal: Float?,
    val descuentos: Float?,
    val numProductos: Int,
    val socioClub: String?,
    val formaPago: String?,
    val importado: Boolean,
    val createdAt: Long
)

data class BackupTicketLine(
    val id: Long,
    val ticketId: Long,
    val nombreOriginal: String,
    val nombreNormalizado: String,
    val cantidad: Int,
    val precioUnitario: Float,
    val precioTotal: Float,
    val articuloId: Long?,
    val categoriaId: Long?,
    val esDescuento: Boolean,
    val codigoPromocion: String?,
    val notas: String?,
    val confirmado: Boolean
)

data class BackupPurchaseHistory(
    val id: Long,
    val fecha: Long,
    val total: Float,
    val tienda: String,
    val numProductos: Int,
    val ahorroTotal: Float
)

data class BackupProductPriceHistory(
    val id: Long,
    val purchaseId: Long,
    val productName: String,
    val price: Float,
    val quantity: Int,
    val aisle: String?,
    val fecha: Long
)

data class BackupProductFrequency(
    val id: Long,
    val productName: String,
    val originalName: String,
    val timesPurchased: Int,
    val lastPurchaseDate: Long,
    val averageDaysBetween: Float?,
    val estimatedNextDate: Long?,
    val category: String?,
    val lastAisleId: Long,
    val lastQuantity: Float,
    val lastPrice: Float,
    val lastSupermarketId: Long,
    val preferredAisleId: Long
)

data class BackupProductHistory(
    val id: Long,
    val name: String,
    val originalName: String,
    val aisleId: Long,
    val lastQuantity: Float,
    val lastPrice: Float?,
    val usageCount: Int,
    val lastUsed: Long
)

data class BackupArticuloSupermarketDefault(
    val id: Long,
    val articuloId: Long,
    val supermarketId: Long,
    val aisleId: Long
)

data class BackupCategorySupermarketOrder(
    val id: Long,
    val categoryId: Long,
    val supermarketId: Long,
    val orderIndex: Int
)
