package com.jose.listacompra.domain.usecase.data

import androidx.room.withTransaction
import com.google.gson.Gson
import com.jose.listacompra.data.local.ShoppingListDatabase
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.ArticuloDao
import com.jose.listacompra.data.local.dao.ArticuloSupermarketDefaultDao
import com.jose.listacompra.data.local.dao.CategoryDao
import com.jose.listacompra.data.local.dao.CategorySupermarketOrderDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.dao.SupermarketDao
import com.jose.listacompra.data.local.dao.TicketDao
import com.jose.listacompra.data.local.entities.AisleEntity
import com.jose.listacompra.data.local.entities.ArticuloEntity
import com.jose.listacompra.data.local.entities.ArticuloSupermarketDefaultEntity
import com.jose.listacompra.data.local.entities.CategoryEntity
import com.jose.listacompra.data.local.entities.CategorySupermarketOrderEntity
import com.jose.listacompra.data.local.entities.OfferEntity
import com.jose.listacompra.data.local.entities.ProductEntity
import com.jose.listacompra.data.local.entities.ProductFrequencyEntity
import com.jose.listacompra.data.local.entities.ProductHistoryEntity
import com.jose.listacompra.data.local.entities.ProductPriceHistoryEntity
import com.jose.listacompra.data.local.entities.PurchaseHistoryEntity
import com.jose.listacompra.data.local.entities.ShoppingListEntity
import com.jose.listacompra.data.local.entities.SupermarketEntity
import com.jose.listacompra.data.local.entities.TicketEntity
import com.jose.listacompra.data.local.entities.TicketLineEntity
import com.jose.listacompra.domain.model.BackupAisle
import com.jose.listacompra.domain.model.BackupArticulo
import com.jose.listacompra.domain.model.BackupArticuloSupermarketDefault
import com.jose.listacompra.domain.model.BackupCategory
import com.jose.listacompra.domain.model.BackupCategorySupermarketOrder
import com.jose.listacompra.domain.model.BackupOffer
import com.jose.listacompra.domain.model.BackupProduct
import com.jose.listacompra.domain.model.BackupProductFrequency
import com.jose.listacompra.domain.model.BackupProductHistory
import com.jose.listacompra.domain.model.BackupProductPriceHistory
import com.jose.listacompra.domain.model.BackupPurchaseHistory
import com.jose.listacompra.domain.model.BackupShoppingList
import com.jose.listacompra.domain.model.BackupSupermarket
import com.jose.listacompra.domain.model.BackupTicket
import com.jose.listacompra.domain.model.BackupTicketLine
import com.jose.listacompra.domain.model.UserDataBackup
import javax.inject.Inject

/**
 * Importa un backup de datos de usuario, reemplazando los datos existentes.
 *
 * Regla vigente: restaurar bloques completos y coherentes, no separar base/custom.
 */
