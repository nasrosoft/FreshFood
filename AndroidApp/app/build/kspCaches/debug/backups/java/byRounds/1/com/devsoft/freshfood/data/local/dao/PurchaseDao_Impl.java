package com.devsoft.freshfood.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.PurchaseEntity;
import com.devsoft.freshfood.data.local.entity.PurchaseItemEntity;
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
public final class PurchaseDao_Impl implements PurchaseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PurchaseEntity> __insertionAdapterOfPurchaseEntity;

  private final EntityInsertionAdapter<PurchaseItemEntity> __insertionAdapterOfPurchaseItemEntity;

  public PurchaseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPurchaseEntity = new EntityInsertionAdapter<PurchaseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `purchases` (`id`,`supplier_id`,`invoice_number`,`total_amount`,`status`,`created_at`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchaseEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getSupplier_id() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getSupplier_id());
        }
        if (entity.getInvoice_number() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getInvoice_number());
        }
        statement.bindDouble(4, entity.getTotal_amount());
        statement.bindString(5, entity.getStatus());
        if (entity.getCreated_at() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCreated_at());
        }
      }
    };
    this.__insertionAdapterOfPurchaseItemEntity = new EntityInsertionAdapter<PurchaseItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `purchase_items` (`id`,`purchase_id`,`product_id`,`batch_id`,`quantity`,`purchase_price`,`expiration_date`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchaseItemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPurchase_id());
        statement.bindString(3, entity.getProduct_id());
        if (entity.getBatch_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getBatch_id());
        }
        statement.bindLong(5, entity.getQuantity());
        statement.bindDouble(6, entity.getPurchase_price());
        statement.bindString(7, entity.getExpiration_date());
      }
    };
  }

  @Override
  public Object insertPurchase(final PurchaseEntity purchase,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPurchaseEntity.insert(purchase);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPurchaseItems(final List<PurchaseItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPurchaseItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PurchaseEntity>> getAllPurchases() {
    final String _sql = "SELECT * FROM purchases ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"purchases"}, new Callable<List<PurchaseEntity>>() {
      @Override
      @NonNull
      public List<PurchaseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSupplierId = CursorUtil.getColumnIndexOrThrow(_cursor, "supplier_id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoice_number");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "total_amount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<PurchaseEntity> _result = new ArrayList<PurchaseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PurchaseEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSupplier_id;
            if (_cursor.isNull(_cursorIndexOfSupplierId)) {
              _tmpSupplier_id = null;
            } else {
              _tmpSupplier_id = _cursor.getString(_cursorIndexOfSupplierId);
            }
            final String _tmpInvoice_number;
            if (_cursor.isNull(_cursorIndexOfInvoiceNumber)) {
              _tmpInvoice_number = null;
            } else {
              _tmpInvoice_number = _cursor.getString(_cursorIndexOfInvoiceNumber);
            }
            final double _tmpTotal_amount;
            _tmpTotal_amount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            _item = new PurchaseEntity(_tmpId,_tmpSupplier_id,_tmpInvoice_number,_tmpTotal_amount,_tmpStatus,_tmpCreated_at);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
