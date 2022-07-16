package com.techyasoft.blureffect.tileBlurs;



import android.graphics.Bitmap;

import android.os.Bundle;

import android.util.Log;
import android.widget.SeekBar;



import com.techyasoft.blureffect.databinding.ActivityTileBlurBinding;



public class TileBlurActivity extends BaseActivity implements BaseActivity.BaseListener {
    private static final String TAG = "TileBlurActivity";

    private ActivityTileBlurBinding binding;
    private boolean firstTime = true;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTileBlurBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());






        // Blur Mode OFF
        binding.blurOff.setOnClickListener( v -> binding.img.blurModeOff()
        );

        // Blur Mode ON
        binding.blurOn.setOnClickListener(v -> binding.img.blurModeOn()
        );

        // set Whole Image to Normal
        binding.blurNormal.setOnClickListener(v -> binding.img.setNormalImage());

        // set Whole Image to Blur
        binding.blurWhole.setOnClickListener(v -> binding.img.setBlurImage());

        binding.undoText.setOnClickListener(v -> binding.img.undo());


        //Change Circle Size
        binding.sizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.img.changeCircleSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Change Offset
        binding.offsetSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.img.changeOffset(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Change density of blur image
        binding.powerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateImage(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        binding.img.setUndoListener(size -> {
            Log.d(TAG, "onUndo: "+size);
            binding.undoText.setText(String.valueOf(size));
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstTime){
            setListener(this);
            launcher.launch("image/*");
            firstTime = false;
        }

    }





    private void updateImage(int radius) {
        Log.d(TAG, "updateImage: "+radius);
        cancelPreTask();
        mFuture = mDispatcher.submit(new BaseActivity.BlurTask(binding.img.orgBitmap,mProcessor, radius) {
            @Override
            void onBlurSuccess(final Bitmap bitmap) {
                if (!isFinishing() && bitmap != null) {
                    binding.img.changeDensity(bitmap);
                }
            }
        });
    }


    @Override
    public void onLoad(Bitmap resource,Bitmap blur) {
        binding.img.post(() ->
                binding.img.init(resource,blur));
    }


}