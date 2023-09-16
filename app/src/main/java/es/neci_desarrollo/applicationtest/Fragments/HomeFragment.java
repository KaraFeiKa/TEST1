package es.neci_desarrollo.applicationtest.Fragments;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.telecom.Connection;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.PhysicalChannelConfig;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.opencsv.CSVWriter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;
import es.neci_desarrollo.applicationtest.location.MyLocationListener;
import es.neci_desarrollo.applicationtest.Store;
import es.neci_desarrollo.applicationtest.location.MyService;


public class HomeFragment extends Fragment implements LocationListenerInterface {
    private static LocationManager locationManager;
    String nocProjectDirInDownload = "noc-project";
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nocProjectDirInDownload;
    private TelephonyManager tm;
    private ConnectivityManager cm;
    SignalStrengthListener signalStrengthListener;
    CellInfoIDListener cellInfoIDListener;
    BWListener bwListener;
    CallList callList;

    private static MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mnc_Mcc, RSSI_RSRP, RSRQ_SNR_ECNO, text, earfcn_uarfcn_aerfcn,
            lac_tac, cid, band_pci_psc, TA, OPerator, cqi_dBm, asulevel, level, enb_rnc_bsic, ul_dl, mode_name,speed;
//    Button LogStart;
Button backk;
Boolean isWriteInBackground=Store.isWriteWorkingBackground;

    double lat, lot = 0;
    int rssi;    int rsrq;    int rsrp;    int snr;    int Cqi;    int dBm;    int Level;    int AsuLevel;    int ta;    int EcNo;
    int ber;    int eNB;    int TAC;    int band;    int EARFCN;    int CELLID;    int PCI;    int LAC;
    int UARFCN;    int PSC;    int RNCID;    int ARFCN;    int BSIC;    int CQi;    int TAa; int UpLinkSpeed; int DownLinkSpeed;
    int BERT;    int BandPlus;
    double FUL; int ss;
    double FDL;
    int [] convertedBands = new int[]{0};
    int [] bandwidnths = new int[]{0};
    String call = "";
    String mcc = "";
    String NameR = "";
    String Mode = "";
    String mnc = "";
    String Operator;
    CSVWriter writer = null;

    private boolean isNeedWrite = false;