class ImportUserDataBackupUseCase @Inject constructor(
    private val database: ShoppingListDatabase,
    private val articuloDao: ArticuloDao,
    private val shoppingListDao: ShoppingListDao,
    private val productDao: ProductDao,
    private val aisleDao: AisleDao,
    private val ticketDao: TicketDao,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val productPriceHistoryDao: ProductPriceHistoryDao,
    private val productFrequencyDao: ProductFrequencyDao,
    private val productHistoryDao: ProductHistoryDao,
    private val articuloSupermarketDefaultDao: ArticuloSupermarketDefaultDao,
    private val categorySupermarketOrderDao: CategorySupermarketOrderDao,
    private val categoryDao: CategoryDao,
    private val supermarketDao: SupermarketDao,
    private val offerDao: OfferDao
) {
    private val gson = Gson()

    suspend operator fun invoke(json: String): ImportResult {
        val backup = gson.fromJson(json, UserDataBackup::class.java)

        database.withTransaction {
            clearAllExportedData()
            insertBackup(backup)
        }

        return ImportResult(
            supermarkets = backup.supermarkets.size,
            categories = backup.categories.size,
            offers = backup.offers.size,
            aisles = backup.aisles.size,
            articulos = backup.articulos.size,
            listas = backup.listas.size,
            productos = backup.productos.size,
            tickets = backup.tickets.size,
            ticketLines = backup.ticketLines.size,
            purchaseHistory = backup.purchaseHistory.size,
            productPriceHistory = backup.productPriceHistory.size,
            productFrequency = backup.productFrequency.size,
            productHistory = backup.productHistory.size,
            articuloSupermarketDefaults = backup.articuloSupermarketDefaults.size,
            categorySupermarketOrders = backup.categorySupermarketOrders.size
        )
    }

    private suspend fun clearAllExportedData() {
        productDao.deleteAll()
        shoppingListDao.deleteAll()
        ticketDao.deleteAllTickets()
        productPriceHistoryDao.deleteAll()
        purchaseHistoryDao.deleteAll()
        productFrequencyDao.deleteAll()
        productHistoryDao.deleteAll()
        articuloSupermarketDefaultDao.deleteAll()
        categorySupermarketOrderDao.deleteAll()
        articuloDao.deleteAll()
        offerDao.deleteAll()
        aisleDao.deleteAll()
        categoryDao.deleteAll()
        supermarketDao.deleteAll()
    }

    private suspend fun insertBackup(backup: UserDataBackup) {
        if (backup.supermarkets.isNotEmpty()) {
            supermarketDao.insertAll(backup.supermarkets.map { it.toEntity() })
        }
        if (backup.categories.isNotEmpty()) {
            categoryDao.insertAll(backup.categories.map { it.toEntity() })
        }
        if (backup.offers.isNotEmpty()) {
            offerDao.insertAll(backup.offers.map { it.toEntity() })
        }
        if (backup.aisles.isNotEmpty()) {
            aisleDao.insertAll(backup.aisles.map { it.toEntity() })
        }
        if (backup.articulos.isNotEmpty()) {
            articuloDao.insertAll(backup.articulos.map { it.toEntity() })
        }
        if (backup.listas.isNotEmpty()) {
            shoppingListDao.insertAll(backup.listas.map { it.toEntity() })
        }
        if (backup.productos.isNotEmpty()) {
            productDao.insertAll(backup.productos.map { it.toEntity() })
        }
        backup.tickets.forEach { ticket ->
            val lines = backup.ticketLines.filter { it.ticketId == ticket.id }
            ticketDao.insertTicketWithLinesPreservingIds(
                ticket = ticket.toEntity(),
                lines = lines.map { it.toEntity(ticket.id) }
            )
        }
        if (backup.purchaseHistory.isNotEmpty()) {
            purchaseHistoryDao.insertAll(backup.purchaseHistory.map { it.toEntity() })
        }
        if (backup.productPriceHistory.isNotEmpty()) {
            productPriceHistoryDao.insertAll(backup.productPriceHistory.map { it.toEntity() })
        }
        backup.productFrequency.forEach {
            productFrequencyDao.insertOrUpdateFrequency(it.toEntity())
        }
        if (backup.productHistory.isNotEmpty()) {
            productHistoryDao.insertAll(backup.productHistory.map { it.toEntity() })
        }
        if (backup.articuloSupermarketDefaults.isNotEmpty()) {
            articuloSupermarketDefaultDao.insertAll(backup.articuloSupermarketDefaults.map { it.toEntity() })
        }
        if (backup.categorySupermarketOrders.isNotEmpty()) {
            categorySupermarketOrderDao.insertAll(backup.categorySupermarketOrders.map { it.toEntity() })
        }
    }
}

data class ImportResult(
    val supermarkets: Int,
    val categories: Int,
    val offers: Int,
    val aisles: Int,
    val articulos: Int,
    val listas: Int,
    val productos: Int,
    val tickets: Int,
    val ticketLines: Int,
    val purchaseHistory: Int,
    val productPriceHistory: Int,
    val productFrequency: Int,
    val productHistory: Int,
    val articuloSupermarketDefaults: Int,
    val categorySupermarketOrders: Int
)

private fun BackupSupermarket.toEntity() = SupermarketEntity(
    id = id,
    name = name,
    emoji = emoji,
    isDefault = isDefault
)

