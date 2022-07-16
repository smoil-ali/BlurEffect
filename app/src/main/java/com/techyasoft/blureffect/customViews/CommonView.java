package com.techyasoft.blureffect.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public abstract class CommonView extends AppCompatImageView {
    private static final String TAG = "CommonView";
    protected final Context context;
    public Bitmap orgBitmap,orgBlurBitmap;
    public Bitmap bitmap;
    protected Canvas bitmapCanvas;
    protected final Matrix matrix;
    protected int screenWidth;
    protected int screenHeight;
    protected float orgWidth;
    protected float orgHeight;


    protected float orgLeft,orgTop,orgRight,orgBottom,orgLeftBitmap,orgRightBitmap,
            orgTopBitmap,orgBottomBitmap;
    protected float finalScale,transX,transY;

    protected float resRatio  =1f;

    protected float touchX = 0f;
    protected float touchY = 0f;
    protected float bitmapX = 0f;
    protected float bitmapY = 0f;


    protected BitmapShader bitmapShader;

    public CommonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        matrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
    }

    public void fitScreen(){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleX =(float) screenWidth / width;
        float scaleY =(float) screenHeight / height;

        finalScale = Math.min(scaleX,scaleY);

        Log.d(TAG, "fitScreen: final scale "+finalScale);

        float resultX = (screenWidth - (width * finalScale))/2;
        float resultY = (screenHeight - (height * finalScale))/2;

        Log.d(TAG, "fitScreen: transition points x,y "+resultX+" "+resultY);

        matrix.setScale(finalScale,finalScale);
        matrix.postTranslate(resultX,resultY);

        transX = resultX;
        transY = resultY;

        orgWidth = screenWidth - (2 * resultX);
        orgHeight = screenHeight - (2 * resultY);




        Log.d(TAG, "fitScreen: org width and height "+orgWidth+" "+orgHeight+" "+screenWidth+" "+screenHeight);

        setImageMatrix(matrix);


        getStrokeSize();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);


        Log.d(TAG, "onMeasure: "+screenWidth+" "+screenHeight);
    }

    private void getStrokeSize(){
        if (bitmap.getWidth() > bitmap.getHeight()){
            resRatio =(float) screenWidth / bitmap.getWidth();
        }else {
            resRatio = orgHeight / bitmap.getHeight();
        }

        readyToGo();
    }

    abstract void readyToGo();
}
