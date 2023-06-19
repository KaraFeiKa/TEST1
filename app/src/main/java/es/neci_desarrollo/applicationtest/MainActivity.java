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
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements LocationListenerInterface {
    TelephonyManager telephonyManager;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mcc, Mnc, RSSI, RSRP, RSRQ, SNR, EArfcn,CI,TAC,Band,Bandwidth;
    int rssi, rsrq, rsrp, snr = 0;
    String mcc = "";
    String mnc = "" ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getLocation();

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 100);
        }
    }


    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);

        } catch (Exception e) {

        }
    }


    private void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        latitude_res = findViewById(R.id.Res_latitude);
        longitude_res = findViewById(R.id.Res_longitude);
        Mnc = findViewById(R.id.Res_MNC_Res);
        Mcc = findViewById(R.id.Res_MCC_Res);
        RSSI = findViewById(R.id.Res_RSSI);
        RSRP = findViewById(R.id.Res_RSRP);
        RSRQ = findViewById(R.id.Res_RSRQ);
        SNR = findViewById(R.id.Res_SNR);
        EArfcn = findViewById(R.id.res_Earfcn);
        TAC = findViewById(R.id.Res_LAC);
        CI = findViewById(R.id.Res_CID);
        Bandwidth = findViewById(R.id.res_BAndwidth);
        Band = findViewById(R.id.res_BAnd);


        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude_res.setText(String.valueOf(location.getLatitude()));
            longitude_res.setText(String.valueOf(location.getLongitude()));
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 10);
            }
            List<CellSignalStrength> cellInfoList;
            cellInfoList = telephonyManager.getSignalStrength().getCellSignalStrengths();
            for (CellSignalStrength cellInfo : cellInfoList) {
                if (cellInfo instanceof CellSignalStrengthLte) {
                    rssi = ((CellSignalStrengthLte) cellInfo).getRssi();
                    rsrp = ((CellSignalStrengthLte) cellInfo).getRsrp();
                    rsrq = ((CellSignalStrengthLte) cellInfo).getRsrq();
                    snr = ((CellSignalStrengthLte) cellInfo).getRssnr();
                }
                RSSI.setText(String.valueOf(rssi));
                RSRP.setText(String.valueOf(rsrp));
                RSRQ.setText(String.valueOf(rsrq));
                SNR.setText(String.valueOf(snr));
            }
            List<CellInfo> cellInfoList1 = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo1 : cellInfoList1)
            {
                if (cellInfo1 instanceof CellInfoLte)
                {
                    mcc = ((CellInfoLte)cellInfo1).getCellIdentity().getMccString();
                    mnc = ((CellInfoLte)cellInfo1).getCellIdentity().getMncString();
                    EArfcn.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getEarfcn()));
                    Bandwidth.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getBandwidth()))  ;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                       Band.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getBands()));
                    }
                    if (cellInfo1 != null)
                    {
                        CI.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getCi())) ;
                        TAC.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getTac())) ;

                    }
                    if (mcc != null && mnc != null)
                    {
                        Mcc.setText(mcc);
                        Mnc.setText(mnc);
                    }
                }
            }
        }
    }
}