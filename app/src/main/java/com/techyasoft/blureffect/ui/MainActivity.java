package com.techyasoft.blureffect.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;

import com.techyasoft.blureffect.CacheFragment;
import com.techyasoft.blureffect.R;
import com.techyasoft.blureffect.ui.filter.FilterFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomePageContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();
    private HomePageContract.HomePresenter mHomePresenter;

    private Button btnPickPhoto, btnMosaicVertical, btnMosaicHorizontal, btnClear;
    private TileView ivMain;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        if (savedInstanceState != null) {
            CacheFragment<HomePageContract.HomePresenter> cacheFragment = (CacheFragment) getFragmentManager().findFragmentByTag(TAG);
            if (cacheFragment != null) {
                mHomePresenter = cacheFragment.getData();
                setupPresenter(mHomePresenter, true);

            }
        } else {
            mHomePresenter = new HomePresenterImpl();
            setupPresenter(mHomePresenter, false);

            CacheFragment<HomePageContract.HomePresenter> cacheFragmentNew = new CacheFragment<>();
            cacheFragmentNew.setData(mHomePresenter);
            getFragmentManager().beginTransaction().add(cacheFragmentNew, TAG).commit();
        }
    }

    private void initViews() {
        btnPickPhoto = (Button) findViewById(R.id.btnPickPhoto);
        btnPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHomePresenter.onPickPhotoButtonClicked(v);
            }
        });

        btnMosaicVertical = (Button) findViewById(R.id.btnMosaicVertical);
        btnMosaicVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 25/07/2016  adds logic to draw mosaic
//                Bitmap mosaicBitmap = ivMain.drawMosaic();
//                mHomePresenter.refreshBitmap(mosaicBitmap);
//                ivMain.addSomeFutures();

                ivMain.renderTiles(TileView.RenderDirection.VERTICAL);
            }
        });


        btnMosaicHorizontal = (Button) findViewById(R.id.btnMosaicHorizontal);
        btnMosaicHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivMain.renderTiles(TileView.RenderDirection.HORIZONTAL);
            }
        });

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivMain.clearEffects();
            }
        });

        ivMain = (TileView) findViewById(R.id.ivMain);


        final List<String> filterNames = new ArrayList<>();
        for (FilterFactory.FilterType filterType : FilterFactory.FilterType.values()) {
            filterNames.add(filterType.toString());
        }

        filterSpinner = (Spinner) findViewById(R.id.spinner);
        filterSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.layout_filter_spinner_item, R.id.textView, filterNames));
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FilterFactory.FilterType filterType = FilterFactory.FilterType.valueOf(filterNames.get(position));
                if (filterType != null) {
                    ivMain.setImageFilterType(filterType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void setupPresenter(HomePageContract.HomePresenter homePresenter, boolean isConfigurationChange) {
        if (homePresenter == null) {
            throw new IllegalStateException("Failed to retrieve Presenter.");
        }
        homePresenter.setView(this);
        homePresenter.onViewCreated(isConfigurationChange);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ivMain.resume();
        mHomePresenter.onViewResumed();
    }

    @Override
    protected void onPause() {
        ivMain.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mHomePresenter.onViewDestroyed();
        ivMain.cancelAllTasks();
        super.onDestroy();
    }

    @Override
    public Activity activity() {
        return this;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mHomePresenter.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void displayImage(Bitmap bitmap) {
        ivMain.setBackgroundBitmap(bitmap);
    }
}
