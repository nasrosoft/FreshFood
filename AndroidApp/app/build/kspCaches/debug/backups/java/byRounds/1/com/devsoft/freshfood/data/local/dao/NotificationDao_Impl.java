package com.devsoft.freshfood.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devsoft.freshfood.data.local.entity.NotificationEntity;
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
public final class NotificationDao_Impl implements NotificationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NotificationEntity> __insertionAdapterOfNotificationEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public NotificationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNotificationEntity = new EntityInsertionAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `notifications` (`id`,`user_id`,`title`,`message`,`is_read`,`created_at`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUser_id());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getMessage());
        final int _tmp = entity.is_read() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getCreated_at());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications";
        return _query;
      }
    };
  }

  @Override
  public Object insertNotifications(final List<NotificationEntity> notifications,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNotificationEntity.insert(notifications);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<NotificationEntity>> getAllNotifications() {
    final String _sql = "SELECT * FROM notifications ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notifications"}, new Callable<List<NotificationEntity>>() {
      @Override
      @NonNull
      public List<NotificationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NotificationEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUser_id;
            _tmpUser_id = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final boolean _tmpIs_read;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIs_read = _tmp != 0;
            final String _tmpCreated_at;
            _tmpCreated_at = _cursor.getString(_cursorIndexOfCreatedAt);
            _item = new NotificationEntity(_tmpId,_tmpUser_id,_tmpTitle,_tmpMessage,_tmpIs_read,_tmpCreated_at);
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
