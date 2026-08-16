package com.devsoft.freshfood.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.SaleItemEntity;
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
public final class SaleItemDao_Impl implements SaleItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaleItemEntity> __insertionAdapterOfSaleItemEntity;

  private final EntityDeletionOrUpdateAdapter<SaleItemEntity> __updateAdapterOfSaleItemEntity;

  public SaleItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaleItemEntity = new EntityInsertionAdapter<SaleItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sale_items` (`id`,`sale_id`,`product_id`,`quantity`,`unit_price`,`subtotal`,`created_at`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleItemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSale_id());
        statement.bindString(3, entity.getProduct_id());
        statement.bindLong(4, entity.getQuantity());
        statement.bindDouble(5, entity.getUnit_price());
        statement.bindDouble(6, entity.getSubtotal());
        if (entity.getCreated_at() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreated_at());
        }
      }
    };
    this.__updateAdapterOfSaleItemEntity = new EntityDeletionOrUpdateAdapter<SaleItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sale_items` SET `id` = ?,`sale_id` = ?,`product_id` = ?,`quantity` = ?,`unit_price` = ?,`subtotal` = ?,`created_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleItemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSale_id());
        statement.bindString(3, entity.getProduct_id());
        statement.bindLong(4, entity.getQuantity());
        statement.bindDouble(5, entity.getUnit_price());
        statement.bindDouble(6, entity.getSubtotal());
        if (entity.getCreated_at() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreated_at());
        }
        statement.bindString(8, entity.getId());
      }
    };
  }

  @Override
  public Object insertSaleItem(final SaleItemEntity saleItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleItemEntity.insert(saleItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSaleItems(final List<SaleItemEntity> saleItems,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleItemEntity.insert(saleItems);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSaleItem(final SaleItemEntity saleItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSaleItemEntity.handle(saleItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getItemsForSale(final String saleId,
      final Continuation<? super List<SaleItemEntity>> $completion) {
    final String _sql = "SELECT * FROM sale_items WHERE sale_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, saleId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaleItemEntity>>() {
      @Override
      @NonNull
      public List<SaleItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "sale_id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnitPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unit_price");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<SaleItemEntity> _result = new ArrayList<SaleItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleItemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSale_id;
            _tmpSale_id = _cursor.getString(_cursorIndexOfSaleId);
            final String _tmpProduct_id;
            _tmpProduct_id = _cursor.getString(_cursorIndexOfProductId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            final double _tmpUnit_price;
            _tmpUnit_price = _cursor.getDouble(_cursorIndexOfUnitPrice);
            final double _tmpSubtotal;
            _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new SaleItemEntity(_tmpId,_tmpSale_id,_tmpProduct_id,_tmpQuantity,_tmpUnit_price,_tmpSubtotal,_tmpCreated_at);
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
