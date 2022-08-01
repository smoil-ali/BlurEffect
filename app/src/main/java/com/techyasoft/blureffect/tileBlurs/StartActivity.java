package com.techyasoft.blureffect.tileBlurs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.techyasoft.blureffect.databinding.ActivityStartBinding;


public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStartBinding binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.letsStart.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this,HomeActivity.class);
            startActivity(intent);
        });


        ParentClass obj = new TestClass();
        obj.f1();
        obj.f2();
        obj.f4();
    }
}