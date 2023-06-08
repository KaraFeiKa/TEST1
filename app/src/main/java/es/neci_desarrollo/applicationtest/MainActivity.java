package es.neci_desarrollo.applicationtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.net.ContentHandler;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListenerInterface {
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    TextView latitude_res;
    TextView longitude_res;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getLocation();

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},100);
        }

    }



    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,myLocationListener);

        } catch (Exception e) {

        }
    }


    private void init()
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        latitude_res = findViewById(R.id.Res_latitude);
        longitude_res = findViewById(R.id.Res_longitude);
    }

    @Override
    public void onLocationChanged(Location location) {
                if (location != null) {
                    latitude_res.setText(String.valueOf(location.getLatitude()));
                    longitude_res.setText(String.valueOf(location.getLongitude()));
        }
    }
}