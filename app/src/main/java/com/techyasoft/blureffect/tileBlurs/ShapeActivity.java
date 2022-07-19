package com.techyasoft.blureffect.tileBlurs;



import android.graphics.Bitmap;
import android.os.Bundle;


import com.techyasoft.blureffect.customViews.ShapeBlurView;
import com.techyasoft.blureffect.databinding.ActivityShapeBinding;



public class ShapeActivity extends BaseActivity implements BaseActivity.BaseListener {
    private static final String TAG="ShapeActivity";
    private ActivityShapeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShapeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListener(this);
        launcher.launch("image/*");

        binding.circle.setOnClickListener(v -> {
            binding.img.changeShapeMode(ShapeBlurView.ShapeMode.CIRCLE);
        });

        binding.rectangle.setOnClickListener(v -> {
            binding.img.changeShapeMode(ShapeBlurView.ShapeMode.RECTANGLE);
        });

        binding.heart.setOnClickListener(v -> {
            binding.img.changeShapeMode(ShapeBlurView.ShapeMode.HEART);
        });

        binding.star.setOnClickListener(v -> {
            binding.img.changeShapeMode(ShapeBlurView.ShapeMode.STAR);
        });
    }



    @Override
    public void onLoad(Bitmap resource, Bitmap blur) {
        binding.img.post(() ->
                binding.img.init(resource,blur));
    }
}