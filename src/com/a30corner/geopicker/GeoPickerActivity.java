package com.a30corner.geopicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import com.a30corner.geopicker.overlay.DragableOverlay;
import com.a30corner.geopicker.overlay.DragableOverlay.OnLocationChangeListener;
import com.a30corner.geopicker.overlay.FallInOverlay;
import com.a30corner.geopicker.overlay.FallInOverlay.OnEndListener;
import com.a30corner.geopicker.provider.Bookmark;
import com.a30corner.geopicker.provider.BookmarkHelper;
import com.a30corner.geopicker.provider.SearchSuggestionProvider;
import com.a30corner.geopicker.util.AddressUtil;
import com.a30corner.geopicker.util.AddressUtil.OnLookAddressListener;
import com.a30corner.geopicker.util.FormatUtil;

import java.util.List;


public class GeoPickerActivity extends MapActivity {
    private static final String TAG = "GeoPickerActivity";
    private MapView mMapView;
    private FallInOverlay fallOverlay;
    private DragableOverlay dragOverlay;
    private MyLocationOverlay mMyLocationOverlay;
    private AndroidPin mPin;
    private TextSwitcher locText;


    private final int DIALOG_PROGRESS = 0;
    private static final int DIALOG_BOOKMARK =1;



    private static final int REQUEST_BOOKMARK_LIST = 11;
    // private static final String COPY_PREF = "copy_pref";
    private boolean isCreate;
    private Handler pHandler;
    private FormatUtil mFormatUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.main);

        
        mFormatUtil = new FormatUtil(this);
        
        HandlerThread pThread = new HandlerThread(TAG);
        pThread.setPriority(Thread.MIN_PRIORITY);
        pThread.start();
        pHandler = new Handler(pThread.getLooper());


        locText = (TextSwitcher) findViewById(R.id.text_location);
        locText.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return getLayoutInflater()
                        .inflate(R.layout.coordinations, null).findViewById(
                                R.id.text);
            }

        });


        locText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFormatUtil.toggleFormat();
                setLocText(mPin.mPoint, true);
            }
        });


        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setBuiltInZoomControls(true);
        mPin = new AndroidPin(this);
        Intent intent = getIntent();
        mPin.setPosition(mMapView.getMapCenter());


        fallOverlay = new FallInOverlay(mMapView);
        fallOverlay.setOnEndListener(new OnEndListener() {
            @Override
            public void onEnd() {
                mMapView.getOverlays().remove(fallOverlay);
                mMapView.getOverlays().add(dragOverlay);
            }
        });

        dragOverlay = new DragableOverlay(mPin, mMapView);
        dragOverlay.setOnLocationChangeListener(new OnLocationChangeListener() {
            @Override
            public void onChage(GeoPoint gp) {
                setLocText(gp, false);
            }
        });

        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);

        mMapView.getOverlays().add(mMyLocationOverlay);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        setLocText(mPin.mPoint, false);

        mMapView.getOverlays().add(fallOverlay);
        isCreate = true;

    }



    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && isCreate) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fallOverlay.play(mPin);
                }
            }, 10);
            isCreate = false;
        }
    }

    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (preferences.getBoolean("pref_compass", false)) {
            mMyLocationOverlay.enableCompass();
        } else {
            mMyLocationOverlay.disableCompass();
        }

        boolean showSatellite = preferences.getBoolean("pref_satellite", false);

        mMapView.setSatellite(showSatellite);



        mMyLocationOverlay.enableMyLocation();

    }

    protected void onPause() {
        super.onPause();
        mMyLocationOverlay.disableMyLocation();

    }


    protected void onDestroy() {

        super.onDestroy();
        pHandler.getLooper().quit();

    }

    private Runnable prevRunnable;

    private void setLocText(final GeoPoint gp, final boolean change) {
        pHandler.removeCallbacks(prevRunnable);
        prevRunnable = new Runnable() {
            @Override
            public void run() {
                final String ftext =  mFormatUtil.getLocText(gp);
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (change) {
                            locText.setText(ftext);
                        } else {
                            ((TextView) locText.getCurrentView())
                                    .setText(ftext);
                        }
                    }
                });
            }
        };

        pHandler.postDelayed(prevRunnable, 20);

    }

    /* ---------------------dialog---------------------------- */
    @Override
    protected Dialog onCreateDialog(int id, Bundle b) {
        switch (id) {
        case DIALOG_PROGRESS:
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.msg_loading));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        case DIALOG_BOOKMARK:
            final EditText input = new EditText(this);
            return new AlertDialog.Builder(this)
            .setTitle(R.string.title_bookmark)
            .setView(input)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            String name = input.getText().toString();

                            BookmarkHelper helper = new BookmarkHelper(GeoPickerActivity.this);
                            int lon = mPin.mPoint.getLongitudeE6();
                            int lat = mPin.mPoint.getLatitudeE6();
                            Bookmark bm = new Bookmark(name, "address", lon, lat);
                            helper.addBookmark(bm);   
                        }
                    })
            .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            // Do nothing.
                        }
                    }).show();
            
        
        }
        return null;
    }



   


    /* ---------------------menu---------------------------- */
    private final int MENU_MYLOCATION = 0;
    private final int MENU_COPY = 1;
    private final int MENU_SEARCH = 2;
    private final int MENU_ADDRESS = 4;
    private final int MENU_SETTING = 6;
    private final int MENU_BOOKMARK = 7;
    private final int MENU_BOOKMARK_LIST = 8;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_MYLOCATION, MENU_MYLOCATION,
                getResources().getString(R.string.menu_mylocation));
        menu.add(0, MENU_COPY, MENU_COPY,
                getResources().getString(R.string.menu_copy));
        menu.add(0, MENU_ADDRESS, MENU_ADDRESS,
                getResources().getString(R.string.menu_address));
        menu.add(0, MENU_SEARCH, MENU_SEARCH,
                getResources().getString(R.string.menu_search));

        menu.add(0, MENU_SETTING, MENU_SETTING,
                getResources().getString(R.string.menu_setting));
        menu.add(0, MENU_BOOKMARK, MENU_BOOKMARK, R.string.menu_bookmark);
        menu.add(0, MENU_BOOKMARK_LIST, MENU_BOOKMARK_LIST, R.string.menu_bookmark_list);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == MENU_ADDRESS) {
            AddressUtil addressUtil = new AddressUtil();
            showDialog(DIALOG_PROGRESS);
            addressUtil.lookup(this, mPin.mPoint, new OnLookAddressListener() {
                @Override
                public void onFailure() {
                    dismissDialog(DIALOG_PROGRESS);
                    showText(R.string.msg_lookup_failure);
                }

                @Override
                public void onSuccess(final String address) {

                    dismissDialog(DIALOG_PROGRESS);
                    new AlertDialog.Builder(GeoPickerActivity.this)
                            .setMessage(address)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton(R.string.menu_copy,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            clipboard.setText(address);
                                            showText(R.string.msg_copy_address_to_clipboard);
                                        }
                                    }).show();
                }

            });

        } else if (item.getItemId() == MENU_SEARCH) {
            onSearchRequested();
        } else if (item.getItemId() == MENU_MYLOCATION) {
            final GeoPoint gp = mMyLocationOverlay.getMyLocation();
            if (gp == null) {
                showText(R.string.msg_mylocation_not_available);
            } else {
                mMapView.getController().animateTo(gp, new Runnable() {
                    public void run() {
                        mPin.setPosition(gp);
                    }
                });

            }
        } else if (item.getItemId() == MENU_COPY) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String text = mFormatUtil.getLocCopy(mPin.mPoint);
            clipboard.setText(text);
            showText(R.string.msg_copy_to_clipboard);

        } else if (item.getItemId() == MENU_BOOKMARK) {
            showDialog(DIALOG_BOOKMARK);

           
            // TODO i18n
        
        } else if (item.getItemId() == MENU_BOOKMARK_LIST) {
            Intent intent = new Intent();
            intent.setClass(this, BookmarkListActivity.class);
            startActivityForResult(intent, REQUEST_BOOKMARK_LIST);


        } else if (item.getItemId() == MENU_SETTING) {
            Intent intent = new Intent();
            intent.setClass(this, SettingActivity.class);
            startActivity(intent);

        }

        return true;
    }


    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        final String queryString = newIntent
                .getStringExtra(SearchManager.QUERY);
        if (queryString != null) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                    this, SearchSuggestionProvider.AUTHORITY,
                    SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(queryString, null);
            showDialog(DIALOG_PROGRESS);
            new Thread() {
                public void run() {
                    Geocoder geoCoder = new Geocoder(GeoPickerActivity.this);
                    try {
                        List<Address> addressList = geoCoder
                                .getFromLocationName(queryString, 1);
                        if (addressList.size() == 1) {
                            Address address = addressList.get(0);
                            int lat = (int) (address.getLatitude() * 1e6);
                            int lon = (int) (address.getLongitude() * 1e6);
                            final GeoPoint gp = new GeoPoint(lat, lon);

                            mMapView.getController().animateTo(gp,
                                    new Runnable() {
                                        public void run() {
                                            mPin.setPosition(gp);
                                        }
                                    });
                        }

                    } catch (Exception e) {
                        Log.w(TAG, e.getMessage(), e);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showText(R.string.msg_reverse_failure);
                            }
                        });

                    } finally {
                        dismissDialog(DIALOG_PROGRESS);
                    }
                }
            }.start();

        }
    }

    private void showText(int resId) {
        showText(getString(resId));
    }

    private void showText(String text) {
        Toast toast = Toast.makeText(GeoPickerActivity.this, text, 8000);
        toast.setMargin(0f, 0.8f);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode ==  REQUEST_BOOKMARK_LIST && resultCode == Activity.RESULT_OK){
            int lat = data.getIntExtra("lat", 0);
            int lon = data.getIntExtra("lon", 0);
            final GeoPoint gp = new GeoPoint(lat,lon);
            mMapView.getController().animateTo(gp, new Runnable() {
                public void run() {
                    mPin.setPosition(gp);
                }
            });
        }
        
        
    }
    

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }



}
