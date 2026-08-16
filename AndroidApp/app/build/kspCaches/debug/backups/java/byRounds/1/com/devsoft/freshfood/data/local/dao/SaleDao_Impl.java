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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.SaleEntity;
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
public final class SaleDao_Impl implements SaleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaleEntity> __insertionAdapterOfSaleEntity;

  private final EntityDeletionOrUpdateAdapter<SaleEntity> __updateAdapterOfSaleEntity;

  public SaleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaleEntity = new EntityInsertionAdapter<SaleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sales` (`id`,`invoice_number`,`customer_id`,`user_id`,`total_amount`,`paid_amount`,`credit_amount`,`payment_method`,`status`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getInvoice_number() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getInvoice_number());
        }
        if (entity.getCustomer_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCustomer_id());
        }
        if (entity.getUser_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUser_id());
        }
        statement.bindDouble(5, entity.getTotal_amount());
        statement.bindDouble(6, entity.getPaid_amount());
        statement.bindDouble(7, entity.getCredit_amount());
        statement.bindString(8, entity.getPayment_method());
        statement.bindString(9, entity.getStatus());
        if (entity.getDeleted_at() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDeleted_at());
        }
      }
    };
    this.__updateAdapterOfSaleEntity = new EntityDeletionOrUpdateAdapter<SaleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sales` SET `id` = ?,`invoice_number` = ?,`customer_id` = ?,`user_id` = ?,`total_amount` = ?,`paid_amount` = ?,`credit_amount` = ?,`payment_method` = ?,`status` = ?,`deleted_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaleEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getInvoice_number() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getInvoice_number());
        }
        if (entity.getCustomer_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCustomer_id());
        }
        if (entity.getUser_id() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUser_id());
        }
        statement.bindDouble(5, entity.getTotal_amount());
        statement.bindDouble(6, entity.getPaid_amount());
        statement.bindDouble(7, entity.getCredit_amount());
        statement.bindString(8, entity.getPayment_method());
        statement.bindString(9, entity.getStatus());
        if (entity.getDeleted_at() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDeleted_at());
        }
        statement.bindString(11, entity.getId());
      }
    };
  }

  @Override
  public Object insertSale(final SaleEntity sale, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleEntity.insert(sale);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSales(final List<SaleEntity> sales,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaleEntity.insert(sales);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSale(final SaleEntity sale, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSaleEntity.handle(sale);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SaleEntity>> getAllSales() {
    final String _sql = "SELECT * FROM sales WHERE deleted_at IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sales"}, new Callable<List<SaleEntity>>() {
      @Override
      @NonNull
      public List<SaleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoice_number");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "total_amount");
          final int _cursorIndexOfPaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "paid_amount");
          final int _cursorIndexOfCreditAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "credit_amount");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<SaleEntity> _result = new ArrayList<SaleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaleEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpInvoice_number;
            if (_cursor.isNull(_cursorIndexOfInvoiceNumber)) {
              _tmpInvoice_number = null;
            } else {
              _tmpInvoice_number = _cursor.getString(_cursorIndexOfInvoiceNumber);
            }
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpUser_id;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUser_id = null;
            } else {
              _tmpUser_id = _cursor.getString(_cursorIndexOfUserId);
            }
            final double _tmpTotal_amount;
            _tmpTotal_amount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final double _tmpPaid_amount;
            _tmpPaid_amount = _cursor.getDouble(_cursorIndexOfPaidAmount);
            final double _tmpCredit_amount;
            _tmpCredit_amount = _cursor.getDouble(_cursorIndexOfCreditAmount);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _item = new SaleEntity(_tmpId,_tmpInvoice_number,_tmpCustomer_id,_tmpUser_id,_tmpTotal_amount,_tmpPaid_amount,_tmpCredit_amount,_tmpPayment_method,_tmpStatus,_tmpDeleted_at);
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
  public Object getSaleById(final String id, final Continuation<? super SaleEntity> $completion) {
    final String _sql = "SELECT * FROM sales WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SaleEntity>() {
      @Override
      @Nullable
      public SaleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoice_number");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTotalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "total_amount");
          final int _cursorIndexOfPaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "paid_amount");
          final int _cursorIndexOfCreditAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "credit_amount");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "payment_method");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final SaleEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpInvoice_number;
            if (_cursor.isNull(_cursorIndexOfInvoiceNumber)) {
              _tmpInvoice_number = null;
            } else {
              _tmpInvoice_number = _cursor.getString(_cursorIndexOfInvoiceNumber);
            }
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpUser_id;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUser_id = null;
            } else {
              _tmpUser_id = _cursor.getString(_cursorIndexOfUserId);
            }
            final double _tmpTotal_amount;
            _tmpTotal_amount = _cursor.getDouble(_cursorIndexOfTotalAmount);
            final double _tmpPaid_amount;
            _tmpPaid_amount = _cursor.getDouble(_cursorIndexOfPaidAmount);
            final double _tmpCredit_amount;
            _tmpCredit_amount = _cursor.getDouble(_cursorIndexOfCreditAmount);
            final String _tmpPayment_method;
            _tmpPayment_method = _cursor.getString(_cursorIndexOfPaymentMethod);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _result = new SaleEntity(_tmpId,_tmpInvoice_number,_tmpCustomer_id,_tmpUser_id,_tmpTotal_amount,_tmpPaid_amount,_tmpCredit_amount,_tmpPayment_method,_tmpStatus,_tmpDeleted_at);
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
