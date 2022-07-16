package com.techyasoft.blureffect.tileBlurs;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hoko.blur.HokoBlur;
import com.hoko.blur.api.IBlurBuild;
import com.hoko.blur.processor.BlurProcessor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private static final float SAMPLE_FACTOR = 4.0f;
    private int screenWidth;
    protected volatile Future mFuture;
    protected final ExecutorService mDispatcher = Executors.newSingleThreadExecutor();
    protected IBlurBuild mBlurBuilder;
    protected BlurProcessor mProcessor;
    protected BaseListener listener;

    protected void setListener(BaseListener listener){
        this.listener = listener;
    }

    ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        try {
            Log.d(TAG, "onActivityResult: "+result.getAuthority());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
            Log.d(TAG, "onActivityResult: start time org stats "+bitmap.getWidth()+" "+bitmap.getHeight());
            createBitmap(bitmap,result);
        }catch (NullPointerException | IOException e){
            Log.d(TAG, "onActivityResult: "+e.getMessage());
        }

    });

    public interface BaseListener{
        void onLoad(Bitmap resource,Bitmap blur);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        mBlurBuilder = HokoBlur.with(this).sampleFactor(SAMPLE_FACTOR);
        mBlurBuilder.scheme(HokoBlur.SCHEME_JAVA);
        mBlurBuilder.mode(HokoBlur.MODE_STACK);
        mProcessor = mBlurBuilder.processor();

    }

    protected void createBitmap(Bitmap bitmap, Uri uri){
        Glide.with(this).asBitmap().load(uri).apply(new RequestOptions()
                        .override(screenWidth,bitmap.getHeight()))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Log.d(TAG, "onLoadFailed: ");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "onResourceReady:Glide "+resource.getWidth()+" "+resource.getHeight());

                        cancelPreTask();
                        mFuture = mDispatcher.submit(new BlurTask(resource,mProcessor,50) {
                            @Override
                            void onBlurSuccess(Bitmap bitmap) {
                                Log.d(TAG, "onBlurSuccess: "+bitmap.getWidth());
                                listener.onLoad(resource,bitmap);
                            }
                        });
                        return true;
                    }
                }).submit();
    }

    protected void cancelPreTask() {
        if (mFuture != null && !mFuture.isCancelled() && !mFuture.isDone()) {
            mFuture.cancel(false);
            mFuture = null;
        }
    }

    public abstract static class BlurTask implements Runnable {
        private final Bitmap bitmap;
        private final BlurProcessor blurProcessor;
        private final int radius;

        BlurTask(Bitmap bitmap, BlurProcessor blurProcessor, int radius) {
            this.bitmap = bitmap;
            this.blurProcessor = blurProcessor;
            this.radius = radius;
        }

        @Override
        public void run() {
            if (bitmap != null && !bitmap.isRecycled() && blurProcessor != null) {
                blurProcessor.radius(radius);
                onBlurSuccess(blurProcessor.blur(bitmap));
            }
        }

        abstract void onBlurSuccess(Bitmap bitmap);
    }
}
