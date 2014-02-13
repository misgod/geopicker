package com.a30corner.geopicker.util;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class AddressUtil {
    public static final String URI_PATTERN = "http://maps.google.com/maps/geo?q={0,number,##.000000},{1,number,###.000000}&output=json&hl={2}&oe=utf8&sensor=false&key=0kCAoDptwbwEkNWyi1QwcyBh1bZsfvL0i1vWIZA";
    protected static final String TAG = "AddressUtil";

    public void lookup(final Context context,final GeoPoint p, final  OnLookAddressListener listener) {
        final Handler handler = new Handler(context.getMainLooper());
        Locale defaultLocale = Locale.getDefault();
        final StringBuilder lang = new StringBuilder();
        lang.append(defaultLocale.getLanguage()).append("_").append(defaultLocale.getCountry());
        new Thread(){
            public void run(){
                MessageFormat mf = new MessageFormat(URI_PATTERN);
                String uriStr = mf.format(new Object[] {
                        p.getLatitudeE6() / 1e6D, p.getLongitudeE6() / 1e6D,lang.toString()});
                URI targetUri = URI.create(uriStr);
                
                AndroidHttpClient client = null;
                try {
                    client = AndroidHttpClient.newInstance("GeoPicker",context);// .newInstance("Android");
                    HttpUriRequest req = new HttpGet(targetUri);
                    HttpResponse response = client.execute(req);
                    StatusLine status = response.getStatusLine();
                    if (status.getStatusCode() != 200) { // HTTP 200 is success.
                        handler.post(new Runnable(){
                            @Override
                            public void run() {
                                listener.onFailure();
                            }    
                        });
                     
                    }else{
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            JSONObject json = new JSONObject(EntityUtils.toString(entity, "UTF-8"));
                            JSONArray placemarkArray = json.getJSONArray("Placemark");
                            int maxAccuracy = 0;
                            JSONObject maxObject=null;
                            for(int i=0;i<placemarkArray.length();i++){
                                JSONObject placemark = placemarkArray.getJSONObject(i);
                                JSONObject addressDetail  = placemark.getJSONObject("AddressDetails");
                                if(addressDetail !=null){
                                    int accuracy = addressDetail.getInt("Accuracy");
                                    if(accuracy >maxAccuracy){
                                        maxAccuracy = accuracy;
                                        maxObject = placemark;
                                    }
                                }
                            }
                            entity.consumeContent();
                            if(maxObject !=null){
                                final JSONObject aPlacemark = maxObject;
                                handler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        try {
                                            listener.onSuccess(aPlacemark.getString("address"));
                                        } catch (JSONException e) {
                                            listener.onFailure();
                                        }
                                    }    
                                });
                            
                            }
                        }
                    }
                  
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    handler.post(new Runnable(){
                        @Override
                        public void run() {
 
                            listener.onFailure();
                        }    
                    });
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }
            }
        }.start();

    }

    public interface OnLookAddressListener {
        public void onSuccess(String address);

        public void onFailure();
    }

}
