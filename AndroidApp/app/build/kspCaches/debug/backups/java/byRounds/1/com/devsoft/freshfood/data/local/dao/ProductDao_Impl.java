package com.devsoft.freshfood.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.ProductEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProductDao_Impl implements ProductDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProductEntity> __insertionAdapterOfProductEntity;

  private final EntityDeletionOrUpdateAdapter<ProductEntity> __updateAdapterOfProductEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteProduct;

  public ProductDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProductEntity = new EntityInsertionAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `products` (`id`,`barcode`,`name`,`category_id`,`brand_id`,`description`,`image_url`,`unit`,`purchase_price`,`selling_price`,`min_selling_price`,`current_stock`,`min_stock`,`max_stock`,`is_active`,`created_at`,`updated_at`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getBarcode() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getBarcode());
        }
        statement.bindString(3, entity.getName());
        if (entity.getCategory_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCategory_id());
        }
        if (entity.getBrand_id() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBrand_id());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDescription());
        }
        if (entity.getImage_url() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getImage_url());
        }
        statement.bindString(8, entity.getUnit());
        statement.bindDouble(9, entity.getPurchase_price());
        statement.bindDouble(10, entity.getSelling_price());
        statement.bindDouble(11, entity.getMin_selling_price());
        statement.bindLong(12, entity.getCurrent_stock());
        statement.bindLong(13, entity.getMin_stock());
        if (entity.getMax_stock() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getMax_stock());
        }
        final int _tmp = entity.is_active() ? 1 : 0;
        statement.bindLong(15, _tmp);
        if (entity.getCreated_at() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getCreated_at());
        }
        if (entity.getUpdated_at() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getUpdated_at());
        }
        if (entity.getDeleted_at() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getDeleted_at());
        }
      }
    };
    this.__updateAdapterOfProductEntity = new EntityDeletionOrUpdateAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `products` SET `id` = ?,`barcode` = ?,`name` = ?,`category_id` = ?,`brand_id` = ?,`description` = ?,`image_url` = ?,`unit` = ?,`purchase_price` = ?,`selling_price` = ?,`min_selling_price` = ?,`current_stock` = ?,`min_stock` = ?,`max_stock` = ?,`is_active` = ?,`created_at` = ?,`updated_at` = ?,`deleted_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getBarcode() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getBarcode());
        }
        statement.bindString(3, entity.getName());
        if (entity.getCategory_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCategory_id());
        }
        if (entity.getBrand_id() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBrand_id());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDescription());
        }
        if (entity.getImage_url() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getImage_url());
        }
        statement.bindString(8, entity.getUnit());
        statement.bindDouble(9, entity.getPurchase_price());
        statement.bindDouble(10, entity.getSelling_price());
        statement.bindDouble(11, entity.getMin_selling_price());
        statement.bindLong(12, entity.getCurrent_stock());
        statement.bindLong(13, entity.getMin_stock());
        if (entity.getMax_stock() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getMax_stock());
        }
        final int _tmp = entity.is_active() ? 1 : 0;
        statement.bindLong(15, _tmp);
        if (entity.getCreated_at() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getCreated_at());
        }
        if (entity.getUpdated_at() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getUpdated_at());
        }
        if (entity.getDeleted_at() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getDeleted_at());
        }
        statement.bindString(19, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDeleteProduct = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE products SET deleted_at = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProduct(final ProductEntity product,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProductEntity.insert(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertProducts(final List<ProductEntity> products,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProductEntity.insert(products);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProduct(final ProductEntity product,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProductEntity.handle(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteProduct(final String id, final String deletedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteProduct.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, deletedAt);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSoftDeleteProduct.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ProductEntity>> getAllProducts() {
    final String _sql = "SELECT * FROM products WHERE deleted_at IS NULL ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"products"}, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfBrandId = CursorUtil.getColumnIndexOrThrow(_cursor, "brand_id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "image_url");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPurchasePrice = CursorUtil.getColumnIndexOrThrow(_cursor, "purchase_price");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "selling_price");
          final int _cursorIndexOfMinSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "min_selling_price");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "min_stock");
          final int _cursorIndexOfMaxStock = CursorUtil.getColumnIndexOrThrow(_cursor, "max_stock");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory_id;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategory_id = null;
            } else {
              _tmpCategory_id = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpBrand_id;
            if (_cursor.isNull(_cursorIndexOfBrandId)) {
              _tmpBrand_id = null;
            } else {
              _tmpBrand_id = _cursor.getString(_cursorIndexOfBrandId);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImage_url;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImage_url = null;
            } else {
              _tmpImage_url = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPurchase_price;
            _tmpPurchase_price = _cursor.getDouble(_cursorIndexOfPurchasePrice);
            final double _tmpSelling_price;
            _tmpSelling_price = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final double _tmpMin_selling_price;
            _tmpMin_selling_price = _cursor.getDouble(_cursorIndexOfMinSellingPrice);
            final int _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMin_stock;
            _tmpMin_stock = _cursor.getInt(_cursorIndexOfMinStock);
            final Integer _tmpMax_stock;
            if (_cursor.isNull(_cursorIndexOfMaxStock)) {
              _tmpMax_stock = null;
            } else {
              _tmpMax_stock = _cursor.getInt(_cursorIndexOfMaxStock);
            }
            final boolean _tmpIs_active;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIs_active = _tmp != 0;
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdated_at;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdated_at = null;
            } else {
              _tmpUpdated_at = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _item = new ProductEntity(_tmpId,_tmpBarcode,_tmpName,_tmpCategory_id,_tmpBrand_id,_tmpDescription,_tmpImage_url,_tmpUnit,_tmpPurchase_price,_tmpSelling_price,_tmpMin_selling_price,_tmpCurrent_stock,_tmpMin_stock,_tmpMax_stock,_tmpIs_active,_tmpCreated_at,_tmpUpdated_at,_tmpDeleted_at);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProductById(final String id,
      final Continuation<? super ProductEntity> $completion) {
    final String _sql = "SELECT * FROM products WHERE id = ? AND deleted_at IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductEntity>() {
      @Override
      @Nullable
      public ProductEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfBrandId = CursorUtil.getColumnIndexOrThrow(_cursor, "brand_id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "image_url");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPurchasePrice = CursorUtil.getColumnIndexOrThrow(_cursor, "purchase_price");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "selling_price");
          final int _cursorIndexOfMinSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "min_selling_price");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "min_stock");
          final int _cursorIndexOfMaxStock = CursorUtil.getColumnIndexOrThrow(_cursor, "max_stock");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final ProductEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory_id;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategory_id = null;
            } else {
              _tmpCategory_id = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpBrand_id;
            if (_cursor.isNull(_cursorIndexOfBrandId)) {
              _tmpBrand_id = null;
            } else {
              _tmpBrand_id = _cursor.getString(_cursorIndexOfBrandId);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImage_url;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImage_url = null;
            } else {
              _tmpImage_url = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPurchase_price;
            _tmpPurchase_price = _cursor.getDouble(_cursorIndexOfPurchasePrice);
            final double _tmpSelling_price;
            _tmpSelling_price = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final double _tmpMin_selling_price;
            _tmpMin_selling_price = _cursor.getDouble(_cursorIndexOfMinSellingPrice);
            final int _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMin_stock;
            _tmpMin_stock = _cursor.getInt(_cursorIndexOfMinStock);
            final Integer _tmpMax_stock;
            if (_cursor.isNull(_cursorIndexOfMaxStock)) {
              _tmpMax_stock = null;
            } else {
              _tmpMax_stock = _cursor.getInt(_cursorIndexOfMaxStock);
            }
            final boolean _tmpIs_active;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIs_active = _tmp != 0;
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdated_at;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdated_at = null;
            } else {
              _tmpUpdated_at = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _result = new ProductEntity(_tmpId,_tmpBarcode,_tmpName,_tmpCategory_id,_tmpBrand_id,_tmpDescription,_tmpImage_url,_tmpUnit,_tmpPurchase_price,_tmpSelling_price,_tmpMin_selling_price,_tmpCurrent_stock,_tmpMin_stock,_tmpMax_stock,_tmpIs_active,_tmpCreated_at,_tmpUpdated_at,_tmpDeleted_at);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getProductByBarcode(final String barcode,
      final Continuation<? super ProductEntity> $completion) {
    final String _sql = "SELECT * FROM products WHERE barcode = ? AND deleted_at IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, barcode);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductEntity>() {
      @Override
      @Nullable
      public ProductEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfBrandId = CursorUtil.getColumnIndexOrThrow(_cursor, "brand_id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "image_url");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfPurchasePrice = CursorUtil.getColumnIndexOrThrow(_cursor, "purchase_price");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "selling_price");
          final int _cursorIndexOfMinSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "min_selling_price");
          final int _cursorIndexOfCurrentStock = CursorUtil.getColumnIndexOrThrow(_cursor, "current_stock");
          final int _cursorIndexOfMinStock = CursorUtil.getColumnIndexOrThrow(_cursor, "min_stock");
          final int _cursorIndexOfMaxStock = CursorUtil.getColumnIndexOrThrow(_cursor, "max_stock");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final ProductEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory_id;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategory_id = null;
            } else {
              _tmpCategory_id = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpBrand_id;
            if (_cursor.isNull(_cursorIndexOfBrandId)) {
              _tmpBrand_id = null;
            } else {
              _tmpBrand_id = _cursor.getString(_cursorIndexOfBrandId);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImage_url;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImage_url = null;
            } else {
              _tmpImage_url = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final double _tmpPurchase_price;
            _tmpPurchase_price = _cursor.getDouble(_cursorIndexOfPurchasePrice);
            final double _tmpSelling_price;
            _tmpSelling_price = _cursor.getDouble(_cursorIndexOfSellingPrice);
            final double _tmpMin_selling_price;
            _tmpMin_selling_price = _cursor.getDouble(_cursorIndexOfMinSellingPrice);
            final int _tmpCurrent_stock;
            _tmpCurrent_stock = _cursor.getInt(_cursorIndexOfCurrentStock);
            final int _tmpMin_stock;
            _tmpMin_stock = _cursor.getInt(_cursorIndexOfMinStock);
            final Integer _tmpMax_stock;
            if (_cursor.isNull(_cursorIndexOfMaxStock)) {
              _tmpMax_stock = null;
            } else {
              _tmpMax_stock = _cursor.getInt(_cursorIndexOfMaxStock);
            }
            final boolean _tmpIs_active;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIs_active = _tmp != 0;
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdated_at;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdated_at = null;
            } else {
              _tmpUpdated_at = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _result = new ProductEntity(_tmpId,_tmpBarcode,_tmpName,_tmpCategory_id,_tmpBrand_id,_tmpDescription,_tmpImage_url,_tmpUnit,_tmpPurchase_price,_tmpSelling_price,_tmpMin_selling_price,_tmpCurrent_stock,_tmpMin_stock,_tmpMax_stock,_tmpIs_active,_tmpCreated_at,_tmpUpdated_at,_tmpDeleted_at);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
