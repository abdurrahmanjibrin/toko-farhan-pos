package com.tokofarhan.pos.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.DBUtil;
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
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReportDao_Impl implements ReportDao {
  private final RoomDatabase __db;

  public ReportDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public Object getDailyProfitLoss(final Continuation<? super List<ProfitLossEntry>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            DATE(t.timestamp) as date,\n"
            + "            COUNT(DISTINCT t.id) as totalTransactions,\n"
            + "            SUM(t.totalAmount) as totalRevenue,\n"
            + "            SUM(ti.qtyJual * ti.recordedHpp) as totalHppCost,\n"
            + "            SUM(ti.profit) as totalProfit\n"
            + "        FROM transactions t\n"
            + "        JOIN transaction_items ti ON t.id = ti.transactionId\n"
            + "        GROUP BY DATE(t.timestamp)\n"
            + "        ORDER BY date ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProfitLossEntry>>() {
      @Override
      @NonNull
      public List<ProfitLossEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = 0;
          final int _cursorIndexOfTotalTransactions = 1;
          final int _cursorIndexOfTotalRevenue = 2;
          final int _cursorIndexOfTotalHppCost = 3;
          final int _cursorIndexOfTotalProfit = 4;
          final List<ProfitLossEntry> _result = new ArrayList<ProfitLossEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProfitLossEntry _item;
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final int _tmpTotalTransactions;
            _tmpTotalTransactions = _cursor.getInt(_cursorIndexOfTotalTransactions);
            final double _tmpTotalRevenue;
            _tmpTotalRevenue = _cursor.getDouble(_cursorIndexOfTotalRevenue);
            final double _tmpTotalHppCost;
            _tmpTotalHppCost = _cursor.getDouble(_cursorIndexOfTotalHppCost);
            final double _tmpTotalProfit;
            _tmpTotalProfit = _cursor.getDouble(_cursorIndexOfTotalProfit);
            _item = new ProfitLossEntry(_tmpDate,_tmpTotalTransactions,_tmpTotalRevenue,_tmpTotalHppCost,_tmpTotalProfit);
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
  public Object getLowStockCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM products WHERE currentStokJual <= minStockAlert AND minStockAlert > 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
