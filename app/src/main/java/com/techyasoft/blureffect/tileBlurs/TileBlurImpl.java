package com.techyasoft.blureffect.tileBlurs;

import android.graphics.Bitmap;

import com.techyasoft.blureffect.tileBlurs.TileBlur;

public class TileBlurImpl extends TileBlur {
    private final TileListener listener;

    public TileBlurImpl(Bitmap srcBitmap,TileListener listener) {
        super(srcBitmap);
        this.listener = listener;
    }

    @Override
    public void readyBitmap(Bitmap bitmap) {
        listener.onResourceReady(bitmap);
    }

    public static interface TileListener{
        void onResourceReady(Bitmap bitmap);
    }
}
