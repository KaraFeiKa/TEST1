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
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity implements LocationListenerInterface {
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mcc, Mnc, RSSI, RSRP, RSRQ, SNR, EArfcn,CI,TAC,Band,OPerator,PCi,CQi,DBm,LEvel,ASuLevel,CQiTAb;
    int rssi, rsrq, rsrp, snr,Cqi,dBm,Level,AsuLevel,CqiTAb = 0;
    String mcc = "";
    String mnc = "";
    String Operator;


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
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
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
        Band = findViewById(R.id.res_BAnd);
        OPerator = findViewById(R.id.OPERATOR_Res);
        PCi = findViewById(R.id.res_PCI);
        CQi = findViewById(R.id.res_CQi);
        DBm = findViewById(R.id.res_dBM);
        LEvel = findViewById(R.id.res_Level);
        ASuLevel = findViewById(R.id.res_AsuLevel);
        CQiTAb = findViewById(R.id.res_CqiTableIndex);
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

            List<CellInfo> cellInfoList;
            cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList){
                if (cellInfo instanceof CellInfoLte)
                {
                    Log.d("LTE ALL",((CellInfoLte)cellInfo).toString());
                    if (((CellInfoLte)cellInfo).isRegistered() != false)
                    {
                        mcc = ((CellInfoLte)cellInfo).getCellIdentity().getMccString();
                        mnc = ((CellInfoLte)cellInfo).getCellIdentity().getMncString();
                        EArfcn.setText(String.valueOf(((CellInfoLte)cellInfo).getCellIdentity().getEarfcn()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            int[] band = ((CellInfoLte) cellInfo).getCellIdentity().getBands();
                            Band.setText(Arrays.stream(band).mapToObj(String::valueOf)
                                    .collect(Collectors.joining(", ")));
                        }
                            CI.setText(String.valueOf(((CellInfoLte)cellInfo).getCellIdentity().getCi()));
                            TAC.setText(String.valueOf(((CellInfoLte)cellInfo).getCellIdentity().getTac())) ;
                            PCi.setText (String.valueOf(((CellInfoLte)cellInfo).getCellIdentity().getPci()));
                            Operator= ((CellInfoLte)cellInfo).getCellIdentity().getMobileNetworkOperator();
                            OPerator.setText(Operator);
                            rssi = ((CellInfoLte)cellInfo).getCellSignalStrength().getRssi();
                            rsrp = ((CellInfoLte)cellInfo).getCellSignalStrength().getRsrp();
                            rsrq = ((CellInfoLte)cellInfo).getCellSignalStrength().getRsrq();
                            snr = ((CellInfoLte)cellInfo).getCellSignalStrength().getRssnr();
                            Cqi = ((CellInfoLte)cellInfo).getCellSignalStrength().getCqi();
                            AsuLevel = ((CellInfoLte)cellInfo).getCellSignalStrength().getAsuLevel();
                            dBm = ((CellInfoLte)cellInfo).getCellSignalStrength().getDbm();
                            Level = ((CellInfoLte)cellInfo).getCellSignalStrength().getLevel();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                CqiTAb = ((CellInfoLte)cellInfo).getCellSignalStrength().getCqiTableIndex();
                            }
                        Mcc.setText(mcc);
                        Mnc.setText(mnc);
                        RSSI.setText(String.valueOf(rssi));
                        RSRP.setText(String.valueOf(rsrp));
                        RSRQ.setText(String.valueOf(rsrq));
                        SNR.setText(String.valueOf(snr));
                        CQi.setText(String.valueOf(Cqi));
                        DBm.setText(String.valueOf(dBm));
                        LEvel.setText(String.valueOf(Level));
                        ASuLevel.setText(String.valueOf(AsuLevel));
                        CQiTAb.setText(String.valueOf(CqiTAb));
                        }
                    else
                    {
                        //Соседи
                    }
                    }
                }
            }
            List<CellSignalStrength> cellInfoList;
            cellInfoList = telephonyManager.getSignalStrength().getCellSignalStrengths();
            for (CellSignalStrength cellInfo1 : cellInfoList) {
                if (cellInfo1 instanceof CellSignalStrengthLte) {
                    Log.d("CELL Signal Strength LTE", ((CellSignalStrengthLte) cellInfo1).toString());
                }
            }
//                        rssi = ((CellSignalStrengthLte) cellInfo).getRssi();
//                        rsrp = ((CellSignalStrengthLte) cellInfo).getRsrp();
//                        rsrq = ((CellSignalStrengthLte) cellInfo).getRsrq();
//                        snr = ((CellSignalStrengthLte) cellInfo).getRssnr();
//                        Cqi = ((CellSignalStrengthLte) cellInfo).getCqi();
//                        AsuLevel = ((CellSignalStrengthLte) cellInfo).getAsuLevel();
//                        dBm = ((CellSignalStrengthLte) cellInfo).getDbm();
//                        Level = ((CellSignalStrengthLte) cellInfo).getLevel();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                            CqiTAb = ((CellSignalStrengthLte) cellInfo).getCqiTableIndex();
//                    }
//                    RSSI.setText(String.valueOf(rssi));
//                    RSRP.setText(String.valueOf(rsrp));
//                    RSRQ.setText(String.valueOf(rsrq));
//                    SNR.setText(String.valueOf(snr));
//                    CQi.setText(String.valueOf(Cqi));
//                    DBm.setText(String.valueOf(dBm));
//                    LEvel.setText(String.valueOf(Level));
//                    ASuLevel.setText(String.valueOf(AsuLevel));
//                    CQiTAb.setText(String.valueOf(CqiTAb));
//                }
//            }
//            List<CellInfo> cellInfoList1 = telephonyManager.getAllCellInfo();
//            for (CellInfo cellInfo1 : cellInfoList1)
//            {
//                if (cellInfo1 instanceof CellInfoLte)
//                {
//                    Log.d("CELL INFO LTE", ((CellInfoLte)cellInfo1).getCellIdentity().toString());
//                    mcc = ((CellInfoLte)cellInfo1).getCellIdentity().getMccString();
//                    mnc = ((CellInfoLte)cellInfo1).getCellIdentity().getMncString();
//                    }
//                    if (isMainCell(mcc,mnc))
//                    {
//                        Mcc.setText(mcc);
//                        Mnc.setText(mnc);
//                        EArfcn.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getEarfcn()));
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                            int[] band = ((CellInfoLte) cellInfo1).getCellIdentity().getBands();
//                            Band.setText(Arrays.stream(band).mapToObj(String::valueOf)
//                                    .collect(Collectors.joining(", ")));
//                        }
//                        CI.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getCi()));
//                        TAC.setText(String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getTac())) ;
//                        PCi.setText (String.valueOf(((CellInfoLte)cellInfo1).getCellIdentity().getPci()));
//                        Operator= ((CellInfoLte)cellInfo1).getCellIdentity().getMobileNetworkOperator();
//                        OPerator.setText(Operator);
//                    }
//                    else
//                    {
//                        //  цикл от каждого Pci
//                    }
//            }
        }
    }
