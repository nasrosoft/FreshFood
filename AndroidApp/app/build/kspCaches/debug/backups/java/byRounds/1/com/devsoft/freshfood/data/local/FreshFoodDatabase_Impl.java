package com.devsoft.freshfood.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.devsoft.freshfood.data.local.dao.CreditTransactionDao;
import com.devsoft.freshfood.data.local.dao.CreditTransactionDao_Impl;
import com.devsoft.freshfood.data.local.dao.CustomerDao;
import com.devsoft.freshfood.data.local.dao.CustomerDao_Impl;
import com.devsoft.freshfood.data.local.dao.DeliveryDao;
import com.devsoft.freshfood.data.local.dao.DeliveryDao_Impl;
import com.devsoft.freshfood.data.local.dao.InventoryDao;
import com.devsoft.freshfood.data.local.dao.InventoryDao_Impl;
import com.devsoft.freshfood.data.local.dao.NotificationDao;
import com.devsoft.freshfood.data.local.dao.NotificationDao_Impl;
import com.devsoft.freshfood.data.local.dao.PaymentDao;
import com.devsoft.freshfood.data.local.dao.PaymentDao_Impl;
import com.devsoft.freshfood.data.local.dao.ProductDao;
import com.devsoft.freshfood.data.local.dao.ProductDao_Impl;
import com.devsoft.freshfood.data.local.dao.ProfileDao;
import com.devsoft.freshfood.data.local.dao.ProfileDao_Impl;
import com.devsoft.freshfood.data.local.dao.PurchaseDao;
import com.devsoft.freshfood.data.local.dao.PurchaseDao_Impl;
import com.devsoft.freshfood.data.local.dao.ReturnDao;
import com.devsoft.freshfood.data.local.dao.ReturnDao_Impl;
import com.devsoft.freshfood.data.local.dao.SaleDao;
import com.devsoft.freshfood.data.local.dao.SaleDao_Impl;
import com.devsoft.freshfood.data.local.dao.SaleItemDao;
import com.devsoft.freshfood.data.local.dao.SaleItemDao_Impl;
import com.devsoft.freshfood.data.local.dao.StockDao;
import com.devsoft.freshfood.data.local.dao.StockDao_Impl;
import com.devsoft.freshfood.data.local.dao.SyncQueueDao;
import com.devsoft.freshfood.data.local.dao.SyncQueueDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FreshFoodDatabase_Impl extends FreshFoodDatabase {
  private volatile ProductDao _productDao;

  private volatile CustomerDao _customerDao;

  private volatile SyncQueueDao _syncQueueDao;

  private volatile PaymentDao _paymentDao;

  private volatile CreditTransactionDao _creditTransactionDao;

  private volatile SaleDao _saleDao;

  private volatile SaleItemDao _saleItemDao;

  private volatile StockDao _stockDao;

  private volatile PurchaseDao _purchaseDao;

  private volatile InventoryDao _inventoryDao;

  private volatile ReturnDao _returnDao;

  private volatile DeliveryDao _deliveryDao;

  private volatile ProfileDao _profileDao;

  private volatile NotificationDao _notificationDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` TEXT NOT NULL, `barcode` TEXT, `name` TEXT NOT NULL, `category_id` TEXT, `brand_id` TEXT, `description` TEXT, `image_url` TEXT, `unit` TEXT NOT NULL, `purchase_price` REAL NOT NULL, `selling_price` REAL NOT NULL, `min_selling_price` REAL NOT NULL, `current_stock` INTEGER NOT NULL, `min_stock` INTEGER NOT NULL, `max_stock` INTEGER, `is_active` INTEGER NOT NULL, `created_at` TEXT, `updated_at` TEXT, `deleted_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `address` TEXT, `wilaya` TEXT, `commune` TEXT, `photo_url` TEXT, `credit_limit` REAL NOT NULL, `current_credit` REAL NOT NULL, `customer_type` TEXT, `is_active` INTEGER NOT NULL, `notes` TEXT, `created_at` TEXT, `updated_at` TEXT, `deleted_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entity_type` TEXT NOT NULL, `entity_id` TEXT NOT NULL, `operation` TEXT NOT NULL, `payload` TEXT, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `retry_count` INTEGER NOT NULL, `last_error` TEXT, `status` TEXT NOT NULL, `device_id` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_metadata` (`entity_type` TEXT NOT NULL, `last_sync_at` TEXT, `last_successful_sync` INTEGER NOT NULL, `sync_status` TEXT NOT NULL, PRIMARY KEY(`entity_type`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `payments` (`id` TEXT NOT NULL, `customer_id` TEXT NOT NULL, `amount` REAL NOT NULL, `payment_method` TEXT NOT NULL, `reference_id` TEXT, `user_id` TEXT NOT NULL, `notes` TEXT, `created_at` TEXT, `deleted_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `credit_transactions` (`id` TEXT NOT NULL, `customer_id` TEXT NOT NULL, `amount` REAL NOT NULL, `transaction_type` TEXT NOT NULL, `reference_id` TEXT, `user_id` TEXT NOT NULL, `created_at` TEXT, `deleted_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sales` (`id` TEXT NOT NULL, `invoice_number` TEXT, `customer_id` TEXT, `user_id` TEXT, `total_amount` REAL NOT NULL, `paid_amount` REAL NOT NULL, `credit_amount` REAL NOT NULL, `payment_method` TEXT NOT NULL, `status` TEXT NOT NULL, `deleted_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sale_items` (`id` TEXT NOT NULL, `sale_id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `unit_price` REAL NOT NULL, `subtotal` REAL NOT NULL, `created_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_batches` (`id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `batch_number` TEXT, `expiration_date` TEXT, `quantity` INTEGER NOT NULL, `purchase_price` REAL NOT NULL, `created_at` TEXT, `updated_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_movements` (`id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `batch_id` TEXT, `movement_type` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `reference_id` TEXT, `user_id` TEXT NOT NULL, `notes` TEXT, `created_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `purchases` (`id` TEXT NOT NULL, `supplier_id` TEXT, `invoice_number` TEXT, `total_amount` REAL NOT NULL, `status` TEXT NOT NULL, `created_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `purchase_items` (`id` TEXT NOT NULL, `purchase_id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `batch_id` TEXT, `quantity` INTEGER NOT NULL, `purchase_price` REAL NOT NULL, `expiration_date` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `inventory_sessions` (`id` TEXT NOT NULL, `date` TEXT, `status` TEXT NOT NULL, `conducted_by` TEXT, `notes` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `inventory_items` (`id` TEXT NOT NULL, `session_id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `expected_quantity` INTEGER NOT NULL, `actual_quantity` INTEGER NOT NULL, `difference` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `returns` (`id` TEXT NOT NULL, `date` TEXT, `customer_id` TEXT, `product_id` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `reason` TEXT, `status` TEXT NOT NULL, `created_by` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `delivery_orders` (`id` TEXT NOT NULL, `customer_id` TEXT, `delivery_employee_id` TEXT, `status` TEXT NOT NULL, `notes` TEXT, `created_at` TEXT, `updated_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `delivery_items` (`id` TEXT NOT NULL, `delivery_order_id` TEXT NOT NULL, `product_id` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `created_at` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` TEXT NOT NULL, `first_name` TEXT NOT NULL, `last_name` TEXT NOT NULL, `phone` TEXT, `role` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `is_read` INTEGER NOT NULL, `created_at` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3595b77c5c2a2cd965f6220a97f0c79a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `products`");
        db.execSQL("DROP TABLE IF EXISTS `customers`");
        db.execSQL("DROP TABLE IF EXISTS `sync_queue`");
        db.execSQL("DROP TABLE IF EXISTS `sync_metadata`");
        db.execSQL("DROP TABLE IF EXISTS `payments`");
        db.execSQL("DROP TABLE IF EXISTS `credit_transactions`");
        db.execSQL("DROP TABLE IF EXISTS `sales`");
        db.execSQL("DROP TABLE IF EXISTS `sale_items`");
        db.execSQL("DROP TABLE IF EXISTS `stock_batches`");
        db.execSQL("DROP TABLE IF EXISTS `stock_movements`");
        db.execSQL("DROP TABLE IF EXISTS `purchases`");
        db.execSQL("DROP TABLE IF EXISTS `purchase_items`");
        db.execSQL("DROP TABLE IF EXISTS `inventory_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `inventory_items`");
        db.execSQL("DROP TABLE IF EXISTS `returns`");
        db.execSQL("DROP TABLE IF EXISTS `delivery_orders`");
        db.execSQL("DROP TABLE IF EXISTS `delivery_items`");
        db.execSQL("DROP TABLE IF EXISTS `profiles`");
        db.execSQL("DROP TABLE IF EXISTS `notifications`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProducts = new HashMap<String, TableInfo.Column>(18);
        _columnsProducts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("barcode", new TableInfo.Column("barcode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("category_id", new TableInfo.Column("category_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("brand_id", new TableInfo.Column("brand_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("image_url", new TableInfo.Column("image_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("purchase_price", new TableInfo.Column("purchase_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("selling_price", new TableInfo.Column("selling_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("min_selling_price", new TableInfo.Column("min_selling_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("current_stock", new TableInfo.Column("current_stock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("min_stock", new TableInfo.Column("min_stock", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("max_stock", new TableInfo.Column("max_stock", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("updated_at", new TableInfo.Column("updated_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("deleted_at", new TableInfo.Column("deleted_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProducts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProducts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProducts = new TableInfo("products", _columnsProducts, _foreignKeysProducts, _indicesProducts);
        final TableInfo _existingProducts = TableInfo.read(db, "products");
        if (!_infoProducts.equals(_existingProducts)) {
          return new RoomOpenHelper.ValidationResult(false, "products(com.devsoft.freshfood.data.local.entity.ProductEntity).\n"
                  + " Expected:\n" + _infoProducts + "\n"
                  + " Found:\n" + _existingProducts);
        }
        final HashMap<String, TableInfo.Column> _columnsCustomers = new HashMap<String, TableInfo.Column>(15);
        _columnsCustomers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("phone", new TableInfo.Column("phone", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("address", new TableInfo.Column("address", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("wilaya", new TableInfo.Column("wilaya", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("commune", new TableInfo.Column("commune", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("photo_url", new TableInfo.Column("photo_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("credit_limit", new TableInfo.Column("credit_limit", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("current_credit", new TableInfo.Column("current_credit", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("customer_type", new TableInfo.Column("customer_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("updated_at", new TableInfo.Column("updated_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("deleted_at", new TableInfo.Column("deleted_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCustomers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCustomers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCustomers = new TableInfo("customers", _columnsCustomers, _foreignKeysCustomers, _indicesCustomers);
        final TableInfo _existingCustomers = TableInfo.read(db, "customers");
        if (!_infoCustomers.equals(_existingCustomers)) {
          return new RoomOpenHelper.ValidationResult(false, "customers(com.devsoft.freshfood.data.local.entity.CustomerEntity).\n"
                  + " Expected:\n" + _infoCustomers + "\n"
                  + " Found:\n" + _existingCustomers);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncQueue = new HashMap<String, TableInfo.Column>(11);
        _columnsSyncQueue.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("entity_type", new TableInfo.Column("entity_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("entity_id", new TableInfo.Column("entity_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("operation", new TableInfo.Column("operation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("payload", new TableInfo.Column("payload", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("retry_count", new TableInfo.Column("retry_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("last_error", new TableInfo.Column("last_error", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("device_id", new TableInfo.Column("device_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncQueue = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncQueue = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncQueue = new TableInfo("sync_queue", _columnsSyncQueue, _foreignKeysSyncQueue, _indicesSyncQueue);
        final TableInfo _existingSyncQueue = TableInfo.read(db, "sync_queue");
        if (!_infoSyncQueue.equals(_existingSyncQueue)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_queue(com.devsoft.freshfood.data.local.entity.SyncQueueEntity).\n"
                  + " Expected:\n" + _infoSyncQueue + "\n"
                  + " Found:\n" + _existingSyncQueue);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncMetadata = new HashMap<String, TableInfo.Column>(4);
        _columnsSyncMetadata.put("entity_type", new TableInfo.Column("entity_type", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncMetadata.put("last_sync_at", new TableInfo.Column("last_sync_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncMetadata.put("last_successful_sync", new TableInfo.Column("last_successful_sync", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncMetadata.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncMetadata = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncMetadata = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncMetadata = new TableInfo("sync_metadata", _columnsSyncMetadata, _foreignKeysSyncMetadata, _indicesSyncMetadata);
        final TableInfo _existingSyncMetadata = TableInfo.read(db, "sync_metadata");
        if (!_infoSyncMetadata.equals(_existingSyncMetadata)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_metadata(com.devsoft.freshfood.data.local.entity.SyncMetadataEntity).\n"
                  + " Expected:\n" + _infoSyncMetadata + "\n"
                  + " Found:\n" + _existingSyncMetadata);
        }
        final HashMap<String, TableInfo.Column> _columnsPayments = new HashMap<String, TableInfo.Column>(9);
        _columnsPayments.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("customer_id", new TableInfo.Column("customer_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("payment_method", new TableInfo.Column("payment_method", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("reference_id", new TableInfo.Column("reference_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("deleted_at", new TableInfo.Column("deleted_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPayments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPayments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPayments = new TableInfo("payments", _columnsPayments, _foreignKeysPayments, _indicesPayments);
        final TableInfo _existingPayments = TableInfo.read(db, "payments");
        if (!_infoPayments.equals(_existingPayments)) {
          return new RoomOpenHelper.ValidationResult(false, "payments(com.devsoft.freshfood.data.local.entity.PaymentEntity).\n"
                  + " Expected:\n" + _infoPayments + "\n"
                  + " Found:\n" + _existingPayments);
        }
        final HashMap<String, TableInfo.Column> _columnsCreditTransactions = new HashMap<String, TableInfo.Column>(8);
        _columnsCreditTransactions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("customer_id", new TableInfo.Column("customer_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("transaction_type", new TableInfo.Column("transaction_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("reference_id", new TableInfo.Column("reference_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCreditTransactions.put("deleted_at", new TableInfo.Column("deleted_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCreditTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCreditTransactions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCreditTransactions = new TableInfo("credit_transactions", _columnsCreditTransactions, _foreignKeysCreditTransactions, _indicesCreditTransactions);
        final TableInfo _existingCreditTransactions = TableInfo.read(db, "credit_transactions");
        if (!_infoCreditTransactions.equals(_existingCreditTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "credit_transactions(com.devsoft.freshfood.data.local.entity.CreditTransactionEntity).\n"
                  + " Expected:\n" + _infoCreditTransactions + "\n"
                  + " Found:\n" + _existingCreditTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsSales = new HashMap<String, TableInfo.Column>(10);
        _columnsSales.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("invoice_number", new TableInfo.Column("invoice_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("customer_id", new TableInfo.Column("customer_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("user_id", new TableInfo.Column("user_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("total_amount", new TableInfo.Column("total_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("paid_amount", new TableInfo.Column("paid_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("credit_amount", new TableInfo.Column("credit_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("payment_method", new TableInfo.Column("payment_method", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSales.put("deleted_at", new TableInfo.Column("deleted_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSales = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSales = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSales = new TableInfo("sales", _columnsSales, _foreignKeysSales, _indicesSales);
        final TableInfo _existingSales = TableInfo.read(db, "sales");
        if (!_infoSales.equals(_existingSales)) {
          return new RoomOpenHelper.ValidationResult(false, "sales(com.devsoft.freshfood.data.local.entity.SaleEntity).\n"
                  + " Expected:\n" + _infoSales + "\n"
                  + " Found:\n" + _existingSales);
        }
        final HashMap<String, TableInfo.Column> _columnsSaleItems = new HashMap<String, TableInfo.Column>(7);
        _columnsSaleItems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleItems.put("sale_id", new TableInfo.Column("sale_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleItems.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleItems.put("unit_price", new TableInfo.Column("unit_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleItems.put("subtotal", new TableInfo.Column("subtotal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaleItems.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSaleItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSaleItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSaleItems = new TableInfo("sale_items", _columnsSaleItems, _foreignKeysSaleItems, _indicesSaleItems);
        final TableInfo _existingSaleItems = TableInfo.read(db, "sale_items");
        if (!_infoSaleItems.equals(_existingSaleItems)) {
          return new RoomOpenHelper.ValidationResult(false, "sale_items(com.devsoft.freshfood.data.local.entity.SaleItemEntity).\n"
                  + " Expected:\n" + _infoSaleItems + "\n"
                  + " Found:\n" + _existingSaleItems);
        }
        final HashMap<String, TableInfo.Column> _columnsStockBatches = new HashMap<String, TableInfo.Column>(8);
        _columnsStockBatches.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("batch_number", new TableInfo.Column("batch_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("expiration_date", new TableInfo.Column("expiration_date", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("purchase_price", new TableInfo.Column("purchase_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockBatches.put("updated_at", new TableInfo.Column("updated_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockBatches = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockBatches = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockBatches = new TableInfo("stock_batches", _columnsStockBatches, _foreignKeysStockBatches, _indicesStockBatches);
        final TableInfo _existingStockBatches = TableInfo.read(db, "stock_batches");
        if (!_infoStockBatches.equals(_existingStockBatches)) {
          return new RoomOpenHelper.ValidationResult(false, "stock_batches(com.devsoft.freshfood.data.local.entity.StockBatchEntity).\n"
                  + " Expected:\n" + _infoStockBatches + "\n"
                  + " Found:\n" + _existingStockBatches);
        }
        final HashMap<String, TableInfo.Column> _columnsStockMovements = new HashMap<String, TableInfo.Column>(9);
        _columnsStockMovements.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("batch_id", new TableInfo.Column("batch_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("movement_type", new TableInfo.Column("movement_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("reference_id", new TableInfo.Column("reference_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockMovements.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockMovements = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockMovements = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockMovements = new TableInfo("stock_movements", _columnsStockMovements, _foreignKeysStockMovements, _indicesStockMovements);
        final TableInfo _existingStockMovements = TableInfo.read(db, "stock_movements");
        if (!_infoStockMovements.equals(_existingStockMovements)) {
          return new RoomOpenHelper.ValidationResult(false, "stock_movements(com.devsoft.freshfood.data.local.entity.StockMovementEntity).\n"
                  + " Expected:\n" + _infoStockMovements + "\n"
                  + " Found:\n" + _existingStockMovements);
        }
        final HashMap<String, TableInfo.Column> _columnsPurchases = new HashMap<String, TableInfo.Column>(6);
        _columnsPurchases.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("supplier_id", new TableInfo.Column("supplier_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("invoice_number", new TableInfo.Column("invoice_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("total_amount", new TableInfo.Column("total_amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchases.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPurchases = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPurchases = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPurchases = new TableInfo("purchases", _columnsPurchases, _foreignKeysPurchases, _indicesPurchases);
        final TableInfo _existingPurchases = TableInfo.read(db, "purchases");
        if (!_infoPurchases.equals(_existingPurchases)) {
          return new RoomOpenHelper.ValidationResult(false, "purchases(com.devsoft.freshfood.data.local.entity.PurchaseEntity).\n"
                  + " Expected:\n" + _infoPurchases + "\n"
                  + " Found:\n" + _existingPurchases);
        }
        final HashMap<String, TableInfo.Column> _columnsPurchaseItems = new HashMap<String, TableInfo.Column>(7);
        _columnsPurchaseItems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseItems.put("purchase_id", new TableInfo.Column("purchase_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseItems.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseItems.put("batch_id", new TableInfo.Column("batch_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseItems.put("purchase_price", new TableInfo.Column("purchase_price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchaseItems.put("expiration_date", new TableInfo.Column("expiration_date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPurchaseItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPurchaseItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPurchaseItems = new TableInfo("purchase_items", _columnsPurchaseItems, _foreignKeysPurchaseItems, _indicesPurchaseItems);
        final TableInfo _existingPurchaseItems = TableInfo.read(db, "purchase_items");
        if (!_infoPurchaseItems.equals(_existingPurchaseItems)) {
          return new RoomOpenHelper.ValidationResult(false, "purchase_items(com.devsoft.freshfood.data.local.entity.PurchaseItemEntity).\n"
                  + " Expected:\n" + _infoPurchaseItems + "\n"
                  + " Found:\n" + _existingPurchaseItems);
        }
        final HashMap<String, TableInfo.Column> _columnsInventorySessions = new HashMap<String, TableInfo.Column>(5);
        _columnsInventorySessions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventorySessions.put("date", new TableInfo.Column("date", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventorySessions.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventorySessions.put("conducted_by", new TableInfo.Column("conducted_by", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventorySessions.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInventorySessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInventorySessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInventorySessions = new TableInfo("inventory_sessions", _columnsInventorySessions, _foreignKeysInventorySessions, _indicesInventorySessions);
        final TableInfo _existingInventorySessions = TableInfo.read(db, "inventory_sessions");
        if (!_infoInventorySessions.equals(_existingInventorySessions)) {
          return new RoomOpenHelper.ValidationResult(false, "inventory_sessions(com.devsoft.freshfood.data.local.entity.InventorySessionEntity).\n"
                  + " Expected:\n" + _infoInventorySessions + "\n"
                  + " Found:\n" + _existingInventorySessions);
        }
        final HashMap<String, TableInfo.Column> _columnsInventoryItems = new HashMap<String, TableInfo.Column>(6);
        _columnsInventoryItems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventoryItems.put("session_id", new TableInfo.Column("session_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventoryItems.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventoryItems.put("expected_quantity", new TableInfo.Column("expected_quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventoryItems.put("actual_quantity", new TableInfo.Column("actual_quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInventoryItems.put("difference", new TableInfo.Column("difference", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInventoryItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInventoryItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInventoryItems = new TableInfo("inventory_items", _columnsInventoryItems, _foreignKeysInventoryItems, _indicesInventoryItems);
        final TableInfo _existingInventoryItems = TableInfo.read(db, "inventory_items");
        if (!_infoInventoryItems.equals(_existingInventoryItems)) {
          return new RoomOpenHelper.ValidationResult(false, "inventory_items(com.devsoft.freshfood.data.local.entity.InventoryItemEntity).\n"
                  + " Expected:\n" + _infoInventoryItems + "\n"
                  + " Found:\n" + _existingInventoryItems);
        }
        final HashMap<String, TableInfo.Column> _columnsReturns = new HashMap<String, TableInfo.Column>(8);
        _columnsReturns.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("date", new TableInfo.Column("date", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("customer_id", new TableInfo.Column("customer_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("reason", new TableInfo.Column("reason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReturns.put("created_by", new TableInfo.Column("created_by", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReturns = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReturns = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReturns = new TableInfo("returns", _columnsReturns, _foreignKeysReturns, _indicesReturns);
        final TableInfo _existingReturns = TableInfo.read(db, "returns");
        if (!_infoReturns.equals(_existingReturns)) {
          return new RoomOpenHelper.ValidationResult(false, "returns(com.devsoft.freshfood.data.local.entity.ReturnEntity).\n"
                  + " Expected:\n" + _infoReturns + "\n"
                  + " Found:\n" + _existingReturns);
        }
        final HashMap<String, TableInfo.Column> _columnsDeliveryOrders = new HashMap<String, TableInfo.Column>(7);
        _columnsDeliveryOrders.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryOrders.put("customer_id", new TableInfo.Column("customer_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryOrders.put("delivery_employee_id", new TableInfo.Column("delivery_employee_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryOrders.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryOrders.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryOrders.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryOrders.put("updated_at", new TableInfo.Column("updated_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeliveryOrders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeliveryOrders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeliveryOrders = new TableInfo("delivery_orders", _columnsDeliveryOrders, _foreignKeysDeliveryOrders, _indicesDeliveryOrders);
        final TableInfo _existingDeliveryOrders = TableInfo.read(db, "delivery_orders");
        if (!_infoDeliveryOrders.equals(_existingDeliveryOrders)) {
          return new RoomOpenHelper.ValidationResult(false, "delivery_orders(com.devsoft.freshfood.data.local.entity.DeliveryOrderEntity).\n"
                  + " Expected:\n" + _infoDeliveryOrders + "\n"
                  + " Found:\n" + _existingDeliveryOrders);
        }
        final HashMap<String, TableInfo.Column> _columnsDeliveryItems = new HashMap<String, TableInfo.Column>(5);
        _columnsDeliveryItems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryItems.put("delivery_order_id", new TableInfo.Column("delivery_order_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryItems.put("product_id", new TableInfo.Column("product_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeliveryItems.put("created_at", new TableInfo.Column("created_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeliveryItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeliveryItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeliveryItems = new TableInfo("delivery_items", _columnsDeliveryItems, _foreignKeysDeliveryItems, _indicesDeliveryItems);
        final TableInfo _existingDeliveryItems = TableInfo.read(db, "delivery_items");
        if (!_infoDeliveryItems.equals(_existingDeliveryItems)) {
          return new RoomOpenHelper.ValidationResult(false, "delivery_items(com.devsoft.freshfood.data.local.entity.DeliveryItemEntity).\n"
                  + " Expected:\n" + _infoDeliveryItems + "\n"
                  + " Found:\n" + _existingDeliveryItems);
        }
        final HashMap<String, TableInfo.Column> _columnsProfiles = new HashMap<String, TableInfo.Column>(8);
        _columnsProfiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("first_name", new TableInfo.Column("first_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("last_name", new TableInfo.Column("last_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("phone", new TableInfo.Column("phone", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProfiles.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProfiles = new TableInfo("profiles", _columnsProfiles, _foreignKeysProfiles, _indicesProfiles);
        final TableInfo _existingProfiles = TableInfo.read(db, "profiles");
        if (!_infoProfiles.equals(_existingProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "profiles(com.devsoft.freshfood.data.local.entity.ProfileEntity).\n"
                  + " Expected:\n" + _infoProfiles + "\n"
                  + " Found:\n" + _existingProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsNotifications = new HashMap<String, TableInfo.Column>(6);
        _columnsNotifications.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("is_read", new TableInfo.Column("is_read", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotifications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotifications = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNotifications = new TableInfo("notifications", _columnsNotifications, _foreignKeysNotifications, _indicesNotifications);
        final TableInfo _existingNotifications = TableInfo.read(db, "notifications");
        if (!_infoNotifications.equals(_existingNotifications)) {
          return new RoomOpenHelper.ValidationResult(false, "notifications(com.devsoft.freshfood.data.local.entity.NotificationEntity).\n"
                  + " Expected:\n" + _infoNotifications + "\n"
                  + " Found:\n" + _existingNotifications);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "3595b77c5c2a2cd965f6220a97f0c79a", "cb50a2dd00ac0a4bb3fece8761db53c0");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "products","customers","sync_queue","sync_metadata","payments","credit_transactions","sales","sale_items","stock_batches","stock_movements","purchases","purchase_items","inventory_sessions","inventory_items","returns","delivery_orders","delivery_items","profiles","notifications");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `products`");
      _db.execSQL("DELETE FROM `customers`");
      _db.execSQL("DELETE FROM `sync_queue`");
      _db.execSQL("DELETE FROM `sync_metadata`");
      _db.execSQL("DELETE FROM `payments`");
      _db.execSQL("DELETE FROM `credit_transactions`");
      _db.execSQL("DELETE FROM `sales`");
      _db.execSQL("DELETE FROM `sale_items`");
      _db.execSQL("DELETE FROM `stock_batches`");
      _db.execSQL("DELETE FROM `stock_movements`");
      _db.execSQL("DELETE FROM `purchases`");
      _db.execSQL("DELETE FROM `purchase_items`");
      _db.execSQL("DELETE FROM `inventory_sessions`");
      _db.execSQL("DELETE FROM `inventory_items`");
      _db.execSQL("DELETE FROM `returns`");
      _db.execSQL("DELETE FROM `delivery_orders`");
      _db.execSQL("DELETE FROM `delivery_items`");
      _db.execSQL("DELETE FROM `profiles`");
      _db.execSQL("DELETE FROM `notifications`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProductDao.class, ProductDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CustomerDao.class, CustomerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncQueueDao.class, SyncQueueDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PaymentDao.class, PaymentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CreditTransactionDao.class, CreditTransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SaleDao.class, SaleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SaleItemDao.class, SaleItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StockDao.class, StockDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PurchaseDao.class, PurchaseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InventoryDao.class, InventoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReturnDao.class, ReturnDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DeliveryDao.class, DeliveryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProfileDao.class, ProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(NotificationDao.class, NotificationDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProductDao productDao() {
    if (_productDao != null) {
      return _productDao;
    } else {
      synchronized(this) {
        if(_productDao == null) {
          _productDao = new ProductDao_Impl(this);
        }
        return _productDao;
      }
    }
  }

  @Override
  public CustomerDao customerDao() {
    if (_customerDao != null) {
      return _customerDao;
    } else {
      synchronized(this) {
        if(_customerDao == null) {
          _customerDao = new CustomerDao_Impl(this);
        }
        return _customerDao;
      }
    }
  }

  @Override
  public SyncQueueDao syncQueueDao() {
    if (_syncQueueDao != null) {
      return _syncQueueDao;
    } else {
      synchronized(this) {
        if(_syncQueueDao == null) {
          _syncQueueDao = new SyncQueueDao_Impl(this);
        }
        return _syncQueueDao;
      }
    }
  }

  @Override
  public PaymentDao paymentDao() {
    if (_paymentDao != null) {
      return _paymentDao;
    } else {
      synchronized(this) {
        if(_paymentDao == null) {
          _paymentDao = new PaymentDao_Impl(this);
        }
        return _paymentDao;
      }
    }
  }

  @Override
  public CreditTransactionDao creditTransactionDao() {
    if (_creditTransactionDao != null) {
      return _creditTransactionDao;
    } else {
      synchronized(this) {
        if(_creditTransactionDao == null) {
          _creditTransactionDao = new CreditTransactionDao_Impl(this);
        }
        return _creditTransactionDao;
      }
    }
  }

  @Override
  public SaleDao saleDao() {
    if (_saleDao != null) {
      return _saleDao;
    } else {
      synchronized(this) {
        if(_saleDao == null) {
          _saleDao = new SaleDao_Impl(this);
        }
        return _saleDao;
      }
    }
  }

  @Override
  public SaleItemDao saleItemDao() {
    if (_saleItemDao != null) {
      return _saleItemDao;
    } else {
      synchronized(this) {
        if(_saleItemDao == null) {
          _saleItemDao = new SaleItemDao_Impl(this);
        }
        return _saleItemDao;
      }
    }
  }

  @Override
  public StockDao stockDao() {
    if (_stockDao != null) {
      return _stockDao;
    } else {
      synchronized(this) {
        if(_stockDao == null) {
          _stockDao = new StockDao_Impl(this);
        }
        return _stockDao;
      }
    }
  }

  @Override
  public PurchaseDao purchaseDao() {
    if (_purchaseDao != null) {
      return _purchaseDao;
    } else {
      synchronized(this) {
        if(_purchaseDao == null) {
          _purchaseDao = new PurchaseDao_Impl(this);
        }
        return _purchaseDao;
      }
    }
  }

  @Override
  public InventoryDao inventoryDao() {
    if (_inventoryDao != null) {
      return _inventoryDao;
    } else {
      synchronized(this) {
        if(_inventoryDao == null) {
          _inventoryDao = new InventoryDao_Impl(this);
        }
        return _inventoryDao;
      }
    }
  }

  @Override
  public ReturnDao returnDao() {
    if (_returnDao != null) {
      return _returnDao;
    } else {
      synchronized(this) {
        if(_returnDao == null) {
          _returnDao = new ReturnDao_Impl(this);
        }
        return _returnDao;
      }
    }
  }

  @Override
  public DeliveryDao deliveryDao() {
    if (_deliveryDao != null) {
      return _deliveryDao;
    } else {
      synchronized(this) {
        if(_deliveryDao == null) {
          _deliveryDao = new DeliveryDao_Impl(this);
        }
        return _deliveryDao;
      }
    }
  }

  @Override
  public ProfileDao profileDao() {
    if (_profileDao != null) {
      return _profileDao;
    } else {
      synchronized(this) {
        if(_profileDao == null) {
          _profileDao = new ProfileDao_Impl(this);
        }
        return _profileDao;
      }
    }
  }

  @Override
  public NotificationDao notificationDao() {
    if (_notificationDao != null) {
      return _notificationDao;
    } else {
      synchronized(this) {
        if(_notificationDao == null) {
          _notificationDao = new NotificationDao_Impl(this);
        }
        return _notificationDao;
      }
    }
  }
}
