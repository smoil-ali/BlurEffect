package com.techyasoft.blureffect.tileBlurs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.techyasoft.blureffect.R;
import com.techyasoft.blureffect.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.blur.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this,TileBlurActivity.class);
            startActivity(intent);
        });

        binding.shape.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this,ShapeActivity.class);
            startActivity(intent);
        });
    }
}