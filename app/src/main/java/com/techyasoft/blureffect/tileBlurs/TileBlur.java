package com.techyasoft.blureffect.tileBlurs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.techyasoft.blureffect.util.BitmapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;


public abstract class TileBlur {
    private static final String TAG="TileBlur";
    private final Bitmap srcBitmap;
    private final Canvas canvas;
    private final ExecutorService mExecutorService = (ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR;
    private final BlockingDeque<Future<Collection<Pair<Rect, Bitmap>>>> mDrawingQueue = new LinkedBlockingDeque<>();

    public abstract void readyBitmap(Bitmap bitmap);

    public TileBlur(Bitmap srcBitmap) {
        this.srcBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),srcBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        canvas = new Canvas(this.srcBitmap);
        canvas.drawBitmap(srcBitmap,0,0,null);
        createListOfRectangles();
    }

    public void createListOfRectangles(){

        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        Log.d(TAG, "createListOfRectangles:bitmap "+width+" "+height);

        int GRID_WIDTH = 64;
        int horCount = (int) Math.ceil(width / (float) GRID_WIDTH);
        int GRID_HEIGHT = 64;
        int verCount = (int) Math.ceil(height / (float) GRID_HEIGHT);

        Log.d(TAG, "createListOfRectangles:hor x ver  "+horCount+" "+verCount);

        Rect[][] mRectangles = new Rect[horCount][verCount];

        Log.d(TAG, "createListOfRectangles: mRectangles size "+mRectangles.length);

        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int left = GRID_WIDTH * horIndex;
                int top = GRID_HEIGHT * verIndex;
                int right = left + GRID_WIDTH;
                if (right > width) {
                    right = width;
                }
                int bottom = top + GRID_HEIGHT;
                if (bottom > height) {
                    bottom = height;
                }
                mRectangles[horIndex][verIndex] = new Rect(left, top, right, bottom);
            }
        }
        renderVertically(mRectangles);
    }

    public Callable<Collection<Pair<Rect, Bitmap>>>createPairList(Rect[] rectArray){
        Log.d(TAG, "createPairList: size "+rectArray.length);
        if (rectArray.length < 1) {
            return null;
        }

        return () -> {
            List<Pair<Rect, Bitmap>> pairList = new ArrayList<>();

            for (Rect tileRect : rectArray) {
                    Bitmap tileBitmap = createTile(tileRect, srcBitmap);
                    pairList.add(new Pair<>(tileRect, tileBitmap));

            }
            return pairList;

        };
    }

    private void addTask(Callable<Collection<Pair<Rect, Bitmap>>> callable){
        Future<Collection<Pair<Rect, Bitmap>>> future = mExecutorService.submit(callable);
        mDrawingQueue.addLast(future);
    }

    public void renderVertically(Rect[][] mRectangle) {

        for (Rect[] rect : mRectangle) {
            Callable<Collection<Pair<Rect, Bitmap>>> callable = createPairList(rect);
            addTask(callable);
        }
        startEngine();
    }


    private Bitmap createTile(Rect tileRect, Bitmap srcBitmap) {
        Bitmap tileBitmap = Bitmap.createBitmap(tileRect.width(), tileRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tileBitmap);
        int color = BitmapUtils.getAverageColor(srcBitmap, tileRect);
        Paint paint = new Paint();
        paint.setColor(color);
        Rect newRect = new Rect(0, 0, tileRect.width(), tileRect.height());
        canvas.drawRect(newRect, paint);
        return tileBitmap;
    }

    public void startEngine(){
        new Thread(() -> {
            while (!mDrawingQueue.isEmpty()) {
                try {
                    Future<Collection<Pair<Rect, Bitmap>>> future = mDrawingQueue.takeFirst();
                    Collection<Pair<Rect, Bitmap>> collection = future.get();
                    for (Pair<Rect, Bitmap> rectBitmapPair : collection) {
                        if (rectBitmapPair != null && rectBitmapPair.first != null && rectBitmapPair.second != null) {
                            Rect rect = rectBitmapPair.first;
                            Bitmap bitmap = rectBitmapPair.second;
                            canvas.drawBitmap(bitmap, null, rect, null);
                        }
                    }
                } catch (CancellationException | InterruptedException | ExecutionException e) {
                    // the Callable is cancelled.
                    e.printStackTrace();
                }
            }
            readyBitmap(srcBitmap);
        }).start();
    }
}
