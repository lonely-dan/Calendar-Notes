package com.example.calendarmemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.calendarmemo.Constants;
import com.example.calendarmemo.model.Memo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 备忘录数据访问对象（DAO）。
 *
 * <p>把 SQL 与界面代码隔离：Activity 只调用 insert / update / findByDate 等方法，
 * 不直接拼 SQL，便于维护与讲解。</p>
 */
public class MemoDao {

    private final MemoDbHelper helper;

    public MemoDao(Context context) {
        helper = new MemoDbHelper(context.getApplicationContext());
    }

    public long insert(Memo memo) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long now = System.currentTimeMillis();
        if (memo.getCreatedAt() <= 0) {
            memo.setCreatedAt(now);
        }
        memo.setUpdatedAt(now);

        ContentValues values = toValues(memo, true);
        long id = db.insert(MemoDbHelper.TABLE_MEMOS, null, values);
        if (id > 0) {
            memo.setId(id);
        }
        return id;
    }

    public int update(Memo memo) {
        if (memo.getId() == Constants.NO_MEMO_ID) {
            return 0;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        memo.setUpdatedAt(System.currentTimeMillis());
        ContentValues values = toValues(memo, false);
        return db.update(MemoDbHelper.TABLE_MEMOS, values,
                MemoDbHelper.COL_ID + " = ?",
                new String[]{String.valueOf(memo.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(MemoDbHelper.TABLE_MEMOS,
                MemoDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Memo findById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.query(MemoDbHelper.TABLE_MEMOS, null,
                MemoDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return readMemo(cursor);
            }
        }
        return null;
    }

    /**
     * 查询某一天的备忘录，按重要程度、更新时间倒序。
     *
     * @param category      null 或空字符串表示全部类别
     * @param onlyImportant 是否只看重要/紧急
     */
    public List<Memo> findByDate(String date, String category, boolean onlyImportant) {
        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder selection = new StringBuilder(MemoDbHelper.COL_DATE + " = ?");
        List<String> args = new ArrayList<>();
        args.add(date);
        if (category != null && !category.isEmpty()) {
            selection.append(" AND ").append(MemoDbHelper.COL_CATEGORY).append(" = ?");
            args.add(category);
        }
        if (onlyImportant) {
            selection.append(" AND ").append(MemoDbHelper.COL_PRIORITY).append(" >= ?");
            args.add(String.valueOf(Constants.PRIORITY_IMPORTANT));
        }

        List<Memo> result = new ArrayList<>();
        try (Cursor cursor = db.query(MemoDbHelper.TABLE_MEMOS, null,
                selection.toString(), args.toArray(new String[0]),
                null, null,
                MemoDbHelper.COL_PRIORITY + " DESC, "
                        + MemoDbHelper.COL_UPDATED_AT + " DESC, "
                        + MemoDbHelper.COL_ID + " DESC")) {
            while (cursor.moveToNext()) {
                result.add(readMemo(cursor));
            }
        }
        return result;
    }

    public int countByDate(String date) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + MemoDbHelper.TABLE_MEMOS
                        + " WHERE " + MemoDbHelper.COL_DATE + " = ?",
                new String[]{date})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /** 返回“日期 -> 圆点数”，用于月历下方的小圆点。 */
    public Map<String, Integer> dotCounts(String monthPrefix) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Map<String, Integer> counts = new HashMap<>();
        try (Cursor cursor = db.rawQuery(
                "SELECT " + MemoDbHelper.COL_DATE + ", COUNT(*) AS dot_count"
                        + " FROM " + MemoDbHelper.TABLE_MEMOS
                        + " WHERE " + MemoDbHelper.COL_DATE + " LIKE ?"
                        + " AND " + MemoDbHelper.COL_SHOW_DOT + " = 1"
                        + " GROUP BY " + MemoDbHelper.COL_DATE,
                new String[]{monthPrefix + "%"})) {
            while (cursor.moveToNext()) {
                counts.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        return counts;
    }

    public void close() {
        helper.close();
    }

    private ContentValues toValues(Memo memo, boolean includeCreatedAt) {
        ContentValues values = new ContentValues();
        values.put(MemoDbHelper.COL_TITLE, memo.getTitle());
        values.put(MemoDbHelper.COL_CONTENT, memo.getContent());
        values.put(MemoDbHelper.COL_DATE, memo.getDate());
        values.put(MemoDbHelper.COL_CATEGORY, memo.getCategory());
        values.put(MemoDbHelper.COL_PRIORITY, memo.getPriority());
        values.put(MemoDbHelper.COL_SHOW_DOT, memo.isShowDot() ? 1 : 0);
        if (includeCreatedAt) {
            values.put(MemoDbHelper.COL_CREATED_AT, memo.getCreatedAt());
        }
        values.put(MemoDbHelper.COL_UPDATED_AT, memo.getUpdatedAt());
        return values;
    }

    private Memo readMemo(Cursor cursor) {
        Memo memo = new Memo();
        memo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_ID)));
        memo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_TITLE)));
        memo.setContent(cursor.getString(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_CONTENT)));
        memo.setDate(cursor.getString(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_DATE)));
        memo.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_CATEGORY)));
        memo.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_PRIORITY)));
        memo.setShowDot(cursor.getInt(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_SHOW_DOT)) == 1);
        memo.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_CREATED_AT)));
        memo.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(MemoDbHelper.COL_UPDATED_AT)));
        return memo;
    }
}