    public HomeFragment(TelephonyManager tm) {
        this.tm = tm;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.MODIFY_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MODIFY_PHONE_STATE}, 100);
        }

        tm = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        getLocation();
        cellInfoIDListener = new CellInfoIDListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListener, CellInfoIDListener.LISTEN_CELL_INFO);
        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
        bwListener = new BWListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(bwListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        callList = new CallList();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(callList, PhoneStateListener.LISTEN_CALL_STATE);
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        startCell(cellInfoList);
        calc();
        calcUmts();
        calcArfcn();


        try {
            Log.d("public directory", csv);
            File appDir = new File(csv);
            if (!appDir.exists() && !appDir.isDirectory()) {
                if (appDir.mkdirs()) {
                    Log.d("public directory", "creted");
                } else {
                    Log.d("public directory", "not creted");
                    csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        backk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isWriteInBackground){
                    StartServive();
                    backk.setText("Остановить запись");
                    text.setText("Идет запись");
                    backk.setBackgroundColor(0xFFFF0000);
                    Store.isWriteWorkingBackground=true;
                    isWriteInBackground=true;
                }else{
                    stopService();
                    backk.setText("Начать запись");
                    text.setText("Запись сохранена!");
                    backk.setBackgroundColor(0xFF00FF00);
                    Store.isWriteWorkingBackground=false;
                    isWriteInBackground=false;
                }

            }
        });
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.MODIFY_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MODIFY_PHONE_STATE}, 100);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        latitude_res = view.findViewById(R.id.Latitude);
        longitude_res = view.findViewById(R.id.Longitude);
        Mnc_Mcc = view.findViewById(R.id.MCC_MNC);
        RSSI_RSRP = view.findViewById(R.id.RSRP_RSSI);
        RSRQ_SNR_ECNO = view.findViewById(R.id.RSRQ_SNR_EcNo);
        earfcn_uarfcn_aerfcn = view.findViewById(R.id.Earfcn_Uarfcn_Aerfcn);
        lac_tac = view.findViewById(R.id.LAC_TAC);
        cid = view.findViewById(R.id.CID);
        band_pci_psc = view.findViewById(R.id.Band_Pci_Psc);
        mode_name = view.findViewById(R.id.ModeName);
        ul_dl = view.findViewById(R.id.friq);
        TA = view.findViewById(R.id.TA);
        OPerator = view.findViewById(R.id.Operator);
        cqi_dBm = view.findViewById(R.id.Cqi_dBm);
        level = view.findViewById(R.id.Level);
        asulevel = view.findViewById(R.id.AsuLevel);
        enb_rnc_bsic = view.findViewById(R.id.eNB_Rnc_Bsic);
        text = view.findViewById(R.id.text);
        speed = view.findViewById(R.id.Speed);
        backk = view.findViewById(R.id.button2);
        String bname = "Начать запись";
        int color =(0xFF00FF00);
        if(Store.isWriteWorkingBackground || isWriteInBackground){
            bname ="Остановить запись";
            color = (0xFFFF0000);
            text.setText("Идет запись");

        }
        backk.setBackgroundColor(color);
        backk.setText(bname);

        return view;
    }
    public void StartServive() {
        Intent serviceIntent = new Intent(getContext(), MyService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
//        ContextCompat.startForegroundService(getContext(), serviceIntent);
        getContext().startForegroundService(serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(getContext(), MyService.class);
        getContext().stopService(serviceIntent);
    }
    private String DecToHex(int dec) {
        return String.format("%x", dec);
    }

    public int HexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }



    @SuppressLint("MissingPermission")
    public static void updateRangeLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, Store.range, myLocationListener);
    }
    private void calc() {
        int FDL_low, NDL, NOffs_DL, FUL_low, NUL, NOffs_UL;
        if (0 <= EARFCN && EARFCN <= 599) {
            NameR = "2100";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2110;
            NOffs_DL = 0;
            BandPlus = 1;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1920;
            NOffs_UL = 18000;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));

        }
        if (600 <= EARFCN && EARFCN <= 1199) {
            NameR = "1900 PCS";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 600;
            BandPlus = 2;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1850;
            NOffs_UL = 18600;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (1200 <= EARFCN && EARFCN <= 1949) {
            NameR = "1800+";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1805;
            NOffs_DL = 1200;
            BandPlus = 3;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1710;
            NOffs_UL = 19200;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (1950 <= EARFCN && EARFCN <= 2399) {
            NameR = "AWS-1";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2110;
            NOffs_DL = 1950;
            BandPlus = 4;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1710;
            NOffs_UL = 19950;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (2400 <= EARFCN && EARFCN <= 2649) {
            NameR = "850";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 869;
            NOffs_DL = 2400;
            BandPlus = 5;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 824;
            NOffs_UL = 20400;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (2750 <= EARFCN && EARFCN <= 3449) {
            NameR = "2600";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2620;
            NOffs_DL = 2750;
            BandPlus = 7;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 2500;
            NOffs_UL = 20750;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (3450 <= EARFCN && EARFCN <= 3799) {
            NameR = "900 GSM";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 925;
            NOffs_DL = 3450;
            BandPlus = 8;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 880;
            NOffs_UL = 21450;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (3800 <= EARFCN && EARFCN <= 4149) {
            NameR = "1800";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 1844.9;
            NOffs_DL = 3800;
            BandPlus = 9;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1749.9;
            NOffs_UL = 21800;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }

        if (4150 <= EARFCN && EARFCN <= 4749) {
            NameR = "AWS-3";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2110;
            NOffs_DL = 4150;
            BandPlus = 10;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1710;
            NOffs_UL = 22150;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (4750 <= EARFCN && EARFCN <= 4949) {
            NameR = "1500 Lower";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 1475.9;
            NOffs_DL = 4750;
            BandPlus = 11;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1427.9;
            NOffs_UL = 22750;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5010 <= EARFCN && EARFCN <= 5179) {
            NameR = "700 a";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 729;
            NOffs_DL = 5010;
            BandPlus = 12;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 699;
            NOffs_UL = 23010;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5180 <= EARFCN && EARFCN <= 5279) {
            NameR = "700 c";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 746;
            NOffs_DL = 5180;
            BandPlus = 13;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low =  777;
            NOffs_UL = 23180;
            FUL = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5280 <= EARFCN && EARFCN <= 5379) {
            NameR = "700 PS";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 758;
            NOffs_DL = 5280;
            BandPlus = 14;
            FDL =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 788;
            NOffs_UL = 23280;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5730 <= EARFCN && EARFCN <= 5849) {
            NameR = "700 b";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 734;
            NOffs_DL = 5730;
            BandPlus = 17;
            FDL =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 704;
            NOffs_UL = 23730;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5850 <= EARFCN && EARFCN <= 5999) {
            NameR = "800 Lower";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 860;
            NOffs_DL = 5850;
            BandPlus = 18;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 815;
            NOffs_UL = 23850;
            FUL =  (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6000 <= EARFCN && EARFCN <= 6149) {
            NameR = "800 Upper";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 875;
            NOffs_DL = 6000;
            BandPlus = 19;
            FDL =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 830;
            NOffs_UL = 24000;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6150 <= EARFCN && EARFCN <= 6449) {
            NameR = "800 DD";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 791;
            NOffs_DL = 6150;
            BandPlus = 20;
            FDL =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 832;
            NOffs_UL = 24150;
            FUL =  (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6450 <= EARFCN && EARFCN <= 6599) {
            NameR = "1500 Upper";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 1495.9;
            NOffs_DL = 6450;
            BandPlus = 21;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1447.9;
            NOffs_UL = 24450;
            FUL =  (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6600 <= EARFCN && EARFCN <= 7399) {
            NameR = "3500";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 3510;
            NOffs_DL = 6600;
            BandPlus = 22;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 3410;
            NOffs_UL = 24600;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (7700 <= EARFCN && EARFCN <= 8039) {
            NameR = "1600 L-band";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1525;
            NOffs_DL = 7700;
            BandPlus = 24;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1626.5;
            NOffs_UL = 25700;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (8040 <= EARFCN && EARFCN <= 8689) {
            NameR = "1900+";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 8040;
            BandPlus = 25;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1850;
            NOffs_UL = 26040;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (8690 <= EARFCN && EARFCN <= 9039) {
            NameR = "850+";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 859;
            NOffs_DL = 8690;
            BandPlus = 26;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 814;
            NOffs_UL = 26690;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9040 <= EARFCN && EARFCN <= 9209) {
            NameR = "800 SMR";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 852;
            NOffs_DL = 8690;
            BandPlus = 27;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 814;
            NOffs_UL = 26690;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9210 <= EARFCN && EARFCN <= 9659) {
            NameR = "700 APT";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 758;
            NOffs_DL = 9210;
            BandPlus = 28;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 703;
            NOffs_UL = 27210;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9660 <= EARFCN && EARFCN <= 9769) {
            NameR = "700 d";
            Mode = "SDL";
            NDL = EARFCN;
            FDL_low = 717;
            NOffs_DL = 9660;
            BandPlus = 29;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));

        }
        if (9770 <= EARFCN && EARFCN <= 9869) {
            NameR = "2300 WCS";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2350;
            NOffs_DL = 9770;
            BandPlus = 30;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 2305;
            NOffs_UL = 27660;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9870 <= EARFCN && EARFCN <= 9919) {
            NameR = "450";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 462.5;
            NOffs_DL = 9870;
            BandPlus = 31;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 452.5;
            NOffs_UL = 27760;
            FUL = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9920 <= EARFCN && EARFCN <= 10359) {
            NameR = "1500 L-band";
            Mode = "SDL";
            NDL = EARFCN;
            FDL_low = 1452;
            NOffs_DL = 9920;
            BandPlus = 32;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36000 <= EARFCN && EARFCN <= 36199) {
            NameR = "TD 1900";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1900;
            NOffs_DL = 36000;
            BandPlus = 33;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36200 <= EARFCN && EARFCN <= 36349) {
            NameR = "TD 2000";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 2010;
            NOffs_DL = 36200;
            BandPlus = 34;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36200 <= EARFCN && EARFCN <= 36349) {
            NameR = "TD PCS Lower";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1850;
            NOffs_DL = 36350;
            BandPlus = 35;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36950 <= EARFCN && EARFCN <= 37549) {
            NameR = "TD PCS Upper";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 36950;
            BandPlus = 36;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (37550 <= EARFCN && EARFCN <= 37749) {
            NameR = "TD PCS Center gap";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1910;
            NOffs_DL = 37550;
            BandPlus = 37;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (37750 <= EARFCN && EARFCN <= 38249) {
            NameR = "TD 2600";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 2570;
            NOffs_DL = 37750;
            BandPlus = 38;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
    }
    private void calcUmts() {
        int  NDL, NOffs_DL, NUL, NOffs_UL;
        if (10562 <= UARFCN && UARFCN <= 10838) {
            NameR = "2100";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 0;
            BandPlus = 1;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 950;
            NOffs_UL = 0;
            FUL = NOffs_UL+NUL/5;

        }
        if (9662 <= UARFCN && UARFCN <= 9938) {
            NameR = "1900 PCS";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 0;
            BandPlus = 2;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 400;
            NOffs_UL = 0;
            FUL = NOffs_UL+NUL/5;
        }
        if (1162 <= UARFCN && UARFCN <= 1513) {
            NameR = "1800 DCS";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 1575;
            BandPlus = 3;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 1525;
            FUL = NOffs_UL+NUL/5;
        }
        if (1537 <= UARFCN && UARFCN <= 1738) {
            NameR = "AWS-1";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 1805;
            BandPlus = 4;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 1450;
            FUL = NOffs_UL+NUL/5;
        }
        if (4357 <= UARFCN && UARFCN <= 4458) {
            NameR = "850";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 0;
            BandPlus = 5;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 0;
            FUL = NOffs_UL+NUL/5;
        }
        if (2237 <= UARFCN && UARFCN <= 2563) {
            NameR = "2600";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 2175;
            BandPlus = 7;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 2100;
            FUL = NOffs_UL+NUL/5;
        }
        if (2237 <= UARFCN && UARFCN <= 2563) {
            NameR = "900 GSM";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 340;
            BandPlus = 8;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 340;
            FUL = NOffs_UL+NUL/5;
        }
        if (3112 <= UARFCN && UARFCN <= 3388) {
            NameR = "AWS-1+";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 1490;
            BandPlus = 10;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 1135;
            FUL = NOffs_UL+NUL/5;
        }
        if (3712 <= UARFCN && UARFCN <= 3787) {
            NameR = "1500 Lower";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = 736;
            BandPlus = 11;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = 733;
            FUL = NOffs_UL+NUL/5;
        }
        if (3842 <= UARFCN && UARFCN <= 3903) {
            NameR = "700 a";
            Mode = "FDD";
            NDL = UARFCN;
            NOffs_DL = -37;
            BandPlus = 12;
            FDL = NOffs_DL+NDL/5;
            NUL = UARFCN - 225;
            NOffs_UL = -22;
            FUL = NOffs_UL+NUL/5;
        }
    }
    private void calcArfcn() {
        if (0 <= ARFCN && ARFCN <= 124) {
            NameR = "E-GSM";
            FUL = 890+0.2*ARFCN;
            FDL = FUL + 45;
        }
        if (512 <= ARFCN && ARFCN <= 885) {
            NameR = "DCS 1800";
            FUL = 1710.2+0.2*(ARFCN-512);
            FDL = FUL + 95;
        }
    }
    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, Store.range, myLocationListener);

        } catch (Exception ignored) {

        }
    }


 private class CallList extends PhoneStateListener
 {
     @Override
     public void onCallStateChanged(int state, String incomingNumber) {
         switch (state) {
             case TelephonyManager.CALL_STATE_IDLE:
                 call = "IDEL";
                 break;
             case TelephonyManager.CALL_STATE_RINGING:
                 call = "RINGING";
                 break;
             case TelephonyManager.CALL_STATE_OFFHOOK:
                 call = "OFFHOOK";
                 break;
         }
     }

     public void onHandoverComplete(Connection connection) {
     }

     public void onHandoverFailed(Connection connection, int error) {
         // Handover failed, do something
     }
 }


    @SuppressLint({"SetTextI18n", "MissingPermission"})
    private void startCell(List<CellInfo> cellInfoList) {
        for (CellInfo cellInfo : cellInfoList) {
            switch (tm.getDataNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE:
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (cellInfoLte.isRegistered()) {
                            calc();
                            mcc = cellInfoLte.getCellIdentity().getMccString();
                            mnc = cellInfoLte.getCellIdentity().getMncString();
                            ul_dl.setText("DL:  "+FDL+" МГц    " +"UL:  "+FUL+" МГц");
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoLte.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + "  4G");
                            lac_tac.setText("TAC:   " + cellInfoLte.getCellIdentity().getTac());
                            CELLID = cellInfoLte.getCellIdentity().getCi();
                            cid.setText("Cell ID:  " + CELLID + "  PCI:  "+PCI);
                            earfcn_uarfcn_aerfcn.setText("Earfcn:   " + (cellInfoLte.getCellIdentity().getEarfcn()));
                            String cellidHex = DecToHex(CELLID);
                            String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                            eNB = HexToDec(eNBHex);
                            enb_rnc_bsic.setText("eNB:   " + eNB);
                            PCI = cellInfoLte.getCellIdentity().getPci();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                int[] bands = cellInfoLte.getCellIdentity().getBands();
                                if (bands.length > 0) {
                                    band = bands[0];
                                    band_pci_psc.setText("Band:  " + band+ (" ("+NameR+") ")+ "  Режим:  "+Mode);
                                }
                                else
                                {
                                    band_pci_psc.setText("Band:  " + BandPlus+(" ("+NameR+") ")+ "  Режим:  "+Mode );
                                }
                            }
                            else
                            {
                                band_pci_psc.setText("Band:  " + BandPlus +(" ("+NameR+") ")+ "  Режим:  "+Mode);
                            }

                            TAC = cellInfoLte.getCellIdentity().getTac();
                            EARFCN = cellInfoLte.getCellIdentity().getEarfcn();
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
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                        if (cellInfoWcdma.isRegistered()) {
                            calcUmts();
                            mcc = cellInfoWcdma.getCellIdentity().getMccString();
                            mnc = cellInfoWcdma.getCellIdentity().getMncString();
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoWcdma.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + " 3G");
                            lac_tac.setText("LAC:   " + cellInfoWcdma.getCellIdentity().getLac());
                            CELLID = cellInfoWcdma.getCellIdentity().getCid();
                            cid.setText("Cell ID:  " + CELLID);
                            earfcn_uarfcn_aerfcn.setText("Uarfcn:   " + (cellInfoWcdma.getCellIdentity().getUarfcn()));
                            RNCID = CELLID / 65536;
                            enb_rnc_bsic.setText("Rnc:   " + RNCID);
                            PSC = cellInfoWcdma.getCellIdentity().getPsc();
                            band_pci_psc.setText("Psc:   " + PSC);
                            mode_name.setText("Band:  " + BandPlus +(" ("+NameR+") ")+ "  Режим:  "+Mode);
                            ul_dl.setText("DL:  "+FDL+" МГц    " +"UL:  "+FUL+" МГц");
                            LAC = cellInfoWcdma.getCellIdentity().getLac();
                            UARFCN = cellInfoWcdma.getCellIdentity().getUarfcn();
                        }
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_GSM:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered()) {
                            calcArfcn();
                            ul_dl.setText("DL:  "+FDL+" МГц    " +"UL:  "+FUL+" МГц");
                            mcc = cellInfoGsm.getCellIdentity().getMccString();
                            mnc = cellInfoGsm.getCellIdentity().getMncString();
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoGsm.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + " 2G");
                            lac_tac.setText("LAC:   " + cellInfoGsm.getCellIdentity().getLac());
                            CELLID = cellInfoGsm.getCellIdentity().getCid();
                            cid.setText("Cell ID:  " + CELLID);
                            earfcn_uarfcn_aerfcn.setText("Arfcn:   " + (cellInfoGsm.getCellIdentity().getArfcn()));
                            mode_name.setText("Band:  " + BandPlus +(" ("+NameR+") ")+ "  Режим:  "+Mode);
//                            RNCID = CELLID / 65536;
                            enb_rnc_bsic.setText("Bcis:  " + cellInfoGsm.getCellIdentity().getBsic() + "   Rnc: ");
                            LAC = cellInfoGsm.getCellIdentity().getLac();
                            ARFCN = cellInfoGsm.getCellIdentity().getArfcn();
                            BSIC = cellInfoGsm.getCellIdentity().getBsic();
                        }
                    }
                    break;
                default:
                    OPerator.setText("Необработано");
            }
        }
    }
    private class BWListener extends PhoneStateListener
    {
        public void onServiceStateChanged(ServiceState serviceState) {

            bandwidnths = serviceState.getCellBandwidths();
            if (bandwidnths.length > 0)
            {
                convertedBands = new int[bandwidnths.length];
                for(int i=0;i<bandwidnths.length;i++){
                    convertedBands[i]=bandwidnths[i]/1000;
                }
                if (convertedBands.length > 1)
                {
                    mode_name.setText("BW:  ["+Arrays.stream(convertedBands).mapToObj(String::valueOf).collect(Collectors.joining("/"))+"] CA, МГц" );
                }else
                {
                    mode_name.setText("BW:  "+Arrays.stream(convertedBands).mapToObj(String::valueOf).collect(Collectors.joining("/"))+" МГц" );
                }
            }
        }
    }
    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            startCell(cellInfoList);
            super.onCellInfoChanged(cellInfoList);
        }
    }
    private class SignalStrengthListener extends PhoneStateListener {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {

            List<CellSignalStrength> strengthAmplitude = signalStrength.getCellSignalStrengths();
            for (CellSignalStrength cellSignalStrength : strengthAmplitude) {
                switch (tm.getDataNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        if (cellSignalStrength instanceof CellSignalStrengthLte) {
                            snr = ((CellSignalStrengthLte) cellSignalStrength).getRssnr();
                            rssi = ((CellSignalStrengthLte) cellSignalStrength).getRssi();
                            rsrp = ((CellSignalStrengthLte) cellSignalStrength).getRsrp();
                            rsrq = ((CellSignalStrengthLte) cellSignalStrength).getRsrq();
                            Cqi = ((CellSignalStrengthLte) cellSignalStrength).getCqi();
                            dBm = cellSignalStrength.getDbm();
                            if (Cqi != Integer.MAX_VALUE) {
                                CQi = Cqi;
                                cqi_dBm.setText("dBm: " + dBm + "  Cqi: " + CQi);
                            } else {

                                cqi_dBm.setText("dBm: " + dBm + "  Cqi:  N/A");
                            }
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            ta = ((CellSignalStrengthLte) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TAa = ta;
                                TA.setText("TA:   " + (TAa) + "  RCC:  " + call);
                            } else {
                                TA.setText("TA:   N/A"+ "  RCC:  " + call);
                            }
                            RSRQ_SNR_ECNO.setText("RSRQ: " + rsrq + "  дБ" + "  SNR: " + snr + "  дБ");
                            if (rssi != Integer.MAX_VALUE)
                            {
                                RSSI_RSRP.setText("RSSI: " + rssi + "  дБм" + "   RSRP: " + rsrp + "  дБм");
                            }
                            else {
                                RSSI_RSRP.setText("RSSI: " +  " N/A " + "   RSRP: " + rsrp + "  дБм");
                            }
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel + "  дБм");
                        }
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        if (cellSignalStrength instanceof CellSignalStrengthWcdma) {
                            Log.d("Check",cellSignalStrength.toString());
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel + "  дБм");
                            dBm = cellSignalStrength.getDbm();
                            cqi_dBm.setText("RSCP: " + dBm );
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                EcNo = ((CellSignalStrengthWcdma) cellSignalStrength).getEcNo();
                            }
                            RSRQ_SNR_ECNO.setText("EcNo:   " + EcNo + "  дБ");

                            String[] CellSignalStrengthArr = cellSignalStrength.toString().split(" ");
                            ss = 0;
                            if(CellSignalStrengthArr.length>1) {
                                String[] elem = CellSignalStrengthArr[1].split("=");
                                if (elem[0].contains("ss")) {
                                    ss = Integer.parseInt(elem[1]);
                                }
                            }
                            TA.setText("RCC:  " + call);
                            RSSI_RSRP.setText("RSSI: " + ss + "  дБм");

                        }
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        if (cellSignalStrength instanceof CellSignalStrengthGsm) {
                            cqi_dBm.setText("dBm:   " + cellSignalStrength.getDbm());
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rssi = ((CellSignalStrengthGsm) cellSignalStrength).getRssi();
                            }
                            ber = ((CellSignalStrengthGsm) cellSignalStrength).getBitErrorRate();
                            if (ber != Integer.MAX_VALUE)
                            {
                                BERT = ber;
                                RSRQ_SNR_ECNO.setText("Bit Error Rate:  " + BERT);
                            } else
                            {
                                RSRQ_SNR_ECNO.setText("Bit Error Rate:  " +"   N/A");
                            }
                            dBm = cellSignalStrength.getDbm();
                            ta = ((CellSignalStrengthGsm) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TAa = ta;
                                TA.setText("TA:   " + (TAa) + "  RCC:  " + call);
                            } else {
                                TA.setText("TA:   N/A"+ "  RCC:  " + call);
                            }
                            RSSI_RSRP.setText("RSSI:   " + rssi + "  дБм");
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel + "  дБм");
                            cqi_dBm.setText("dBm: " + dBm);
                        }

                        break;
                    default:
                        OPerator.setText("Необработано");
                }
            }
        }
    }
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lot = location.getLongitude();
        latitude_res.setText("Широта:   " + lat);
        longitude_res.setText("Долгота:   " + lot);
    }
}