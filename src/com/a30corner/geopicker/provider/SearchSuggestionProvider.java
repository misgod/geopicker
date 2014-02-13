
package com.a30corner.geopicker.provider;

import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.net.Uri;


public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    
    /**
     * This is the provider authority identifier.  The same string must appear in your
     * Manifest file, and any time you instantiate a 
     * {@link android.provider.SearchRecentSuggestions} helper class. 
     */
    public final static String AUTHORITY = "com.android.lee.geopicker.provider.SearchSuggestionProvider";
    /**
     * These flags determine the operating mode of the suggestions provider.  This value should 
     * not change from run to run, because when it does change, your suggestions database may 
     * be wiped.
     */
    public final static int MODE = DATABASE_MODE_QUERIES;

//    private GoogleSuggestionUtil mUtil;
    /**
     * The main job of the constructor is to call {@link #setupSuggestions(String, int)} with the
     * appropriate configuration values.
     */
    public SearchSuggestionProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
//        mUtil = new GoogleSuggestionUtil();
    }
    @Override
    public Cursor query(Uri uri, String projection[], String selection,
            String selectionArgs[], String sortOrder) {

        Cursor result = super.query(uri, projection, selection, selectionArgs, sortOrder);
        result.deactivate();
//        if(result.getCount()==0){
//
//            result.close();
//            JSONArray jsonArr = mUtil.search(getContext(),selectionArgs[0]);
//            JSONArray nameArr;
//            try {
//                nameArr = jsonArr.getJSONArray(1);
//                JSONArray countArr = jsonArr.getJSONArray(2);
//                Object[] data = new Object[2];
//            
//                String[] columns = new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1,SearchManager.SUGGEST_COLUMN_TEXT_2};
//                MatrixCursor  mycursor = new  MatrixCursor(columns); 
//
//                if(nameArr !=null){
//                    int length = nameArr.length();
//                    for(int i=0;i<length;i++){
//                        mycursor.addRow( new Object[]{ nameArr.getString(i), countArr.getString(i)});
//                    }
//                }
//                result = mycursor;
//            } catch (JSONException e) {
//               Log.e("SearchSuggestionProvider",e.getMessage(),e);
//            }
//            
//        }
        
        return result;
    }
    
}
