package com.a30corner.geopicker.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import com.a30corner.geopicker.AndroidPin;


public class FallInOverlay extends Overlay {
    private static final String TAG = "FallInOverlay";
    private AndroidPin mPin;
    private int height=0;
    private int targetHeight = -1;
    private MapView mMapView;
    private long  mStartTime;
    private OnEndListener mOnEndListener;
    public FallInOverlay(MapView mapView) {
        mMapView = mapView;
    }

    public void play(AndroidPin pin) {
        mMapView.setClickable(false);
        mPin = pin;
        mStartTime = SystemClock.uptimeMillis();
        mHandler.sendEmptyMessageDelayed(0,50);
        height=0;
        targetHeight = -1;
    }

    private void onEnd(){
        mMapView.setClickable(true);
        if(mOnEndListener !=null){
            mOnEndListener.onEnd();
        }
    }

    
    private Point reusePoint = new Point();

    @Override
    public void draw(Canvas canvas, MapView mapview, boolean shadow) {
        if(mPin==null) return;
        mapview.getProjection().toPixels(mPin.mPoint, reusePoint);
        if(targetHeight <0){
            targetHeight = reusePoint.y;
        }
       

        mPin.setHeight(Math.max(0, targetHeight-height));
        drawAt(canvas, mPin, reusePoint.x, reusePoint.y, shadow);
    }

    private Handler mHandler = new Handler() {
        private float mDurationReciprocal = 0.002f;
        private int detal;
        private AccelerateInterpolator interpolator = new AccelerateInterpolator();
        public void handleMessage(Message message) {
            if(detal ==0 &&targetHeight>0){
                detal = targetHeight-height;
            }

            int timePassed = (int)(SystemClock.uptimeMillis() - mStartTime);
            float y = timePassed * mDurationReciprocal;
            height = Math.round(interpolator.getInterpolation(y) * detal);
            
            
            mMapView.invalidate();
            if(y<1){
                mHandler.sendEmptyMessageDelayed(0, 50);
            }else{
                this.postDelayed(new Runnable(){
                    public void run(){
                        onEnd();
                    }
                }, 50);
             
            }
         
        }
    };
    
    public void setOnEndListener(OnEndListener listener){
        mOnEndListener = listener;
    }
    
    public interface OnEndListener{
        public void onEnd();
    }


}
