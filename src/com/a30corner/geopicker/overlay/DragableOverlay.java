package com.a30corner.geopicker.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import com.a30corner.geopicker.AndroidPin;
import com.a30corner.geopicker.R;


public class DragableOverlay extends Overlay {
    private static final String TAG = "DragableOverlay";
    private AndroidPin mPin;
    private final Rect mTouchableBounds = new Rect();
    private static final int MINIMUM_TOUCH_DIAMETER = ViewConfiguration
            .getTouchSlop();

    /* jump */
    private long mStartTime;
    private int detalHeight;
    private final int MAX_HEIGHT = 30;
    private float mDurationReciprocal = 0.002f;
    private boolean isTouching;
    private MapView mMapView;
    private Projection mProjector;
    private int initHeight = 0;

    /*target*/
    private boolean showTarget;
    private Drawable targetDrawable;
    
    /* recenter*/
    private boolean needRecenter;
    private GeoPoint newPosition;
    
    /*Location TextView*/
    private OnLocationChangeListener mListener;
    
    private Vibrator mVibrator;
    public DragableOverlay(AndroidPin pin, MapView mapview) {
        mPin = pin;
        mMapView = mapview;
        mProjector = mapview.getProjection();
        mVibrator = (Vibrator)mapview.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        
        
        newPosition = pin.mPoint;
        targetDrawable = mapview.getContext().getResources().getDrawable(R.drawable.target);
        int width = targetDrawable.getIntrinsicWidth();
        int height = targetDrawable.getIntrinsicHeight();
        int w2 = width / 2;
        int h2 = height / 2;
       
        targetDrawable.setBounds(-w2, -h2, w2,h2);
       
    }


    private Rect getTouchableBounds(Rect bounds) {

        int w = bounds.width();
        int h = bounds.height();
        if (w >= MINIMUM_TOUCH_DIAMETER && h >= MINIMUM_TOUCH_DIAMETER) {
            return bounds;
        } else {
            int cx = bounds.centerX();
            int cy = bounds.centerY();
            int touchW = Math.max(MINIMUM_TOUCH_DIAMETER, w);
            int touchL = cx - touchW / 2;
            int touchH = Math.max(MINIMUM_TOUCH_DIAMETER, h);
            int touchT = cy - touchH / 2;
            mTouchableBounds.set(touchL, touchT, touchL + touchW, touchT
                    + touchH);
            return mTouchableBounds;
        }
    }


    private boolean checkItemAtLocation(int hitX, int hitY, MapView mapView) {
        Point p = new Point();
        mapView.getProjection().toPixels(mPin.mPoint, p);

        int offsetX = hitX - p.x;
        int offsetY = hitY - p.y;
        Rect touchBound  = mPin.copyBounds();
        touchBound.offset(0, -mPin.getBounds().height()/3);
        
        
        Rect bounds = getTouchableBounds(touchBound);
        

        return bounds.contains(offsetX, offsetY);

    }

