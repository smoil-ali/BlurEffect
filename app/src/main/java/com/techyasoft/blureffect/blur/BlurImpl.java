package com.techyasoft.blureffect.blur;


import android.graphics.Bitmap;
import android.util.Log;


public class BlurImpl extends Blur{
    private static final String TAG = "BlurImpl";
    private final BlurListener listener;

    public BlurImpl(Bitmap source,int radius,BlurListener listener) {
        super(source,radius);


        this.listener = listener;
    }


    @Override
    void ready(Bitmap blurBitmap) {
        listener.onMyResourceReady(blurBitmap);
    }

    public interface BlurListener{
        void onMyResourceReady(Bitmap blurBitmap);
    }
}
