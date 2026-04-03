package com.jose.listacompra.di

import android.content.Context
import androidx.room.Room
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
import com.jose.listacompra.data.repository.AisleRepositoryImpl
import com.jose.listacompra.data.repository.ArticuloSupermarketDefaultRepository
import com.jose.listacompra.data.repository.CategoryRepository
import com.jose.listacompra.data.repository.CategorySupermarketOrderRepository
import com.jose.listacompra.data.repository.OpenFoodFactsRepositoryImpl
import com.jose.listacompra.data.repository.ProductRepositoryImpl
import com.jose.listacompra.data.repository.SupermarketRepository
import com.jose.listacompra.domain.repository.IAisleRepository
import com.jose.listacompra.domain.repository.IArticuloSupermarketDefaultRepository
import com.jose.listacompra.domain.repository.ICategoryRepository
import com.jose.listacompra.domain.repository.ICategorySupermarketOrderRepository
import com.jose.listacompra.domain.repository.IOpenFoodFactsRepository
import com.jose.listacompra.domain.repository.IProductRepository
import com.jose.listacompra.domain.repository.ISupermarketRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShoppingListDatabase {
        return Room.databaseBuilder(
            context,
            ShoppingListDatabase::class.java,
            "shopping_list_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProductDao(db: ShoppingListDatabase): ProductDao = db.productDao()

    @Provides
    fun provideShoppingListDao(db: ShoppingListDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    fun provideAisleDao(db: ShoppingListDatabase): AisleDao = db.aisleDao()

    @Provides
    fun provideOfferDao(db: ShoppingListDatabase): OfferDao = db.offerDao()

    @Provides
    fun provideProductHistoryDao(db: ShoppingListDatabase): ProductHistoryDao = db.productHistoryDao()

    @Provides
    fun providePurchaseHistoryDao(database: ShoppingListDatabase): PurchaseHistoryDao = database.purchaseHistoryDao()

    @Provides
    fun provideProductPriceHistoryDao(db: ShoppingListDatabase): ProductPriceHistoryDao = db.productPriceHistoryDao()

    @Provides
    fun provideProductFrequencyDao(db: ShoppingListDatabase): ProductFrequencyDao = db.productFrequencyDao()

    @Provides
    fun provideArticuloDao(db: ShoppingListDatabase): ArticuloDao = db.articuloDao()

    @Provides
    fun provideSupermarketDao(db: ShoppingListDatabase): SupermarketDao = db.supermarketDao()

    @Provides
    fun provideCategoryDao(db: ShoppingListDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideArticuloSupermarketDefaultDao(db: ShoppingListDatabase): ArticuloSupermarketDefaultDao = db.articuloSupermarketDefaultDao()

    @Provides
    fun provideCategorySupermarketOrderDao(db: ShoppingListDatabase): CategorySupermarketOrderDao = db.categorySupermarketOrderDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAisleRepository(impl: AisleRepositoryImpl): IAisleRepository

    @Binds
    @Singleton
    abstract fun bindSupermarketRepository(impl: SupermarketRepository): ISupermarketRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepository): ICategoryRepository

    @Binds
    @Singleton
    abstract fun bindArticuloSupermarketDefaultRepository(impl: ArticuloSupermarketDefaultRepository): IArticuloSupermarketDefaultRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): IProductRepository

    @Binds
    @Singleton
    abstract fun bindCategorySupermarketOrderRepository(impl: CategorySupermarketOrderRepository): ICategorySupermarketOrderRepository

    @Binds
    @Singleton
    abstract fun bindOpenFoodFactsRepository(impl: OpenFoodFactsRepositoryImpl): IOpenFoodFactsRepository

    @Binds
    @Singleton
    abstract fun bindOfferRepository(impl: com.jose.listacompra.data.repository.OfferRepositoryImpl): com.jose.listacompra.domain.repository.IOfferRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: com.jose.listacompra.data.repository.HistoryRepositoryImpl): com.jose.listacompra.domain.repository.IHistoryRepository
}
