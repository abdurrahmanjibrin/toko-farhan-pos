package com.tokofarhan.pos.data.local.dao;

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
import com.tokofarhan.pos.data.local.entity.ProductEntity;
import com.tokofarhan.pos.data.local.entity.StockInEntity;
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
public final class ProductDao_Impl implements ProductDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProductEntity> __insertionAdapterOfProductEntity;

  private final EntityInsertionAdapter<StockInEntity> __insertionAdapterOfStockInEntity;

  private final EntityDeletionOrUpdateAdapter<ProductEntity> __updateAdapterOfProductEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProduct;

  private final SharedSQLiteStatement __preparedStmtOfUpdateProductPrice;

  public ProductDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProductEntity = new EntityInsertionAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `products` (`id`,`name`,`barcode`,`satuanBeli`,`satuanJual`,`rasioKonversi`,`currentStokJual`,`hppMovingAverage`,`hargaJualDefault`,`minStockAlert`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getBarcode() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBarcode());
        }
        statement.bindString(4, entity.getSatuanBeli());
        statement.bindString(5, entity.getSatuanJual());
        statement.bindLong(6, entity.getRasioKonversi());
        statement.bindDouble(7, entity.getCurrentStokJual());
        statement.bindDouble(8, entity.getHppMovingAverage());
        statement.bindDouble(9, entity.getHargaJualDefault());
        statement.bindLong(10, entity.getMinStockAlert());
        statement.bindString(11, entity.getCreatedAt());
        statement.bindString(12, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfStockInEntity = new EntityInsertionAdapter<StockInEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `stock_ins` (`id`,`productId`,`qtyBeli`,`qtyJualConverted`,`totalHargaBeli`,`hppPerUnitJual`,`hppAfterRestock`,`timestamp`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockInEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getProductId());
        statement.bindDouble(3, entity.getQtyBeli());
        statement.bindDouble(4, entity.getQtyJualConverted());
        statement.bindDouble(5, entity.getTotalHargaBeli());
        statement.bindDouble(6, entity.getHppPerUnitJual());
        statement.bindDouble(7, entity.getHppAfterRestock());
        statement.bindString(8, entity.getTimestamp());
      }
    };
    this.__updateAdapterOfProductEntity = new EntityDeletionOrUpdateAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `products` SET `id` = ?,`name` = ?,`barcode` = ?,`satuanBeli` = ?,`satuanJual` = ?,`rasioKonversi` = ?,`currentStokJual` = ?,`hppMovingAverage` = ?,`hargaJualDefault` = ?,`minStockAlert` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getBarcode() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBarcode());
        }
        statement.bindString(4, entity.getSatuanBeli());
        statement.bindString(5, entity.getSatuanJual());
        statement.bindLong(6, entity.getRasioKonversi());
        statement.bindDouble(7, entity.getCurrentStokJual());
        statement.bindDouble(8, entity.getHppMovingAverage());
        statement.bindDouble(9, entity.getHargaJualDefault());
        statement.bindLong(10, entity.getMinStockAlert());
        statement.bindString(11, entity.getCreatedAt());
        statement.bindString(12, entity.getUpdatedAt());
        statement.bindString(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteProduct = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM products WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateProductPrice = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE products SET hargaJualDefault = ?, updatedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProduct(final ProductEntity product,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProductEntity.insert(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStockIn(final StockInEntity stockIn,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockInEntity.insert(stockIn);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProduct(final ProductEntity product,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProductEntity.handle(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProduct(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProduct.acquire();
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
          __preparedStmtOfDeleteProduct.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProductPrice(final String id, final double newPrice, final String updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateProductPrice.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, newPrice);
        _argIndex = 2;
        _stmt.bindString(_argIndex, updatedAt);
        _argIndex = 3;
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
          __preparedStmtOfUpdateProductPrice.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllProducts(final Continuation<? super List<ProductEntity>> $completion) {
    final String _sql = "SELECT * FROM products ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSatuanBeli = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanBeli");
          final int _cursorIndexOfSatuanJual = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanJual");
          final int _cursorIndexOfRasioKonversi = CursorUtil.getColumnIndexOrThrow(_cursor, "rasioKonversi");
          final int _cursorIndexOfCurrentStokJual = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStokJual");
          final int _cursorIndexOfHppMovingAverage = CursorUtil.getColumnIndexOrThrow(_cursor, "hppMovingAverage");
          final int _cursorIndexOfHargaJualDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "hargaJualDefault");
          final int _cursorIndexOfMinStockAlert = CursorUtil.getColumnIndexOrThrow(_cursor, "minStockAlert");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpSatuanBeli;
            _tmpSatuanBeli = _cursor.getString(_cursorIndexOfSatuanBeli);
            final String _tmpSatuanJual;
            _tmpSatuanJual = _cursor.getString(_cursorIndexOfSatuanJual);
            final int _tmpRasioKonversi;
            _tmpRasioKonversi = _cursor.getInt(_cursorIndexOfRasioKonversi);
            final double _tmpCurrentStokJual;
            _tmpCurrentStokJual = _cursor.getDouble(_cursorIndexOfCurrentStokJual);
            final double _tmpHppMovingAverage;
            _tmpHppMovingAverage = _cursor.getDouble(_cursorIndexOfHppMovingAverage);
            final double _tmpHargaJualDefault;
            _tmpHargaJualDefault = _cursor.getDouble(_cursorIndexOfHargaJualDefault);
            final int _tmpMinStockAlert;
            _tmpMinStockAlert = _cursor.getInt(_cursorIndexOfMinStockAlert);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            final String _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            _item = new ProductEntity(_tmpId,_tmpName,_tmpBarcode,_tmpSatuanBeli,_tmpSatuanJual,_tmpRasioKonversi,_tmpCurrentStokJual,_tmpHppMovingAverage,_tmpHargaJualDefault,_tmpMinStockAlert,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getProductByBarcode(final String barcode,
      final Continuation<? super ProductEntity> $completion) {
    final String _sql = "SELECT * FROM products WHERE barcode = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, barcode);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductEntity>() {
      @Override
      @Nullable
      public ProductEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSatuanBeli = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanBeli");
          final int _cursorIndexOfSatuanJual = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanJual");
          final int _cursorIndexOfRasioKonversi = CursorUtil.getColumnIndexOrThrow(_cursor, "rasioKonversi");
          final int _cursorIndexOfCurrentStokJual = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStokJual");
          final int _cursorIndexOfHppMovingAverage = CursorUtil.getColumnIndexOrThrow(_cursor, "hppMovingAverage");
          final int _cursorIndexOfHargaJualDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "hargaJualDefault");
          final int _cursorIndexOfMinStockAlert = CursorUtil.getColumnIndexOrThrow(_cursor, "minStockAlert");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProductEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpSatuanBeli;
            _tmpSatuanBeli = _cursor.getString(_cursorIndexOfSatuanBeli);
            final String _tmpSatuanJual;
            _tmpSatuanJual = _cursor.getString(_cursorIndexOfSatuanJual);
            final int _tmpRasioKonversi;
            _tmpRasioKonversi = _cursor.getInt(_cursorIndexOfRasioKonversi);
            final double _tmpCurrentStokJual;
            _tmpCurrentStokJual = _cursor.getDouble(_cursorIndexOfCurrentStokJual);
            final double _tmpHppMovingAverage;
            _tmpHppMovingAverage = _cursor.getDouble(_cursorIndexOfHppMovingAverage);
            final double _tmpHargaJualDefault;
            _tmpHargaJualDefault = _cursor.getDouble(_cursorIndexOfHargaJualDefault);
            final int _tmpMinStockAlert;
            _tmpMinStockAlert = _cursor.getInt(_cursorIndexOfMinStockAlert);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            final String _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            _result = new ProductEntity(_tmpId,_tmpName,_tmpBarcode,_tmpSatuanBeli,_tmpSatuanJual,_tmpRasioKonversi,_tmpCurrentStokJual,_tmpHppMovingAverage,_tmpHargaJualDefault,_tmpMinStockAlert,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getProductById(final String id,
      final Continuation<? super ProductEntity> $completion) {
    final String _sql = "SELECT * FROM products WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductEntity>() {
      @Override
      @Nullable
      public ProductEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSatuanBeli = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanBeli");
          final int _cursorIndexOfSatuanJual = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanJual");
          final int _cursorIndexOfRasioKonversi = CursorUtil.getColumnIndexOrThrow(_cursor, "rasioKonversi");
          final int _cursorIndexOfCurrentStokJual = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStokJual");
          final int _cursorIndexOfHppMovingAverage = CursorUtil.getColumnIndexOrThrow(_cursor, "hppMovingAverage");
          final int _cursorIndexOfHargaJualDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "hargaJualDefault");
          final int _cursorIndexOfMinStockAlert = CursorUtil.getColumnIndexOrThrow(_cursor, "minStockAlert");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProductEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpSatuanBeli;
            _tmpSatuanBeli = _cursor.getString(_cursorIndexOfSatuanBeli);
            final String _tmpSatuanJual;
            _tmpSatuanJual = _cursor.getString(_cursorIndexOfSatuanJual);
            final int _tmpRasioKonversi;
            _tmpRasioKonversi = _cursor.getInt(_cursorIndexOfRasioKonversi);
            final double _tmpCurrentStokJual;
            _tmpCurrentStokJual = _cursor.getDouble(_cursorIndexOfCurrentStokJual);
            final double _tmpHppMovingAverage;
            _tmpHppMovingAverage = _cursor.getDouble(_cursorIndexOfHppMovingAverage);
            final double _tmpHargaJualDefault;
            _tmpHargaJualDefault = _cursor.getDouble(_cursorIndexOfHargaJualDefault);
            final int _tmpMinStockAlert;
            _tmpMinStockAlert = _cursor.getInt(_cursorIndexOfMinStockAlert);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            final String _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            _result = new ProductEntity(_tmpId,_tmpName,_tmpBarcode,_tmpSatuanBeli,_tmpSatuanJual,_tmpRasioKonversi,_tmpCurrentStokJual,_tmpHppMovingAverage,_tmpHargaJualDefault,_tmpMinStockAlert,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getLowStockProducts(final Continuation<? super List<ProductEntity>> $completion) {
    final String _sql = "SELECT * FROM products WHERE currentStokJual <= minStockAlert AND minStockAlert > 0 ORDER BY currentStokJual ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfSatuanBeli = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanBeli");
          final int _cursorIndexOfSatuanJual = CursorUtil.getColumnIndexOrThrow(_cursor, "satuanJual");
          final int _cursorIndexOfRasioKonversi = CursorUtil.getColumnIndexOrThrow(_cursor, "rasioKonversi");
          final int _cursorIndexOfCurrentStokJual = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStokJual");
          final int _cursorIndexOfHppMovingAverage = CursorUtil.getColumnIndexOrThrow(_cursor, "hppMovingAverage");
          final int _cursorIndexOfHargaJualDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "hargaJualDefault");
          final int _cursorIndexOfMinStockAlert = CursorUtil.getColumnIndexOrThrow(_cursor, "minStockAlert");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpSatuanBeli;
            _tmpSatuanBeli = _cursor.getString(_cursorIndexOfSatuanBeli);
            final String _tmpSatuanJual;
            _tmpSatuanJual = _cursor.getString(_cursorIndexOfSatuanJual);
            final int _tmpRasioKonversi;
            _tmpRasioKonversi = _cursor.getInt(_cursorIndexOfRasioKonversi);
            final double _tmpCurrentStokJual;
            _tmpCurrentStokJual = _cursor.getDouble(_cursorIndexOfCurrentStokJual);
            final double _tmpHppMovingAverage;
            _tmpHppMovingAverage = _cursor.getDouble(_cursorIndexOfHppMovingAverage);
            final double _tmpHargaJualDefault;
            _tmpHargaJualDefault = _cursor.getDouble(_cursorIndexOfHargaJualDefault);
            final int _tmpMinStockAlert;
            _tmpMinStockAlert = _cursor.getInt(_cursorIndexOfMinStockAlert);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            final String _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            _item = new ProductEntity(_tmpId,_tmpName,_tmpBarcode,_tmpSatuanBeli,_tmpSatuanJual,_tmpRasioKonversi,_tmpCurrentStokJual,_tmpHppMovingAverage,_tmpHargaJualDefault,_tmpMinStockAlert,_tmpCreatedAt,_tmpUpdatedAt);
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
