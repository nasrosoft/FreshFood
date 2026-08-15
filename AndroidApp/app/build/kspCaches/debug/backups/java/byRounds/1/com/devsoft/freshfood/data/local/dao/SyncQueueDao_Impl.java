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
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class SyncQueueDao_Impl implements SyncQueueDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SyncQueueEntity> __insertionAdapterOfSyncQueueEntity;

  private final EntityDeletionOrUpdateAdapter<SyncQueueEntity> __updateAdapterOfSyncQueueEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSyncedOperations;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatusAndError;

  public SyncQueueDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSyncQueueEntity = new EntityInsertionAdapter<SyncQueueEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sync_queue` (`id`,`entity_type`,`entity_id`,`operation`,`payload`,`created_at`,`updated_at`,`retry_count`,`last_error`,`status`,`device_id`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SyncQueueEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEntity_type());
        statement.bindString(3, entity.getEntity_id());
        statement.bindString(4, entity.getOperation());
        if (entity.getPayload() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPayload());
        }
        statement.bindLong(6, entity.getCreated_at());
        statement.bindLong(7, entity.getUpdated_at());
        statement.bindLong(8, entity.getRetry_count());
        if (entity.getLast_error() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getLast_error());
        }
        statement.bindString(10, entity.getStatus());
        statement.bindString(11, entity.getDevice_id());
      }
    };
    this.__updateAdapterOfSyncQueueEntity = new EntityDeletionOrUpdateAdapter<SyncQueueEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sync_queue` SET `id` = ?,`entity_type` = ?,`entity_id` = ?,`operation` = ?,`payload` = ?,`created_at` = ?,`updated_at` = ?,`retry_count` = ?,`last_error` = ?,`status` = ?,`device_id` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SyncQueueEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEntity_type());
        statement.bindString(3, entity.getEntity_id());
        statement.bindString(4, entity.getOperation());
        if (entity.getPayload() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPayload());
        }
        statement.bindLong(6, entity.getCreated_at());
        statement.bindLong(7, entity.getUpdated_at());
        statement.bindLong(8, entity.getRetry_count());
        if (entity.getLast_error() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getLast_error());
        }
        statement.bindString(10, entity.getStatus());
        statement.bindString(11, entity.getDevice_id());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSyncedOperations = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sync_queue WHERE status = 'SYNCED'";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sync_queue SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStatusAndError = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sync_queue SET status = ?, last_error = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SyncQueueEntity entity, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSyncQueueEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final SyncQueueEntity entity, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSyncQueueEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSyncedOperations(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSyncedOperations.acquire();
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
          __preparedStmtOfDeleteSyncedOperations.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final int id, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatusAndError(final int id, final String status, final String error,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatusAndError.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, error);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateStatusAndError.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingOperations(final String status,
      final Continuation<? super List<SyncQueueEntity>> $completion) {
    final String _sql = "SELECT * FROM sync_queue WHERE status = ? ORDER BY created_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, status);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncQueueEntity>>() {
      @Override
      @NonNull
      public List<SyncQueueEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEntityType = CursorUtil.getColumnIndexOrThrow(_cursor, "entity_type");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entity_id");
          final int _cursorIndexOfOperation = CursorUtil.getColumnIndexOrThrow(_cursor, "operation");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfLastError = CursorUtil.getColumnIndexOrThrow(_cursor, "last_error");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final List<SyncQueueEntity> _result = new ArrayList<SyncQueueEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SyncQueueEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpEntity_type;
            _tmpEntity_type = _cursor.getString(_cursorIndexOfEntityType);
            final String _tmpEntity_id;
            _tmpEntity_id = _cursor.getString(_cursorIndexOfEntityId);
            final String _tmpOperation;
            _tmpOperation = _cursor.getString(_cursorIndexOfOperation);
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final long _tmpCreated_at;
            _tmpCreated_at = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdated_at;
            _tmpUpdated_at = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpRetry_count;
            _tmpRetry_count = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpLast_error;
            if (_cursor.isNull(_cursorIndexOfLastError)) {
              _tmpLast_error = null;
            } else {
              _tmpLast_error = _cursor.getString(_cursorIndexOfLastError);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpDevice_id;
            _tmpDevice_id = _cursor.getString(_cursorIndexOfDeviceId);
            _item = new SyncQueueEntity(_tmpId,_tmpEntity_type,_tmpEntity_id,_tmpOperation,_tmpPayload,_tmpCreated_at,_tmpUpdated_at,_tmpRetry_count,_tmpLast_error,_tmpStatus,_tmpDevice_id);
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

  @Override
  public Object getPendingOperationForEntity(final String entityId, final String status,
      final Continuation<? super SyncQueueEntity> $completion) {
    final String _sql = "SELECT * FROM sync_queue WHERE entity_id = ? AND status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, entityId);
    _argIndex = 2;
    _statement.bindString(_argIndex, status);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SyncQueueEntity>() {
      @Override
      @Nullable
      public SyncQueueEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEntityType = CursorUtil.getColumnIndexOrThrow(_cursor, "entity_type");
          final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(_cursor, "entity_id");
          final int _cursorIndexOfOperation = CursorUtil.getColumnIndexOrThrow(_cursor, "operation");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retry_count");
          final int _cursorIndexOfLastError = CursorUtil.getColumnIndexOrThrow(_cursor, "last_error");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final SyncQueueEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpEntity_type;
            _tmpEntity_type = _cursor.getString(_cursorIndexOfEntityType);
            final String _tmpEntity_id;
            _tmpEntity_id = _cursor.getString(_cursorIndexOfEntityId);
            final String _tmpOperation;
            _tmpOperation = _cursor.getString(_cursorIndexOfOperation);
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final long _tmpCreated_at;
            _tmpCreated_at = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdated_at;
            _tmpUpdated_at = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpRetry_count;
            _tmpRetry_count = _cursor.getInt(_cursorIndexOfRetryCount);
            final String _tmpLast_error;
            if (_cursor.isNull(_cursorIndexOfLastError)) {
              _tmpLast_error = null;
            } else {
              _tmpLast_error = _cursor.getString(_cursorIndexOfLastError);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpDevice_id;
            _tmpDevice_id = _cursor.getString(_cursorIndexOfDeviceId);
            _result = new SyncQueueEntity(_tmpId,_tmpEntity_type,_tmpEntity_id,_tmpOperation,_tmpPayload,_tmpCreated_at,_tmpUpdated_at,_tmpRetry_count,_tmpLast_error,_tmpStatus,_tmpDevice_id);
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
