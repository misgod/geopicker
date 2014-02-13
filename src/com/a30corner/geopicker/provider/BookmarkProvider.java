package com.a30corner.geopicker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class BookmarkProvider extends ContentProvider {

    private static final String TAG = BookmarkProvider.class.getSimpleName();
    private static final String DATABASE_NAME = "bookmark.db";

    private static final int DATABASE_VERSION = 1;

    public static final String AUTHORITY = "com.a30corner.geopicker";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private SQLiteDatabase mDB;
    private SQLiteOpenHelper mOpenHelper;
    private static final int CASE_BOOKMARK = 0x111;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, TableBookmark.TABLE, CASE_BOOKMARK);

    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new SQLiteOpenHelper(getContext(), DATABASE_NAME, null,
                DATABASE_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(TableBookmark.getCreateTableSQL());
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion,
                    int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + TableBookmark.TABLE);
                onCreate(db);
            }

        };

        mDB = mOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (sUriMatcher.match(uri)) {
        case CASE_BOOKMARK:
            count = mDB.delete(TableBookmark.TABLE, selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = 0L;
        switch (sUriMatcher.match(uri)) {
        case CASE_BOOKMARK:
            rowId = mDB.insert(TableBookmark.TABLE, null, values);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rowId > 0) {
            return Uri.withAppendedPath(uri, "" + rowId);
        }
        throw new SQLException("Failed to insert row into " + uri);

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch(sUriMatcher.match(uri)) {
        case CASE_BOOKMARK:
                qb.setTables(TableBookmark.TABLE);
                break;
      
        default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return qb.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int count = 0;
        switch(sUriMatcher.match(uri)) {
        case CASE_BOOKMARK:
                count = mDB.update(TableBookmark.TABLE, values, selection, selectionArgs);
                break;
        default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return count;
    }

}
