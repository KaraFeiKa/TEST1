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
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements LocationListenerInterface {
    TelephonyManager telephonyManager;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mcc, Mnc, RSSI, RSRP, RSRQ, SNR;
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

        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude_res.setText(String.valueOf(location.getLatitude()));
            longitude_res.setText(String.valueOf(location.getLongitude()));
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 10);
            }
            List<CellSignalStrength> cellInfoList;
            cellInfoList = telephonyManager.getSignalStrength().getCellSignalStrengths();
            for (CellSignalStrength cellInfo : cellInfoList) {
                if (cellInfo instanceof CellSignalStrengthLte) {
//            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
//            for (CellInfo cellInfo : cellInfoList) {
//                if (cellInfo instanceof CellInfoLte) {
                    rssi = ((CellSignalStrengthLte) cellInfo).getRssi();
                    rsrp = ((CellSignalStrengthLte) cellInfo).getRsrp();
                    rsrq = ((CellSignalStrengthLte) cellInfo).getRsrq();
                    snr = ((CellSignalStrengthLte) cellInfo).getRssnr();
                }
                List<CellInfo> cellInfoList1;
                cellInfoList1 = telephonyManager.getAllCellInfo();

                for (CellInfo cellInfo1 : cellInfoList1) {
                    if (cellInfo1 instanceof CellInfoLte) {
                        mcc = ((CellInfoLte) cellInfo1).getCellIdentity().getMncString();
                        mnc = ((CellInfoLte) cellInfo1).getCellIdentity().getMccString();
                    }
                }
//                    rssi = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssi();
//                    //CQI.setText(String.valueOf(((CellInfoLte)cellInfo).getCellSignalStrength().getCqi()));
//                    rsrp = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrp();
//                    snr = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr();
//                    rsrq = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq();
//                    Mnc.setText(String.valueOf(((CellInfoLte) cellInfo).getCellIdentity().getMnc()));
//                    Mcc.setText(String.valueOf(((CellInfoLte) cellInfo).getCellIdentity().getMcc()));
//
//                    Log.d("LTE. Cell Info", "RSSI: " + ((CellInfoLte) cellInfo).getCellSignalStrength().getRssi());
//                }
////                        if (cellInfo instanceof CellInfoWcdma)
////                        {
////                            //Log.d("Wcdma. Cell Info", cellInfoList.toString());
////                        }
////                        if (cellInfo instanceof CellInfoGsm)
////                        {
////                           // Log.d("Gsm. Cell Info", cellInfoList.toString());
////                        }
////                    }
////            List<CellInfo> neighboringCellInfoList = telephonyManager.getAllCellInfo();
////                    for (CellInfo cellInfo : neighboringCellInfoList)
////                    {
////                        Log.d("Соседи", neighboringCellInfoList.toString());
////                    }
////            }
                RSSI.setText(String.valueOf(rssi));
                RSRP.setText(String.valueOf(rsrp));
                RSRQ.setText(String.valueOf(rsrq));
                SNR.setText(String.valueOf(snr));
                Mcc.setText("" + mcc);
                Mnc.setText("" + mnc);
            }
        }
    }
}