private fun BackupCategory.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon
)

private fun BackupOffer.toEntity() = OfferEntity(
    id = id,
    code = code,
    name = name,
    description = description,
    isDefault = isDefault,
    formula = formula
)

private fun BackupAisle.toEntity() = AisleEntity(
    id = id,
    name = name,
    emoji = emoji,
    orderIndex = orderIndex,
    supermarketId = supermarketId,
    isDefault = isDefault
)

private fun BackupArticulo.toEntity() = ArticuloEntity(
    id = id,
    name = name,
    basePrice = basePrice,
    photoUri = photoUri,
    ean = ean,
    categoryId = categoryId,
    size = size,
    unit = unit
)

private fun BackupShoppingList.toEntity() = ShoppingListEntity(
    id = id,
    name = name,
    supermarketId = supermarketId,
    fechaCreacion = fechaCreacion,
    estado = estado
)

private fun BackupProduct.toEntity() = ProductEntity(
    id = id,
    name = name,
    aisleId = aisleId,
    shoppingListId = shoppingListId,
    articuloId = articuloId,
    supermarketId = supermarketId,
    quantity = quantity,
    estimatedPrice = estimatedPrice,
    offerId = offerId,
    finalPrice = finalPrice,
    isPurchased = isPurchased,
    notes = notes,
    orderIndex = orderIndex,
    photoUri = photoUri,
    ean = ean
)

private fun BackupTicket.toEntity() = TicketEntity(
    id = id,
    fecha = fecha,
    supermarketId = supermarketId,
    supermarketName = supermarketName,
    total = total,
    subtotal = subtotal,
    descuentos = descuentos,
    numProductos = numProductos,
    socioClub = socioClub,
    formaPago = formaPago,
    pdfPath = null,
    importado = importado,
    createdAt = createdAt
)

private fun BackupTicketLine.toEntity(ticketId: Long) = TicketLineEntity(
    id = id,
    ticketId = ticketId,
    nombreOriginal = nombreOriginal,
    nombreNormalizado = nombreNormalizado,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    precioTotal = precioTotal,
    articuloId = articuloId,
    categoriaId = categoriaId,
    esDescuento = esDescuento,
    codigoPromocion = codigoPromocion,
    notas = notas,
    confirmado = confirmado
)

private fun BackupPurchaseHistory.toEntity() = PurchaseHistoryEntity(
    id = id,
    fecha = fecha,
    total = total,
    tienda = tienda,
    numProductos = numProductos,
    ahorroTotal = ahorroTotal,
    ticketUrl = null
)

private fun BackupProductPriceHistory.toEntity() = ProductPriceHistoryEntity(
    id = id,
    purchaseId = purchaseId,
    productName = productName,
    price = price,
    quantity = quantity,
    aisle = aisle,
    fecha = fecha
)

private fun BackupProductFrequency.toEntity() = ProductFrequencyEntity(
    id = id,
    productName = productName,
    originalName = originalName,
    timesPurchased = timesPurchased,
    lastPurchaseDate = lastPurchaseDate,
    averageDaysBetween = averageDaysBetween,
    estimatedNextDate = estimatedNextDate,
    category = category,
    lastAisleId = lastAisleId,
    lastQuantity = lastQuantity,
    lastPrice = lastPrice,
    lastSupermarketId = lastSupermarketId,
    preferredAisleId = preferredAisleId
)

private fun BackupProductHistory.toEntity() = ProductHistoryEntity(
    id = id,
    name = name,
    originalName = originalName,
    aisleId = aisleId,
    lastQuantity = lastQuantity,
    lastPrice = lastPrice,
    usageCount = usageCount,
    lastUsed = lastUsed
)

private fun BackupArticuloSupermarketDefault.toEntity() = ArticuloSupermarketDefaultEntity(
    id = id,
    articuloId = articuloId,
    supermarketId = supermarketId,
    aisleId = aisleId
)

private fun BackupCategorySupermarketOrder.toEntity() = CategorySupermarketOrderEntity(
    id = id,
    categoryId = categoryId,
    supermarketId = supermarketId,
    orderIndex = orderIndex
)
