package com.devsoft.freshfood.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.StockBatchEntity;
import com.devsoft.freshfood.data.local.entity.StockMovementEntity;
import java.lang.Class;
import java.lang.Exception;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StockDao_Impl implements StockDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockBatchEntity> __insertionAdapterOfStockBatchEntity;

  private final EntityInsertionAdapter<StockMovementEntity> __insertionAdapterOfStockMovementEntity;

  public StockDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockBatchEntity = new EntityInsertionAdapter<StockBatchEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stock_batches` (`id`,`product_id`,`batch_number`,`expiration_date`,`quantity`,`purchase_price`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockBatchEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getProduct_id());
        if (entity.getBatch_number() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBatch_number());
        }
        if (entity.getExpiration_date() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getExpiration_date());
        }
        statement.bindLong(5, entity.getQuantity());
        statement.bindDouble(6, entity.getPurchase_price());
        if (entity.getCreated_at() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreated_at());
        }
        if (entity.getUpdated_at() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getUpdated_at());
        }
      }
    };
    this.__insertionAdapterOfStockMovementEntity = new EntityInsertionAdapter<StockMovementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stock_movements` (`id`,`product_id`,`batch_id`,`movement_type`,`quantity`,`reference_id`,`user_id`,`notes`,`created_at`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockMovementEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getProduct_id());
        if (entity.getBatch_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBatch_id());
        }
        statement.bindString(4, entity.getMovement_type());
        statement.bindLong(5, entity.getQuantity());
        if (entity.getReference_id() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getReference_id());
        }
        statement.bindString(7, entity.getUser_id());
        if (entity.getNotes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotes());
        }
        if (entity.getCreated_at() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCreated_at());
        }
      }
    };
  }

  @Override
  public Object insertStockBatch(final StockBatchEntity batch,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockBatchEntity.insert(batch);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStockBatches(final List<StockBatchEntity> batches,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockBatchEntity.insert(batches);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStockMovement(final StockMovementEntity movement,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockMovementEntity.insert(movement);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStockMovements(final List<StockMovementEntity> movements,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockMovementEntity.insert(movements);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAvailableBatchesForProduct(final String productId,
      final Continuation<? super List<StockBatchEntity>> $completion) {
    final String _sql = "SELECT * FROM stock_batches WHERE product_id = ? AND quantity > 0 ORDER BY expiration_date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, productId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockBatchEntity>>() {
      @Override
      @NonNull
      public List<StockBatchEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfBatchNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "batch_number");
          final int _cursorIndexOfExpirationDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expiration_date");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfPurchasePrice = CursorUtil.getColumnIndexOrThrow(_cursor, "purchase_price");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<StockBatchEntity> _result = new ArrayList<StockBatchEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockBatchEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpProduct_id;
            _tmpProduct_id = _cursor.getString(_cursorIndexOfProductId);
            final String _tmpBatch_number;
            if (_cursor.isNull(_cursorIndexOfBatchNumber)) {
              _tmpBatch_number = null;
            } else {
              _tmpBatch_number = _cursor.getString(_cursorIndexOfBatchNumber);
            }
            final String _tmpExpiration_date;
            if (_cursor.isNull(_cursorIndexOfExpirationDate)) {
              _tmpExpiration_date = null;
            } else {
              _tmpExpiration_date = _cursor.getString(_cursorIndexOfExpirationDate);
            }
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            final double _tmpPurchase_price;
            _tmpPurchase_price = _cursor.getDouble(_cursorIndexOfPurchasePrice);
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
            _item = new StockBatchEntity(_tmpId,_tmpProduct_id,_tmpBatch_number,_tmpExpiration_date,_tmpQuantity,_tmpPurchase_price,_tmpCreated_at,_tmpUpdated_at);
            _result.add(_item);
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
