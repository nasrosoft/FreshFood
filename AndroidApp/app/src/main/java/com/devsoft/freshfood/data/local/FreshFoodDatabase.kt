package com.devsoft.freshfood.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devsoft.freshfood.data.local.dao.*
import com.devsoft.freshfood.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SyncQueueEntity::class,
        SyncMetadataEntity::class,
        PaymentEntity::class,
        CreditTransactionEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockBatchEntity::class,
        StockMovementEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        InventorySessionEntity::class,
        InventoryItemEntity::class,
        ReturnEntity::class,
        DeliveryOrderEntity::class,
        DeliveryItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FreshFoodDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun paymentDao(): PaymentDao
    abstract fun creditTransactionDao(): CreditTransactionDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun stockDao(): StockDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun returnDao(): ReturnDao
    abstract fun deliveryDao(): DeliveryDao

    companion object {
        @Volatile
        private var INSTANCE: FreshFoodDatabase? = null

        fun getDatabase(context: Context): FreshFoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FreshFoodDatabase::class.java,
                    "freshfood_database"
                )
                .fallbackToDestructiveMigration() // Use migrations in production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
