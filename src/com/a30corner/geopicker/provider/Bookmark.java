package com.a30corner.geopicker.provider;

import com.google.android.maps.GeoPoint;

import android.content.ContentValues;
import android.database.Cursor;

public class Bookmark {
    public int id;
    public String name;
    public String address;
    public int lon;
    public int lat;

    public Bookmark(Cursor cursor) {
        id = cursor.getInt(cursor
                .getColumnIndexOrThrow(TableBookmark.Columns.ID));
        name = cursor.getString(cursor
                .getColumnIndexOrThrow(TableBookmark.Columns.NAME));
        address = cursor.getString(cursor
                .getColumnIndexOrThrow(TableBookmark.Columns.ADDRESS));
        
        String rawCor = cursor.getString(cursor
                .getColumnIndexOrThrow(TableBookmark.Columns.LON_LAT));

        String[] corr = rawCor.split(",");
        lon = Integer.parseInt(corr[0]);
        lat = Integer.parseInt(corr[1]);
        
    }
    
    
    public Bookmark(String name,String address,int lon,int lat) {
        this.id = -1;
        this.name = name;
        this.address = address;
        this.lon = lon;
        this.lat = lat;
    }
    
    
    public ContentValues toContentValues(){
        ContentValues result = new ContentValues();
        result.put(TableBookmark.Columns.NAME, name);
        result.put(TableBookmark.Columns.ADDRESS, address);
        result.put(TableBookmark.Columns.LON_LAT, lon+","+lat);

        
        return result;
        
    }
    
    
    @Override
    public int hashCode() {

        return id;
    }
    
    
    @Override
    public boolean equals(Object o) {

        return hashCode() == o.hashCode();
    }


    public GeoPoint getPoint() {
        
        return new GeoPoint(lat,lon);
    }
    

}
