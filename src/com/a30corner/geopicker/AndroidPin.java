package com.a30corner.geopicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;

public class AndroidPin extends Drawable {
    public Drawable mDrawable;
    public GeoPoint mPoint;
    private int mHeight = 0;

    public AndroidPin(Context context) {
        mDrawable = boundCenterBottom(context.getResources().getDrawable(
                R.drawable.android_pin));
        boundCenterBottom(this);
        
    }

    public void setHeight(int height) {
        if(height <0){
            height =0;
        }
        mHeight = height;
    }

    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }


    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }
    
    
    

    public void setPosition(int latE6, int lonE6) {
        mPoint = new GeoPoint(latE6, lonE6);
    }

    public void setPosition(GeoPoint gp) {
        mPoint = gp;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setPressed(boolean pressed) {
        if (pressed) {
            setState(new int[] {android.R.attr.state_pressed});
        } else {
            setState(new int[] {});
        }

    }

    public boolean setState(int stateSet[]) {
        super.setState(stateSet);
        return mDrawable.setState(stateSet);
    }



    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(0, -mHeight);
        mDrawable.getCurrent().draw(canvas);
        canvas.restore();
    }

    private Drawable boundCenterBottom(Drawable pin) {
        int width = pin.getIntrinsicWidth();
        int w2 = width / 2;
        int height = pin.getIntrinsicHeight();
        pin.setBounds(-w2, 1 - height, width - w2, 1);
        return pin;
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public void setAlpha(int i) {
        mDrawable.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorfilter) {
        mDrawable.setColorFilter(colorfilter);
    }

    public boolean isPressed() {
        for(int s:getState()){
            if(s==android.R.attr.state_pressed){
                return true;
            }
        }
        return   false;
    }


}
