package com.jose.listacompra.di

import android.content.Context
import androidx.room.Room
import com.jose.listacompra.data.local.dao.AisleDao
import com.jose.listacompra.data.local.dao.OfferDao
import com.jose.listacompra.data.local.dao.ProductDao
import com.jose.listacompra.data.local.dao.ProductHistoryDao
import com.jose.listacompra.data.local.dao.ShoppingListDao
import com.jose.listacompra.data.local.ShoppingListDatabase
import com.jose.listacompra.data.local.ShoppingListDatabase.Companion.MIGRATION_6_7
import com.jose.listacompra.data.local.dao.ProductFrequencyDao
import com.jose.listacompra.data.local.dao.ProductPriceHistoryDao
import com.jose.listacompra.data.local.dao.PurchaseHistoryDao
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
        ).fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_6_7)
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
    fun provideProductHistoryDao(db: ShoppingListDatabase): ProductHistoryDao =
        db.productHistoryDao()

    @Provides
    fun providePurchaseHistoryDao(database: ShoppingListDatabase): PurchaseHistoryDao {
        return database.purchaseHistoryDao()
    }

   @Provides
    fun provideProductPriceHistoryDao(db: ShoppingListDatabase): ProductPriceHistoryDao =
        db.productPriceHistoryDao()

    @Provides
    fun provideProductFrequencyDao(db: ShoppingListDatabase): ProductFrequencyDao =
        db.productFrequencyDao()


}