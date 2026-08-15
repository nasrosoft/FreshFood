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
import com.devsoft.freshfood.data.local.entity.CreditTransactionEntity;
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
public final class CreditTransactionDao_Impl implements CreditTransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CreditTransactionEntity> __insertionAdapterOfCreditTransactionEntity;

  public CreditTransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCreditTransactionEntity = new EntityInsertionAdapter<CreditTransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `credit_transactions` (`id`,`customer_id`,`amount`,`transaction_type`,`reference_id`,`user_id`,`created_at`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CreditTransactionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCustomer_id());
        statement.bindDouble(3, entity.getAmount());
        statement.bindString(4, entity.getTransaction_type());
        if (entity.getReference_id() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getReference_id());
        }
        statement.bindString(6, entity.getUser_id());
        if (entity.getCreated_at() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCreated_at());
        }
        if (entity.getDeleted_at() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDeleted_at());
        }
      }
    };
  }

  @Override
  public Object insertCreditTransaction(final CreditTransactionEntity creditTransaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCreditTransactionEntity.insert(creditTransaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCreditTransactions(final List<CreditTransactionEntity> creditTransactions,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCreditTransactionEntity.insert(creditTransactions);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CreditTransactionEntity>> getTransactionsForCustomer(final String customerId) {
    final String _sql = "SELECT * FROM credit_transactions WHERE customer_id = ? AND deleted_at IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"credit_transactions"}, new Callable<List<CreditTransactionEntity>>() {
      @Override
      @NonNull
      public List<CreditTransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfTransactionType = CursorUtil.getColumnIndexOrThrow(_cursor, "transaction_type");
          final int _cursorIndexOfReferenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "reference_id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CreditTransactionEntity> _result = new ArrayList<CreditTransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CreditTransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomer_id;
            _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpTransaction_type;
            _tmpTransaction_type = _cursor.getString(_cursorIndexOfTransactionType);
            final String _tmpReference_id;
            if (_cursor.isNull(_cursorIndexOfReferenceId)) {
              _tmpReference_id = null;
            } else {
              _tmpReference_id = _cursor.getString(_cursorIndexOfReferenceId);
            }
            final String _tmpUser_id;
            _tmpUser_id = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpCreated_at;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreated_at = null;
            } else {
              _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _item = new CreditTransactionEntity(_tmpId,_tmpCustomer_id,_tmpAmount,_tmpTransaction_type,_tmpReference_id,_tmpUser_id,_tmpCreated_at,_tmpDeleted_at);
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
