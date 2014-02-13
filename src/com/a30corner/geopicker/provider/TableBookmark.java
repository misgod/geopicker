package com.a30corner.geopicker.provider;

import android.net.Uri;

public class TableBookmark {
	public static String TABLE = "bookmark";

	public static final Uri CONTENT_URI = Uri.withAppendedPath(BookmarkProvider.CONTENT_URI, TABLE);
	
	public static String getCreateTableSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(TABLE).append("(");
		sb.append(Columns.ID).append(" INTEGER PRIMARY KEY,");
		sb.append(Columns.NAME).append(" TEXT,");
		sb.append(Columns.ADDRESS).append(" TEXT,");
		sb.append(Columns.LON_LAT).append(" TEXT");

		sb.append(");");
		return sb.toString();
	}
	
	public static final class Columns {
		public static final String ID = "_id";
		public static final String NAME = "name";
		public static final String ADDRESS = "address";
		public static final String LON_LAT = "lon_lat";

	}
}
