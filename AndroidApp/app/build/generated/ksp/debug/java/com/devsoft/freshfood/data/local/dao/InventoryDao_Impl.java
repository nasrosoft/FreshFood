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
import com.devsoft.freshfood.data.local.entity.InventoryItemEntity;
import com.devsoft.freshfood.data.local.entity.InventorySessionEntity;
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
public final class InventoryDao_Impl implements InventoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<InventorySessionEntity> __insertionAdapterOfInventorySessionEntity;

  private final EntityInsertionAdapter<InventoryItemEntity> __insertionAdapterOfInventoryItemEntity;

  private final EntityDeletionOrUpdateAdapter<InventorySessionEntity> __updateAdapterOfInventorySessionEntity;

  public InventoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInventorySessionEntity = new EntityInsertionAdapter<InventorySessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `inventory_sessions` (`id`,`date`,`status`,`conducted_by`,`notes`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InventorySessionEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getDate() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDate());
        }
        statement.bindString(3, entity.getStatus());
        if (entity.getConducted_by() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getConducted_by());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNotes());
        }
      }
    };
    this.__insertionAdapterOfInventoryItemEntity = new EntityInsertionAdapter<InventoryItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `inventory_items` (`id`,`session_id`,`product_id`,`expected_quantity`,`actual_quantity`,`difference`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InventoryItemEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSession_id());
        statement.bindString(3, entity.getProduct_id());
        statement.bindLong(4, entity.getExpected_quantity());
        statement.bindLong(5, entity.getActual_quantity());
        if (entity.getDifference() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDifference());
        }
      }
    };
    this.__updateAdapterOfInventorySessionEntity = new EntityDeletionOrUpdateAdapter<InventorySessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `inventory_sessions` SET `id` = ?,`date` = ?,`status` = ?,`conducted_by` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InventorySessionEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getDate() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDate());
        }
        statement.bindString(3, entity.getStatus());
        if (entity.getConducted_by() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getConducted_by());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNotes());
        }
        statement.bindString(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertSession(final InventorySessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInventorySessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertItems(final List<InventoryItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInventoryItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final InventorySessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfInventorySessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<InventorySessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM inventory_sessions ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"inventory_sessions"}, new Callable<List<InventorySessionEntity>>() {
      @Override
      @NonNull
      public List<InventorySessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfConductedBy = CursorUtil.getColumnIndexOrThrow(_cursor, "conducted_by");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<InventorySessionEntity> _result = new ArrayList<InventorySessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventorySessionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpConducted_by;
            if (_cursor.isNull(_cursorIndexOfConductedBy)) {
              _tmpConducted_by = null;
            } else {
              _tmpConducted_by = _cursor.getString(_cursorIndexOfConductedBy);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new InventorySessionEntity(_tmpId,_tmpDate,_tmpStatus,_tmpConducted_by,_tmpNotes);
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
  public Flow<List<InventoryItemEntity>> getItemsForSession(final String sessionId) {
    final String _sql = "SELECT * FROM inventory_items WHERE session_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"inventory_items"}, new Callable<List<InventoryItemEntity>>() {
      @Override
      @NonNull
      public List<InventoryItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfExpectedQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "expected_quantity");
          final int _cursorIndexOfActualQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "actual_quantity");
          final int _cursorIndexOfDifference = CursorUtil.getColumnIndexOrThrow(_cursor, "difference");
          final List<InventoryItemEntity> _result = new ArrayList<InventoryItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InventoryItemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSession_id;
            _tmpSession_id = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpProduct_id;
            _tmpProduct_id = _cursor.getString(_cursorIndexOfProductId);
            final int _tmpExpected_quantity;
            _tmpExpected_quantity = _cursor.getInt(_cursorIndexOfExpectedQuantity);
            final int _tmpActual_quantity;
            _tmpActual_quantity = _cursor.getInt(_cursorIndexOfActualQuantity);
            final Integer _tmpDifference;
            if (_cursor.isNull(_cursorIndexOfDifference)) {
              _tmpDifference = null;
            } else {
              _tmpDifference = _cursor.getInt(_cursorIndexOfDifference);
            }
            _item = new InventoryItemEntity(_tmpId,_tmpSession_id,_tmpProduct_id,_tmpExpected_quantity,_tmpActual_quantity,_tmpDifference);
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
