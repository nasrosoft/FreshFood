package com.devsoft.freshfood.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.ReturnEntity;
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
public final class ReturnDao_Impl implements ReturnDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReturnEntity> __insertionAdapterOfReturnEntity;

  private final EntityDeletionOrUpdateAdapter<ReturnEntity> __updateAdapterOfReturnEntity;

  public ReturnDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReturnEntity = new EntityInsertionAdapter<ReturnEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `returns` (`id`,`date`,`customer_id`,`product_id`,`quantity`,`reason`,`status`,`created_by`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReturnEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getDate() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDate());
        }
        if (entity.getCustomer_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCustomer_id());
        }
        statement.bindString(4, entity.getProduct_id());
        statement.bindLong(5, entity.getQuantity());
        if (entity.getReason() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getReason());
        }
        statement.bindString(7, entity.getStatus());
        if (entity.getCreated_by() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCreated_by());
        }
      }
    };
    this.__updateAdapterOfReturnEntity = new EntityDeletionOrUpdateAdapter<ReturnEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `returns` SET `id` = ?,`date` = ?,`customer_id` = ?,`product_id` = ?,`quantity` = ?,`reason` = ?,`status` = ?,`created_by` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReturnEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getDate() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDate());
        }
        if (entity.getCustomer_id() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCustomer_id());
        }
        statement.bindString(4, entity.getProduct_id());
        statement.bindLong(5, entity.getQuantity());
        if (entity.getReason() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getReason());
        }
        statement.bindString(7, entity.getStatus());
        if (entity.getCreated_by() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCreated_by());
        }
        statement.bindString(9, entity.getId());
      }
    };
  }

  @Override
  public Object insertReturn(final ReturnEntity returnOrder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReturnEntity.insert(returnOrder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateReturn(final ReturnEntity returnOrder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReturnEntity.handle(returnOrder);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReturnEntity>> getAllReturns() {
    final String _sql = "SELECT * FROM returns ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"returns"}, new Callable<List<ReturnEntity>>() {
      @Override
      @NonNull
      public List<ReturnEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "created_by");
          final List<ReturnEntity> _result = new ArrayList<ReturnEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReturnEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpCustomer_id;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomer_id = null;
            } else {
              _tmpCustomer_id = _cursor.getString(_cursorIndexOfCustomerId);
            }
            final String _tmpProduct_id;
            _tmpProduct_id = _cursor.getString(_cursorIndexOfProductId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpCreated_by;
            if (_cursor.isNull(_cursorIndexOfCreatedBy)) {
              _tmpCreated_by = null;
            } else {
              _tmpCreated_by = _cursor.getString(_cursorIndexOfCreatedBy);
            }
            _item = new ReturnEntity(_tmpId,_tmpDate,_tmpCustomer_id,_tmpProduct_id,_tmpQuantity,_tmpReason,_tmpStatus,_tmpCreated_by);
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
