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
import android.os.Environment;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity implements LocationListenerInterface {
    String csv =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/";
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mcc, Mnc, RSSI, RSRP, RSRQ, SNR, EArfcn,CI,TAC,Band,OPerator,PCi,PCI,CQi,DBm,
            LEvel,ASuLevel,CQiTAb,ENB,TA,text,NetWork,PSC,UARfcn,RNC,TAA,EcNo,RSCP,BRR;
    Button LogStart;
    int rssi, rsrq, rsrp, snr,Cqi,dBm,Level,AsuLevel,CqiTAb,ta = 0;
    String mcc = "";
    String mnc = "";
    String Operator;
    CSVWriter writer = null;
    private boolean isNeedWrite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getLocation();

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        View.OnClickListener Log = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNeedWrite) {
                    text.setText("Идет запись");
                    try {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                        LocalDateTime now = LocalDateTime.now();
//                        writer = new CSVWriter(new FileWriter(csv+now.toString()+".csv"));
                        writer = new CSVWriter(new FileWriter(csv+dtf.format(now)+".csv"));
                        List<String[]> data = new ArrayList<String[]>();
                        data.add(new String[]{"lat", "lng","Operator","mnc","mcc","CID","eNB","Band","Earfcn/Uarfcn/Arfcn","PCI/PSC/BSIC","RSSI","RSRP","RSRQ","SNR"});
                        writer.writeAll(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isNeedWrite=true;
                }else{
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    text.setText("Запись сохранена");
                    isNeedWrite=false;
                }

            }
        };
        LogStart.setOnClickListener(Log);
    }


    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 2, myLocationListener);

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
        ENB = findViewById(R.id.Res_eNB);
        TA = findViewById(R.id.res_TA);
        text = findViewById(R.id.res_But);
        LogStart = findViewById(R.id.button);
        NetWork = findViewById(R.id.TypeNetork_Res);
    }

    private String DecToHex(int dec) {
        return String.format("%x",dec);
    }

    public int HexToDec(String hex){
        return  Integer.parseInt(hex, 16);
    }

    @Override
    public void onLocationChanged(Location location) {
            latitude_res.setText(String.valueOf(location.getLatitude()));
            longitude_res.setText(String.valueOf(location.getLongitude()));
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 10);
            }
            List<CellInfo> cellInfoList;
            cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList){
                switch(telephonyManager.getDataNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        NetWork.setText("4G");
                        if (cellInfo instanceof CellInfoLte) {
                            Log.d("LTE ALL", ((CellInfoLte) cellInfo).toString());
                            CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                            if (cellInfoLte.isRegistered()) {
                                mcc = cellInfoLte.getCellIdentity().getMccString();
                                mnc = cellInfoLte.getCellIdentity().getMncString();
                                EArfcn.setText(String.valueOf(cellInfoLte.getCellIdentity().getEarfcn()));
                                int band = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    int[] bands = ((CellInfoLte) cellInfo).getCellIdentity().getBands();
                                    Band.setText(Arrays.stream(bands).mapToObj(String::valueOf)
                                            .collect(Collectors.joining(", ")));
                                    if (bands.length > 0) {
                                        band = bands[0];
                                    }
                                }
                                int CELLID = cellInfoLte.getCellIdentity().getCi();
                                CI.setText(String.valueOf(CELLID));
                                TAC.setText(String.valueOf(((CellInfoLte) cellInfo).getCellIdentity().getTac()));
                                PCi.setText(String.valueOf(((CellInfoLte) cellInfo).getCellIdentity().getPci()));
                                Operator = (String) ((CellInfoLte) cellInfo).getCellIdentity().getOperatorAlphaLong();
                                OPerator.setText(Operator);
                                rssi = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssi();
                                rsrp = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrp();
                                rsrq = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq();
                                Cqi = ((CellInfoLte) cellInfo).getCellSignalStrength().getCqi();
                                ta = ((CellInfoLte) cellInfo).getCellSignalStrength().getTimingAdvance();
                                AsuLevel = ((CellInfoLte) cellInfo).getCellSignalStrength().getAsuLevel();
                                dBm = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
                                Level = ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    CqiTAb = ((CellInfoLte) cellInfo).getCellSignalStrength().getCqiTableIndex();
                                }

                                String cellidHex = DecToHex(CELLID);
                                String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                                int eNB = HexToDec(eNBHex);


                                if (isNeedWrite) {
                                      String[] str = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(Operator), String.valueOf(mnc), String.valueOf(mcc), String.valueOf(CELLID), String.valueOf(eNB),
                                            String.valueOf(band), String.valueOf(((CellInfoLte) cellInfo).getCellIdentity().getEarfcn()), String.valueOf(((CellInfoLte) cellInfo).getCellIdentity().getPci()), String.valueOf(rssi), String.valueOf(rsrp), String.valueOf(rsrq), String.valueOf(snr)};
                                    writer.writeNext(str, false);
                                }
                                ENB.setText("" + eNB);
                                Mcc.setText(mcc);
                                if (ta != Integer.MAX_VALUE) {
                                    TA.setText(String.valueOf(ta));
                                } else {
                                    TA.setText(String.valueOf("-"));
                                }
                                Mnc.setText(mnc);
                                RSSI.setText(String.valueOf(rssi));
                                RSRP.setText(String.valueOf(rsrp));
                                RSRQ.setText(String.valueOf(rsrq));
                                SNR.setText(String.valueOf(snr));
                                if (Cqi != Integer.MAX_VALUE) {
                                    CQi.setText(String.valueOf(Cqi));
                                } else {
                                    CQi.setText("-");
                                }
                                DBm.setText(String.valueOf(dBm));
                                LEvel.setText(String.valueOf(Level));
                                ASuLevel.setText(String.valueOf(AsuLevel));
                                if (Cqi != Integer.MAX_VALUE) {
                                    CQiTAb.setText(String.valueOf(CqiTAb));
                                } else {
                                    CQiTAb.setText("-");
                                }
                            }
                    List<CellSignalStrength> cellInfoList1;
            cellInfoList1 = telephonyManager.getSignalStrength().getCellSignalStrengths();
            for (CellSignalStrength cellInfo1 : cellInfoList1) {
                if (cellInfo1 instanceof CellSignalStrengthLte) {
                    snr = ((CellSignalStrengthLte) cellInfo1).getRssnr();
                }
                SNR.setText(String.valueOf(snr));
            }
                        }
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        NetWork.setText("3G");
                        if (cellInfo instanceof CellInfoWcdma) {
                            Log.d("UMTS ALL", ((CellInfoWcdma) cellInfo).toString());
                            CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                            if (cellInfoWcdma.isRegistered()) {
                                mcc = cellInfoWcdma.getCellIdentity().getMccString();
                                mnc = cellInfoWcdma.getCellIdentity().getMncString();
                                Operator = (String)  cellInfoWcdma.getCellIdentity().getOperatorAlphaLong();
                                OPerator.setText(Operator);
                                Mcc.setText(mcc);
                                Mnc.setText(mnc);
                                int CELLID = cellInfoWcdma.getCellIdentity().getCid();
                                CI.setText(String.valueOf(CELLID));
                                TAC.setText(String.valueOf(cellInfoWcdma.getCellIdentity().getLac()));
                                PSC = findViewById(R.id.eNB);
                                PSC.setText("Psc");
                                ENB.setText(String.valueOf(cellInfoWcdma.getCellIdentity().getPsc()));
                                UARfcn = findViewById(R.id.EArfcn);
                                UARfcn.setText("Uarfcn");
                                EArfcn.setText(String.valueOf(cellInfoWcdma.getCellIdentity().getUarfcn()));
                                RNC = findViewById(R.id.BAnd);
                                RNC.setText("Rnc");
                                PCI = findViewById(R.id.PCI);
                                PCI.setText(" ");
                                PCi.setText(" ");
                                TAA = findViewById(R.id.TA);
                                TAA.setText(" ");
                                TA.setText(" ");
                                AsuLevel = cellInfoWcdma.getCellSignalStrength().getAsuLevel();
                                rsrq = cellInfoWcdma.getCellSignalStrength().getDbm();
                                RSCP = findViewById(R.id.RSRP);
                                RSCP.setText("RSCP");
                                RSRP.setText(String.valueOf(rsrq));
                                Level = cellInfoWcdma.getCellSignalStrength().getLevel();
                                LEvel.setText(String.valueOf(Level));
                                EcNo = findViewById(R.id.RSRQ);
                                EcNo.setText("EcNo");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    rsrq = cellInfoWcdma.getCellSignalStrength().getEcNo();
                                }
                                RSRQ.setText(String.valueOf(rsrq));
                                ASuLevel.setText(String.valueOf(AsuLevel));

                                if (isNeedWrite) {
                                String[] str = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(Operator),
                                        String.valueOf(mnc), String.valueOf(mcc), String.valueOf(CELLID), "_",
                                        "_", String.valueOf(cellInfoWcdma.getCellIdentity().getUarfcn()),
                                        String.valueOf(cellInfoWcdma.getCellIdentity().getPsc()),
                                        "_", String.valueOf(cellInfoWcdma.getCellSignalStrength().getDbm()),
                                        "_", String.valueOf(rsrq)};
                                    writer.writeNext(str, false);
                                }
                            }
                        }

                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_GSM:
                        NetWork.setText("2G");
                        if (cellInfo instanceof CellInfoGsm) {
                            Log.d("UMTS ALL", ((CellInfoGsm) cellInfo).toString());
                            CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                            if (cellInfoGsm.isRegistered()) {
                                mcc = cellInfoGsm.getCellIdentity().getMccString();
                                mnc = cellInfoGsm.getCellIdentity().getMncString();
                                Operator = (String)  cellInfoGsm.getCellIdentity().getOperatorAlphaLong();
                                OPerator.setText(Operator);
                                Mcc.setText(mcc);
                                Mnc.setText(mnc);
                                int CELLID = cellInfoGsm.getCellIdentity().getCid();
                                CI.setText(String.valueOf(CELLID));
                                TAC.setText(String.valueOf(cellInfoGsm.getCellIdentity().getLac()));
                                UARfcn = findViewById(R.id.EArfcn);
                                UARfcn.setText("Arfcn");
                                EArfcn.setText(String.valueOf(cellInfoGsm.getCellIdentity().getArfcn()));
                                PSC = findViewById(R.id.eNB);
                                PSC.setText("Bsic");
                                ENB.setText(String.valueOf(cellInfoGsm.getCellIdentity().getBsic()));
                                RNC = findViewById(R.id.BAnd);
                                RNC.setText(" ");
                                PCI = findViewById(R.id.PCI);
                                PCI.setText(" ");
                                PCi.setText(" ");
                                RSCP = findViewById(R.id.RSRP);
                                RSCP.setText("RSCP");
                                if (cellInfoGsm.getCellSignalStrength().getTimingAdvance() != Integer.MAX_VALUE) {
                                    TA.setText(String.valueOf(cellInfoGsm.getCellSignalStrength().getTimingAdvance()));
                                } else {
                                    TA.setText(("-"));
                                }
                                RSRP.setText(String.valueOf( cellInfoGsm.getCellSignalStrength().getDbm()));
                                ASuLevel.setText(String.valueOf( cellInfoGsm.getCellSignalStrength().getAsuLevel()));
                                LEvel.setText(String.valueOf(cellInfoGsm.getCellSignalStrength().getLevel()));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    rssi = cellInfoGsm.getCellSignalStrength().getRssi();
                                }
                                RSSI.setText(String.valueOf(rssi));
                                BRR = findViewById(R.id.RSRQ);
                                BRR.setText("BRR");
                                RSRQ.setText(String.valueOf(cellInfoGsm.getCellSignalStrength().getBitErrorRate()));

                                if (isNeedWrite) {
                                    String[] str = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(Operator),
                                            String.valueOf(mnc), String.valueOf(mcc), String.valueOf(CELLID), "_",
                                            "_",(String.valueOf(cellInfoGsm.getCellIdentity().getArfcn())),
                                            String.valueOf(cellInfoGsm.getCellIdentity().getBsic()),
                                            String.valueOf(rssi), String.valueOf(cellInfoGsm.getCellSignalStrength().getDbm()),
                                            "_", String.valueOf(rsrq)};
                                    writer.writeNext(str, false);
                                }
                            }
                        }
                            break;
                    default:
                        NetWork.setText("Необработано. "+telephonyManager.getDataNetworkType());
                }
            }
        }
}
