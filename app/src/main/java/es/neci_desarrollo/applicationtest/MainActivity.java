package es.neci_desarrollo.applicationtest;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import es.neci_desarrollo.applicationtest.Fragments.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    TelephonyManager telephonyManager;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    FrameLayout frameLayout;
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            tabLayout = findViewById(R.id.TabLayout);
            viewPager2 = findViewById(R.id.viewPager);
            viewPagerAdapter = new ViewPagerAdapter(this, telephonyManager);
            viewPager2.setAdapter(viewPagerAdapter);
            frameLayout= findViewById(R.id.frameLayout);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager2.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                    viewPager2.setCurrentItem(tab.getPosition());
                    Store.selectedTab = tab.getPosition();
                    Log.d("store", String.valueOf(Store.selectedTab));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    viewPager2.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                }
            });

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    Objects.requireNonNull(tabLayout.getTabAt(position)).select();
                    super.onPageSelected(position);
                }
            });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.MODIFY_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MODIFY_PHONE_STATE}, 100);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();


        Log.d("store", "try to select from store"+String.valueOf(Store.selectedTab));
        TabLayout.Tab tab = tabLayout.getTabAt(Store.selectedTab);
        tab.select();

    }
}