    private int offsetX,offsetY;
    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        boolean result = false;
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            isTouching = true;
            if (checkItemAtLocation(x, y, mapView)) {
                mVibrator.vibrate(40);
                mPin.setPressed(true);
                
                mProjector.toPixels(mPin.mPoint, reusePoint);
                
                
                offsetX = reusePoint.x-x;
                offsetY = reusePoint.y-y;
            } else {
                mHandler.removeMessages(PIN_DOWN);
                mHandler.removeMessages(PIN_UP);
                mHandler.removeMessages(TARGET_DISPLAY);
                mHandler.removeMessages(RE_CENTER);
                
                
                initHeight = Math.max(0,mPin.getHeight());
                detalHeight = MAX_HEIGHT - initHeight;
                mStartTime = SystemClock.uptimeMillis();
                mHandler.sendEmptyMessageDelayed(PIN_UP, 50);
                showTarget = true;

            }
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {

            if (mPin.isPressed()) {
                newPosition = mProjector.fromPixels(x+offsetX, y + offsetY);
                needRecenter = true;

                result = true;
            } else {
                if (reusePoint != null) {
                    newPosition = mProjector.fromPixels(
                            reusePoint.x, reusePoint.y);
                    
                }
            }

           
        } else if (e.getAction() == MotionEvent.ACTION_UP
                || e.getAction() == MotionEvent.ACTION_CANCEL) {
            isTouching = false;
            if (mPin.isPressed()) {
                mPin.setPressed(false);
            }

            mHandler.sendEmptyMessage(RE_CENTER);
            
            offsetX = offsetY= 0;
        }
        return result;
    }


    private Point reusePoint = new Point();
    private boolean isFistDraw = true;
    @Override
    public void draw(Canvas canvas, MapView mapview, boolean shadow) {


        if (isFistDraw) {
            isFistDraw = false;
            mProjector.toPixels(mPin.mPoint, reusePoint);
        }

        if (mPin.isPressed() || needRecenter) {            
         
            mProjector.toPixels(newPosition, reusePoint);
        } else {
            newPosition = mProjector.fromPixels(reusePoint.x,
                    reusePoint.y);
        }

        if(showTarget && !shadow){
            drawAt(canvas, targetDrawable, reusePoint.x, reusePoint.y, false);
        }
        
        
        drawAt(canvas, mPin, reusePoint.x, reusePoint.y, shadow);
        
        if(mListener !=null && !newPosition.equals(mPin.mPoint)){
            mPin.mPoint = newPosition;
            mListener.onChage(newPosition);
        }
        
    }


    public void setOnLocationChangeListener(OnLocationChangeListener listener){
        mListener = listener;
    }
    
    
    /*animation */
    private final int PIN_UP = 0;
    private final int PIN_DOWN = 1;
    private final int TARGET_DISPLAY = 2;
    private final int  RE_CENTER=3;
    private Handler mHandler = new Handler() {

        private Point reusePoint1 = new Point();
        private AccelerateInterpolator accInterpolator = new AccelerateInterpolator();
        private DecelerateInterpolator deInterpolator = new DecelerateInterpolator();

        @Override
        public void handleMessage(Message msg) {
            int timePassed = (int) (SystemClock.uptimeMillis() - mStartTime);
            float y = timePassed * mDurationReciprocal;
            int height;
            switch (msg.what) {
            case PIN_UP:
             
                if (mPin.getHeight() >= 0 && mPin.getHeight() < MAX_HEIGHT) {
                    height = Math.round(deInterpolator.getInterpolation(y)
                            * detalHeight);
                    height = Math.min(height, MAX_HEIGHT)
                            + initHeight;
                    mPin.setHeight(height);
                    if(height >0){ //fix loop call
                        invalidatePin();
                        sendEmptyMessageDelayed(PIN_UP, 50);
                    }
               
                } else {
                    if (isTouching) {
                        sendEmptyMessageDelayed(PIN_UP, 500);
                    } else { //revert down
                        mStartTime = SystemClock.uptimeMillis();
                        detalHeight = mPin.getHeight();
                        sendEmptyMessageDelayed(PIN_DOWN, 50);
                    }
                }
                break;
            case PIN_DOWN:
                if (!hasMessages(PIN_UP)) {
                    height = Math.round(accInterpolator.getInterpolation(y)
                            * detalHeight);
                    height = Math.max(0, detalHeight - height);
                    mPin.setHeight(height);
                    if (height != 0) {
                        sendEmptyMessageDelayed(PIN_DOWN, 50);
                    } else { //finish
                      sendEmptyMessageDelayed(TARGET_DISPLAY,200);
                      sendEmptyMessage(RE_CENTER);
                    }
                    invalidatePin();
                }
                break;
                
            case TARGET_DISPLAY:
                showTarget = false;
                invalidatePin();
                break;
            case RE_CENTER:
                if(needRecenter){
                    mMapView.getController().animateTo(mPin.mPoint,new Runnable(){
                        public void run(){
                            needRecenter=false;
                        }
                    });
                }
                break;    
            }

        }
        
        private void invalidatePin(){

            //TODO: performance
            mProjector.toPixels(mPin.mPoint, reusePoint1);
            Rect rect = mPin.copyBounds();
            rect.offsetTo(reusePoint1.x-rect.width()/2, reusePoint1.y-rect.height()-20);
            rect.top -=mPin.getHeight();
            rect.right +=rect.width();
            rect.bottom +=30;
            mMapView.invalidate(rect);
        }
    };

 
    
    
    public interface OnLocationChangeListener{
        public void onChage(GeoPoint gp);
    }

}
