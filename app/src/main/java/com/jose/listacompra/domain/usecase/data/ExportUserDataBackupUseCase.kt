package com.jose.listacompra.domain.usecase.data

import android.content.Context
import com.google.gson.GsonBuilder
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
import com.jose.listacompra.domain.model.BackupMetadata
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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Exporta todos los datos de usuario a un JSON de backup lógico.
 *
 * Regla vigente: si un bloque de tablas entra en el backup, entra completo y coherente,
 * sin separar base vs custom dentro de ese bloque.
 */
class ExportUserDataBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
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
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend operator fun invoke(): String {
        val backup = UserDataBackup(
            metadata = BackupMetadata(
                appVersionName = getAppVersionName()
            ),
            supermarkets = supermarketDao.getAllSupermarketsOnce()
                .sortedBy { it.id }
                .map { it.toBackup() },
            categories = categoryDao.getAllCategoriesOnce()
                .sortedBy { it.id }
                .map { it.toBackup() },
            offers = offerDao.getAllOffers()
                .sortedBy { it.id }
                .map { it.toBackup() },
            aisles = aisleDao.getAllAisles()
                .sortedBy { it.id }
                .map { it.toBackup() },
            articulos = articuloDao.getAllArticulosOnce()
                .sortedBy { it.id }
                .map { it.toBackup() },
            listas = shoppingListDao.getAllLists()
                .sortedBy { it.id }
                .map { it.toBackup() },
            productos = productDao.getAllProductsOnce()
                .sortedBy { it.id }
                .map { it.toBackup() },
            tickets = ticketDao.getAllTicketsWithLinesOnce()
                .map { it.ticket }
                .sortedBy { it.id }
                .map { it.toBackup() },
            ticketLines = ticketDao.getAllTicketsWithLinesOnce()
                .flatMap { it.lines }
                .sortedBy { it.id }
                .map { it.toBackup() },
            purchaseHistory = purchaseHistoryDao.getAllPurchases()
                .sortedBy { it.id }
                .map { it.toBackup() },
            productPriceHistory = productPriceHistoryDao.getAll()
                .sortedBy { it.id }
                .map { it.toBackup() },
            productFrequency = productFrequencyDao.getAllFrequencies()
                .sortedBy { it.id }
                .map { it.toBackup() },
            productHistory = productHistoryDao.getAll()
                .sortedBy { it.id }
                .map { it.toBackup() },
            articuloSupermarketDefaults = articuloSupermarketDefaultDao.getAllOnce()
                .sortedBy { it.id }
                .map { it.toBackup() },
            categorySupermarketOrders = categorySupermarketOrderDao.getAllOnce()
                .sortedBy { it.id }
                .map { it.toBackup() }
        )

        return gson.toJson(backup)
    }

    private fun getAppVersionName(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (_: Exception) {
        "?"
    }
}

private fun SupermarketEntity.toBackup() = BackupSupermarket(
    id = id,
    name = name,
    emoji = emoji,
    isDefault = isDefault
)

private fun CategoryEntity.toBackup() = BackupCategory(
    id = id,
    name = name,
    icon = icon
)

private fun OfferEntity.toBackup() = BackupOffer(
    id = id,
    code = code,
    name = name,
    description = description,
    isDefault = isDefault,
    formula = formula
)

private fun AisleEntity.toBackup() = BackupAisle(
    id = id,
    name = name,
    emoji = emoji,
    orderIndex = orderIndex,
    supermarketId = supermarketId,
    isDefault = isDefault
)

private fun ArticuloEntity.toBackup() = BackupArticulo(
    id = id,
    name = name,
    basePrice = basePrice,
    photoUri = photoUri,
    ean = ean,
    categoryId = categoryId,
    size = size,
    unit = unit
)

private fun ShoppingListEntity.toBackup() = BackupShoppingList(
    id = id,
    name = name,
    supermarketId = supermarketId,
    fechaCreacion = fechaCreacion,
    estado = estado
)

private fun ProductEntity.toBackup() = BackupProduct(
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

private fun TicketEntity.toBackup() = BackupTicket(
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
    importado = importado,
    createdAt = createdAt
)

private fun TicketLineEntity.toBackup() = BackupTicketLine(
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

private fun PurchaseHistoryEntity.toBackup() = BackupPurchaseHistory(
    id = id,
    fecha = fecha,
    total = total,
    tienda = tienda,
    numProductos = numProductos,
    ahorroTotal = ahorroTotal
)

private fun ProductPriceHistoryEntity.toBackup() = BackupProductPriceHistory(
    id = id,
    purchaseId = purchaseId,
    productName = productName,
    price = price,
    quantity = quantity,
    aisle = aisle,
    fecha = fecha
)

private fun ProductFrequencyEntity.toBackup() = BackupProductFrequency(
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

private fun ProductHistoryEntity.toBackup() = BackupProductHistory(
    id = id,
    name = name,
    originalName = originalName,
    aisleId = aisleId,
    lastQuantity = lastQuantity,
    lastPrice = lastPrice,
    usageCount = usageCount,
    lastUsed = lastUsed
)

private fun ArticuloSupermarketDefaultEntity.toBackup() = BackupArticuloSupermarketDefault(
    id = id,
    articuloId = articuloId,
    supermarketId = supermarketId,
    aisleId = aisleId
)

private fun CategorySupermarketOrderEntity.toBackup() = BackupCategorySupermarketOrder(
    id = id,
    categoryId = categoryId,
    supermarketId = supermarketId,
    orderIndex = orderIndex
)
