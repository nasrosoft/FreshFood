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
import com.devsoft.freshfood.data.local.entity.DeliveryItemEntity;
import com.devsoft.freshfood.data.local.entity.DeliveryOrderEntity;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DeliveryDao_Impl implements DeliveryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DeliveryOrderEntity> __insertionAdapterOfDeliveryOrderEntity;

  private final EntityInsertionAdapter<DeliveryItemEntity> __insertionAdapterOfDeliveryItemEntity;

  private final EntityDeletionOrUpdateAdapter<DeliveryOrderEntity> __updateAdapterOfDeliveryOrderEntity;

  private final EntityDeletionOrUpdateAdapter<DeliveryItemEntity> __updateAdapterOfDeliveryItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteItemsForDeliveryOrder;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDeliveryItemById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDeliveryOrderById;

  public DeliveryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeliveryOrderEntity = new EntityInsertionAdapter<DeliveryOrderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `delivery_orders` (`id`,`customer_id`,`sale_id`,`delivery_employee_id`,`status`,`notes`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeliveryOrderEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getCustomer_id() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getCustomer_id());
        }
        if (entity.getSale_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSale_id());
        }
        if (entity.getDelivery_employee_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDelivery_employee_id());
        }
        statement.bindString(5, entity.getStatus());
        if (entity.getNotes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getNotes());
        }
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
    this.__insertionAdapterOfDeliveryItemEntity = new EntityInsertionAdapter<DeliveryItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `delivery_items` (`id`,`delivery_order_id`,`product_id`,`quantity`,`created_at`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeliveryItemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDelivery_order_id());
        statement.bindString(3, entity.getProduct_id());
        statement.bindLong(4, entity.getQuantity());
        if (entity.getCreated_at() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCreated_at());
        }
      }
    };
    this.__updateAdapterOfDeliveryOrderEntity = new EntityDeletionOrUpdateAdapter<DeliveryOrderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `delivery_orders` SET `id` = ?,`customer_id` = ?,`sale_id` = ?,`delivery_employee_id` = ?,`status` = ?,`notes` = ?,`created_at` = ?,`updated_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeliveryOrderEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getCustomer_id() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getCustomer_id());
        }
        if (entity.getSale_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSale_id());
        }
        if (entity.getDelivery_employee_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDelivery_employee_id());
        }
        statement.bindString(5, entity.getStatus());
        if (entity.getNotes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getNotes());
        }
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
        statement.bindString(9, entity.getId());
      }
    };
    this.__updateAdapterOfDeliveryItemEntity = new EntityDeletionOrUpdateAdapter<DeliveryItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `delivery_items` SET `id` = ?,`delivery_order_id` = ?,`product_id` = ?,`quantity` = ?,`created_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeliveryItemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDelivery_order_id());
        statement.bindString(3, entity.getProduct_id());
        statement.bindLong(4, entity.getQuantity());
        if (entity.getCreated_at() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCreated_at());
        }
        statement.bindString(6, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteItemsForDeliveryOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM delivery_items WHERE delivery_order_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDeliveryItemById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM delivery_items WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDeliveryOrderById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM delivery_orders WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDeliveryOrder(final DeliveryOrderEntity order,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDeliveryOrderEntity.insert(order);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertDeliveryItems(final List<DeliveryItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDeliveryItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDeliveryOrder(final DeliveryOrderEntity order,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDeliveryOrderEntity.handle(order);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDeliveryItem(final DeliveryItemEntity item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDeliveryItemEntity.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteItemsForDeliveryOrder(final String orderId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteItemsForDeliveryOrder.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, orderId);
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
          __preparedStmtOfDeleteItemsForDeliveryOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDeliveryItemById(final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDeliveryItemById.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfDeleteDeliveryItemById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDeliveryOrderById(final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDeliveryOrderById.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfDeleteDeliveryOrderById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DeliveryOrderEntity>> getAllDeliveryOrders() {
    final String _sql = "SELECT * FROM delivery_orders ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"delivery_orders"}, new Callable<List<DeliveryOrderEntity>>() {
      @Override
      @NonNull
      public List<DeliveryOrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "sale_id");
          final int _cursorIndexOfDeliveryEmployeeId = CursorUtil.getColumnIndexOrThrow(_cursor, "delivery_employee_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<DeliveryOrderEntity> _result = new ArrayList<DeliveryOrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeliveryOrderEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpSale_id;
            if (_cursor.isNull(_cursorIndexOfSaleId)) {
              _tmpSale_id = null;
            } else {
              _tmpSale_id = _cursor.getString(_cursorIndexOfSaleId);
            }
            final String _tmpDelivery_employee_id;
            if (_cursor.isNull(_cursorIndexOfDeliveryEmployeeId)) {
              _tmpDelivery_employee_id = null;
            } else {
              _tmpDelivery_employee_id = _cursor.getString(_cursorIndexOfDeliveryEmployeeId);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
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
            _item = new DeliveryOrderEntity(_tmpId,_tmpCustomer_id,_tmpSale_id,_tmpDelivery_employee_id,_tmpStatus,_tmpNotes,_tmpCreated_at,_tmpUpdated_at);
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
  public Object getDeliveryOrderById(final String id,
      final Continuation<? super DeliveryOrderEntity> $completion) {
    final String _sql = "SELECT * FROM delivery_orders WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DeliveryOrderEntity>() {
      @Override
      @Nullable
      public DeliveryOrderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfSaleId = CursorUtil.getColumnIndexOrThrow(_cursor, "sale_id");
          final int _cursorIndexOfDeliveryEmployeeId = CursorUtil.getColumnIndexOrThrow(_cursor, "delivery_employee_id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final DeliveryOrderEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpSale_id;
            if (_cursor.isNull(_cursorIndexOfSaleId)) {
              _tmpSale_id = null;
            } else {
              _tmpSale_id = _cursor.getString(_cursorIndexOfSaleId);
            }
            final String _tmpDelivery_employee_id;
            if (_cursor.isNull(_cursorIndexOfDeliveryEmployeeId)) {
              _tmpDelivery_employee_id = null;
            } else {
              _tmpDelivery_employee_id = _cursor.getString(_cursorIndexOfDeliveryEmployeeId);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
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
            _result = new DeliveryOrderEntity(_tmpId,_tmpCustomer_id,_tmpSale_id,_tmpDelivery_employee_id,_tmpStatus,_tmpNotes,_tmpCreated_at,_tmpUpdated_at);
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
  public Object getItemsForDeliveryOrder(final String orderId,
      final Continuation<? super List<DeliveryItemEntity>> $completion) {
    final String _sql = "SELECT * FROM delivery_items WHERE delivery_order_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, orderId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DeliveryItemEntity>>() {
      @Override
      @NonNull
      public List<DeliveryItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeliveryOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "delivery_order_id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<DeliveryItemEntity> _result = new ArrayList<DeliveryItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeliveryItemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDelivery_order_id;
            _tmpDelivery_order_id = _cursor.getString(_cursorIndexOfDeliveryOrderId);
            final String _tmpProduct_id;
            _tmpProduct_id = _cursor.getString(_cursorIndexOfProductId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new DeliveryItemEntity(_tmpId,_tmpDelivery_order_id,_tmpProduct_id,_tmpQuantity,_tmpCreated_at);
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
