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
import com.devsoft.freshfood.data.local.entity.CustomerEntity;
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
public final class CustomerDao_Impl implements CustomerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CustomerEntity> __insertionAdapterOfCustomerEntity;

  private final EntityDeletionOrUpdateAdapter<CustomerEntity> __updateAdapterOfCustomerEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteCustomer;

  public CustomerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCustomerEntity = new EntityInsertionAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `customers` (`id`,`name`,`phone`,`address`,`wilaya`,`commune`,`photo_url`,`credit_limit`,`current_credit`,`customer_type`,`is_active`,`notes`,`created_at`,`updated_at`,`deleted_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getPhone() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPhone());
        }
        if (entity.getAddress() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAddress());
        }
        if (entity.getWilaya() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getWilaya());
        }
        if (entity.getCommune() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCommune());
        }
        if (entity.getPhoto_url() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPhoto_url());
        }
        statement.bindDouble(8, entity.getCredit_limit());
        statement.bindDouble(9, entity.getCurrent_credit());
        if (entity.getCustomer_type() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getCustomer_type());
        }
        final int _tmp = entity.is_active() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getNotes() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getNotes());
        }
        if (entity.getCreated_at() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCreated_at());
        }
        if (entity.getUpdated_at() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getUpdated_at());
        }
        if (entity.getDeleted_at() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDeleted_at());
        }
      }
    };
    this.__updateAdapterOfCustomerEntity = new EntityDeletionOrUpdateAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `customers` SET `id` = ?,`name` = ?,`phone` = ?,`address` = ?,`wilaya` = ?,`commune` = ?,`photo_url` = ?,`credit_limit` = ?,`current_credit` = ?,`customer_type` = ?,`is_active` = ?,`notes` = ?,`created_at` = ?,`updated_at` = ?,`deleted_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getPhone() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPhone());
        }
        if (entity.getAddress() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAddress());
        }
        if (entity.getWilaya() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getWilaya());
        }
        if (entity.getCommune() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCommune());
        }
        if (entity.getPhoto_url() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPhoto_url());
        }
        statement.bindDouble(8, entity.getCredit_limit());
        statement.bindDouble(9, entity.getCurrent_credit());
        if (entity.getCustomer_type() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getCustomer_type());
        }
        final int _tmp = entity.is_active() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getNotes() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getNotes());
        }
        if (entity.getCreated_at() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCreated_at());
        }
        if (entity.getUpdated_at() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getUpdated_at());
        }
        if (entity.getDeleted_at() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDeleted_at());
        }
        statement.bindString(16, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDeleteCustomer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE customers SET deleted_at = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCustomer(final CustomerEntity customer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCustomerEntity.insert(customer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCustomers(final List<CustomerEntity> customers,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCustomerEntity.insert(customers);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCustomer(final CustomerEntity customer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCustomerEntity.handle(customer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteCustomer(final String id, final String deletedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteCustomer.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, deletedAt);
        _argIndex = 2;
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
          __preparedStmtOfSoftDeleteCustomer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CustomerEntity>> getAllCustomers() {
    final String _sql = "SELECT * FROM customers WHERE deleted_at IS NULL ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"customers"}, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfWilaya = CursorUtil.getColumnIndexOrThrow(_cursor, "wilaya");
          final int _cursorIndexOfCommune = CursorUtil.getColumnIndexOrThrow(_cursor, "commune");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photo_url");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "credit_limit");
          final int _cursorIndexOfCurrentCredit = CursorUtil.getColumnIndexOrThrow(_cursor, "current_credit");
          final int _cursorIndexOfCustomerType = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpWilaya;
            if (_cursor.isNull(_cursorIndexOfWilaya)) {
              _tmpWilaya = null;
            } else {
              _tmpWilaya = _cursor.getString(_cursorIndexOfWilaya);
            }
            final String _tmpCommune;
            if (_cursor.isNull(_cursorIndexOfCommune)) {
              _tmpCommune = null;
            } else {
              _tmpCommune = _cursor.getString(_cursorIndexOfCommune);
            }
            final String _tmpPhoto_url;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhoto_url = null;
            } else {
              _tmpPhoto_url = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final double _tmpCredit_limit;
            _tmpCredit_limit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            final double _tmpCurrent_credit;
            _tmpCurrent_credit = _cursor.getDouble(_cursorIndexOfCurrentCredit);
            final String _tmpCustomer_type;
            if (_cursor.isNull(_cursorIndexOfCustomerType)) {
              _tmpCustomer_type = null;
            } else {
              _tmpCustomer_type = _cursor.getString(_cursorIndexOfCustomerType);
            }
            final boolean _tmpIs_active;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIs_active = _tmp != 0;
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
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _item = new CustomerEntity(_tmpId,_tmpName,_tmpPhone,_tmpAddress,_tmpWilaya,_tmpCommune,_tmpPhoto_url,_tmpCredit_limit,_tmpCurrent_credit,_tmpCustomer_type,_tmpIs_active,_tmpNotes,_tmpCreated_at,_tmpUpdated_at,_tmpDeleted_at);
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
  public Object getCustomerById(final String id,
      final Continuation<? super CustomerEntity> $completion) {
    final String _sql = "SELECT * FROM customers WHERE id = ? AND deleted_at IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CustomerEntity>() {
      @Override
      @Nullable
      public CustomerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfWilaya = CursorUtil.getColumnIndexOrThrow(_cursor, "wilaya");
          final int _cursorIndexOfCommune = CursorUtil.getColumnIndexOrThrow(_cursor, "commune");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photo_url");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "credit_limit");
          final int _cursorIndexOfCurrentCredit = CursorUtil.getColumnIndexOrThrow(_cursor, "current_credit");
          final int _cursorIndexOfCustomerType = CursorUtil.getColumnIndexOrThrow(_cursor, "customer_type");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final CustomerEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final String _tmpWilaya;
            if (_cursor.isNull(_cursorIndexOfWilaya)) {
              _tmpWilaya = null;
            } else {
              _tmpWilaya = _cursor.getString(_cursorIndexOfWilaya);
            }
            final String _tmpCommune;
            if (_cursor.isNull(_cursorIndexOfCommune)) {
              _tmpCommune = null;
            } else {
              _tmpCommune = _cursor.getString(_cursorIndexOfCommune);
            }
            final String _tmpPhoto_url;
            if (_cursor.isNull(_cursorIndexOfPhotoUrl)) {
              _tmpPhoto_url = null;
            } else {
              _tmpPhoto_url = _cursor.getString(_cursorIndexOfPhotoUrl);
            }
            final double _tmpCredit_limit;
            _tmpCredit_limit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            final double _tmpCurrent_credit;
            _tmpCurrent_credit = _cursor.getDouble(_cursorIndexOfCurrentCredit);
            final String _tmpCustomer_type;
            if (_cursor.isNull(_cursorIndexOfCustomerType)) {
              _tmpCustomer_type = null;
            } else {
              _tmpCustomer_type = _cursor.getString(_cursorIndexOfCustomerType);
            }
            final boolean _tmpIs_active;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIs_active = _tmp != 0;
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
            final String _tmpDeleted_at;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeleted_at = null;
            } else {
              _tmpDeleted_at = _cursor.getString(_cursorIndexOfDeletedAt);
            }
            _result = new CustomerEntity(_tmpId,_tmpName,_tmpPhone,_tmpAddress,_tmpWilaya,_tmpCommune,_tmpPhoto_url,_tmpCredit_limit,_tmpCurrent_credit,_tmpCustomer_type,_tmpIs_active,_tmpNotes,_tmpCreated_at,_tmpUpdated_at,_tmpDeleted_at);
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
