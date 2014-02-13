package com.a30corner.geopicker.provider;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

public class BookmarkHelper {
    private Context mContext;

    public BookmarkHelper(Context context) {
        mContext = context;
    }

    public void addBookmark(Bookmark bmk) {
        mContext.getContentResolver().insert(TableBookmark.CONTENT_URI,
                bmk.toContentValues());
    }

    public void delBookmark(Bookmark bmk) {
        mContext.getContentResolver().delete(TableBookmark.CONTENT_URI,
                TableBookmark.Columns.ID + " = ?",
                new String[] { String.valueOf(bmk.id) });
    }

    public ArrayList<Bookmark> getAllBookmarks() {
        Cursor c = mContext.getContentResolver().query(
                TableBookmark.CONTENT_URI, null, null, null,
                TableBookmark.Columns.ID + " asc");
        if (c != null) {
            ArrayList<Bookmark> result = new ArrayList<Bookmark>(c.getCount());
            try {
                while(c.moveToNext()){
                    result.add(new Bookmark(c));
                }
            } catch (Exception e) {
                Log.e("BookmarkHelper", e.getMessage(), e);
                return  new ArrayList<Bookmark>(0);
            } finally {
                c.close();
            }

            return result;
        } else {
            return new ArrayList<Bookmark>(0);
        }

    }

}
