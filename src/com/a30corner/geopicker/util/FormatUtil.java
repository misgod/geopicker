package com.a30corner.geopicker.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import com.a30corner.geopicker.R;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class FormatUtil {
    private static final String LAT_LON_PATTERN = "{0}° {1}'' {2}\" {3}";
    protected static final String TAG = "FormatUtil";
    private MessageFormat formatterDMS, formatterDD;
    private static final String FORMAT_PREF = "format_pref";

    private Context mContext;
    private static final int FORMAT_DECIMAL_DEGREES = 1;
    private static final int FORMAT_DEGREES_MINUTES_SECONDS = 0;
    private int mGeoFormat;


    public FormatUtil(Context context) {
        mContext = context;
        formatterDMS = new MessageFormat(
                context.getString(R.string.geo_text_format_dms));
        formatterDD = new MessageFormat(
                context.getString(R.string.geo_text_format_dd));

        if (context.getSharedPreferences(TAG, Context.MODE_PRIVATE).getBoolean(
                FORMAT_PREF, true)) {
            mGeoFormat = FORMAT_DEGREES_MINUTES_SECONDS;
        } else {
            mGeoFormat = FORMAT_DECIMAL_DEGREES;
        }



    }

    public void toggleFormat() {
        if (mGeoFormat == FORMAT_DEGREES_MINUTES_SECONDS) {
            mGeoFormat = FORMAT_DECIMAL_DEGREES;
        } else {
            mGeoFormat = FORMAT_DEGREES_MINUTES_SECONDS;
        }



        Editor editor = mContext
                .getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        editor.putBoolean(FORMAT_PREF,
                mGeoFormat == FORMAT_DEGREES_MINUTES_SECONDS);
        editor.apply();

    }


    public String getLocText(GeoPoint gp) {
        String result;
        if (mGeoFormat == FORMAT_DEGREES_MINUTES_SECONDS) {
            result = formatterDMS.format(new Object[] {
                    FormatUtil.getFormatedLat(gp.getLatitudeE6()),
                    FormatUtil.getFormatedLon(gp.getLongitudeE6())});
        } else {
            result = formatterDD.format(new Object[] {
                    gp.getLatitudeE6() / 1e6D, gp.getLongitudeE6() / 1e6D});
        }
        return result;
    }

    public String getLocCopy(GeoPoint gp) {
        MessageFormat formatter = new MessageFormat("{0}, {1}");
        String result;
        
        
        if (mGeoFormat == FORMAT_DEGREES_MINUTES_SECONDS) {
            result = formatter.format(new Object[] {
                    FormatUtil.getFormatedLat(gp.getLatitudeE6()),
                    FormatUtil.getFormatedLon(gp.getLongitudeE6())});
        } else {
            DecimalFormat df = new DecimalFormat("###.######");
            String latStr = df.format(gp.getLatitudeE6() / 1e6D);
            String lonStr = df.format(gp.getLongitudeE6() / 1e6D);
            result = formatter.format(new Object[] {latStr, lonStr});
        }

        return result;
    }



    public static String getFormatedLat(int latE6) {
        double lat = latE6 / 1e6D;

        DecimalFormat decFormatter = new DecimalFormat("00.00");

        // DecimalFormat secFormat = new DecimalFormat(SEC_PATTERN);
        MessageFormat mformat = new MessageFormat(LAT_LON_PATTERN);
        String[] argLat = new String[] {"0", "0", "0", "0"};

        String latStr = Location.convert(lat, Location.FORMAT_SECONDS);

        String[] latArr = latStr.split(":");
        argLat[0] = String.valueOf(Math.abs(Integer.parseInt(latArr[0])));
        argLat[1] = latArr[1];

        try {
            Number number = NumberFormat.getNumberInstance().parse(latArr[2]);
            double d = number.doubleValue() + 0.004;
            argLat[2] = decFormatter.format(d);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        // argLat[2] = secFormat.format(Double.parseDouble(latArr[2]));
        argLat[3] = (Integer.parseInt(latArr[0]) >= 0) ? "N" : "S";
        return mformat.format(argLat);
    }

    public static String getFormatedLon(int lonE6) {
        double lon = lonE6 / 1e6D;
        DecimalFormat decFormatter = new DecimalFormat("00.00");
        MessageFormat mformat = new MessageFormat(LAT_LON_PATTERN);
        String[] argLon = new String[] {"0", "0", "0", "0"};
        String lonStr = Location.convert(lon, Location.FORMAT_SECONDS);

        String[] lonArr = lonStr.split(":");
        argLon[0] = String.valueOf(Math.abs(Integer.parseInt(lonArr[0])));
        argLon[1] = lonArr[1];

        try {
            Number number = NumberFormat.getNumberInstance().parse(lonArr[2]);
            double d = number.doubleValue() + 0.004;
            argLon[2] = decFormatter.format(d);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        argLon[3] = (Integer.parseInt(lonArr[0]) >= 0) ? "E" : "W";

        return mformat.format(argLon);
    }



}
