package com.a30corner.geopicker;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.a30corner.geopicker.provider.Bookmark;
import com.a30corner.geopicker.provider.BookmarkHelper;
import com.a30corner.geopicker.util.FormatUtil;

import java.util.ArrayList;

public class BookmarkListActivity extends ListActivity {
    private BookmarkHelper mBKHelper;
    private BookmarkAdapter mAdapter;
    private ArrayList<Bookmark> mData;
    private FormatUtil mFormatUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bookmark);
        mBKHelper = new BookmarkHelper(getApplicationContext());
        mFormatUtil = new FormatUtil(this);

        mData = mBKHelper.getAllBookmarks();
        mAdapter = new BookmarkAdapter(this);
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();



    }


    private class BookmarkAdapter extends ArrayAdapter<Bookmark> implements
            OnItemClickListener {

        public BookmarkAdapter(Context context) {

            super(context, R.layout.bookmark_item, R.id.text_name, mData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.bookmark_item, null);
            }


            TextView title = (TextView) convertView
                    .findViewById(R.id.text_name);
            TextView location = (TextView) convertView
                    .findViewById(R.id.text_location);
            ImageView delete = (ImageView) convertView
                    .findViewById(R.id.delete);


            final Bookmark bm = getItem(position);

            title.setText(bm.name);
            
            String locText = mFormatUtil.getLocCopy(bm.getPoint());
      
            
            location.setText(locText);
            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirm(bm);

                }
            });


            return convertView;
        }

        @Override
        public void remove(Bookmark object) {
            mData.remove(object);
            notifyDataSetChanged();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            setResult(Activity.RESULT_OK);


            Bookmark bk = getItem(position);
            Intent result = new Intent();
            result.putExtra("lat", bk.lat);
            result.putExtra("lon", bk.lon);

            setResult(Activity.RESULT_OK, result);
            finish();

        }

    }


    private void showDeleteConfirm(final Bookmark bm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String delConfirm = getResources().getString(R.string.msg_delete);
        builder.setMessage(String.format(delConfirm, bm.name))
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mBKHelper.delBookmark(bm);
                                mAdapter.remove(bm);

                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
