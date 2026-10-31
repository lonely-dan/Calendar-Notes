package com.example.calendarmemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper：负责数据库创建与版本升级。
 *
 * <p>课程设计只使用一张 memos 表，字段设计见设计文档。所有数据保存在本地，
 * 不申请网络权限，卸载应用后数据随之删除。</p>
 */
public class MemoDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "shiguang_memo.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_MEMOS = "memos";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_DATE = "memo_date";
    public static final String COL_CATEGORY = "category";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_SHOW_DOT = "show_dot";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    private static final String SQL_CREATE_MEMOS =
            "CREATE TABLE " + TABLE_MEMOS + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TITLE + " TEXT NOT NULL, "
                    + COL_CONTENT + " TEXT NOT NULL DEFAULT '', "
                    + COL_DATE + " TEXT NOT NULL, "
                    + COL_CATEGORY + " TEXT NOT NULL DEFAULT '学习', "
                    + COL_PRIORITY + " INTEGER NOT NULL DEFAULT 0, "
                    + COL_SHOW_DOT + " INTEGER NOT NULL DEFAULT 1, "
                    + COL_CREATED_AT + " INTEGER NOT NULL, "
                    + COL_UPDATED_AT + " INTEGER NOT NULL"
                    + ")";

    private static final String SQL_CREATE_DATE_INDEX =
            "CREATE INDEX idx_memo_date ON " + TABLE_MEMOS + "(" + COL_DATE + ")";

    public MemoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MEMOS);
        db.execSQL(SQL_CREATE_DATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 课程设计版本只有 v1；若后续升级，采用重建表策略。
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMOS);
        onCreate(db);
    }
}
