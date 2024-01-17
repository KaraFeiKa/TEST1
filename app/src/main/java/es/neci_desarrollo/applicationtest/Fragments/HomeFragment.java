package es.neci_desarrollo.applicationtest.Fragments;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.Connection;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.MyService;
import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.Store;
import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;
import es.neci_desarrollo.applicationtest.location.MyLocationListener;
import es.neci_desarrollo.applicationtest.speed.ITrafficSpeedListener;
import es.neci_desarrollo.applicationtest.speed.TrafficSpeedMeasurer;
import es.neci_desarrollo.applicationtest.speed.Utils;


public class HomeFragment extends Fragment implements LocationListenerInterface {

    private static LocationManager locationManager;
    private static final boolean SHOW_SPEED_IN_BITS = false;

    private TrafficSpeedMeasurer mTrafficSpeedMeasurer;
    String nocProjectDirInDownload = "noc-project";
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nocProjectDirInDownload;
    private TelephonyManager tm;
    private SignalStrengthListenerHome signalStrengthListenerHome;
    CellInfoIDListenerHome cellInfoIDListenerHome;
    BWListenerHome bwListenerHome;
    CallListHome callListHome;

    private static MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mnc_Mcc, RSSI_RSRP, RSRQ_SNR_ECNO, text, earfcn_uarfcn_aerfcn,
            lac_tac, cid, band_pci_psc, TA, OPerator, cqi_dBm, asulevel, level, enb_rnc_bsic, ul_dl, mode_name,speed;
//    Button LogStart;
Button backk;

Boolean isWriteInBackground=Store.isWriteWorkingBackground;

    double lat, lot = 0;
    int rssiHome;    int rsrqHome;    int rsrpHome;    int snrHome;    int CqiHome;    int dBmHome;    int LevelHome;    int AsuLevelHome;    int taHome;    int EcNoHome;
    int berHome;    int eNBHome;    int TACHome;    int bandHome;    int EARFCNHome;    int CELLIDHome;    int PCIHome;    int LACHome;
    int UARFCNHome;    int PSCHome;    int RNCIDHome;    int ARFCNHome;    int BSICHome;    int CQiHome;    int TAaHome;
    int BERTHome;    int BandPlusHome;
    Double FULHome; int ssHome;
    Double FDLHome;
    String upStreamSpeed;
    String downStreamSpeed;

    int [] convertedBands = new int[]{0};
    int [] bandwidnths = new int[]{0};
    String call = "";
    String mcc = "";
    String NameR = "";
    String Mode = "";
    String mnc = "";
    String Operator;


    public HomeFragment(TelephonyManager tm) {
        this.tm = tm;
    }

    @Override
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTrafficSpeedMeasurer = new TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.MOBILE);
        mTrafficSpeedMeasurer.startMeasuring();
        tm = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        getLocation();
        cellInfoIDListenerHome = new CellInfoIDListenerHome();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListenerHome, CellInfoIDListenerHome.LISTEN_CELL_INFO);
        signalStrengthListenerHome = new SignalStrengthListenerHome();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListenerHome, SignalStrengthListenerHome.LISTEN_SIGNAL_STRENGTHS);
        bwListenerHome = new BWListenerHome();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(bwListenerHome, PhoneStateListener.LISTEN_SERVICE_STATE);
        callListHome = new CallListHome();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(callListHome, PhoneStateListener.LISTEN_CALL_STATE);
          List<CellInfo> cellInfoList = tm.getAllCellInfo();
        startCell(cellInfoList);

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
        double FDL_low, NDL, NOffs_DL, FUL_low, NUL, NOffs_UL;
        if (0 <= EARFCNHome && EARFCNHome <= 599) {
            NameR = "2100";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 2110;
            NOffs_DL = 0;
            BandPlusHome = 1;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1920;
            NOffs_UL = 18000;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));

        }
        if (600 <= EARFCNHome && EARFCNHome <= 1199) {
            NameR = "1900 PCS";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 1930;
            NOffs_DL = 600;
            BandPlusHome = 2;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1850;
            NOffs_UL = 18600;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (1200 <= EARFCNHome && EARFCNHome <= 1949) {
            NameR = "1800+";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 1805;
            NOffs_DL = 1200;
            BandPlusHome = 3;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1710;
            NOffs_UL = 19200;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (1950 <= EARFCNHome && EARFCNHome <= 2399) {
            NameR = "AWS-1";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 2110;
            NOffs_DL = 1950;
            BandPlusHome = 4;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1710;
            NOffs_UL = 19950;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (2400 <= EARFCNHome && EARFCNHome <= 2649) {
            NameR = "850";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 869;
            NOffs_DL = 2400;
            BandPlusHome = 5;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 824;
            NOffs_UL = 20400;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (2750 <= EARFCNHome && EARFCNHome <= 3449) {
            NameR = "2600";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 2620;
            NOffs_DL = 2750;
            BandPlusHome = 7;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 2500;
            NOffs_UL = 20750;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (3450 <= EARFCNHome && EARFCNHome <= 3799) {
            NameR = "900 GSM";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 925;
            NOffs_DL = 3450;
            BandPlusHome = 8;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 880;
            NOffs_UL = 21450;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (3800 <= EARFCNHome && EARFCNHome <= 4149) {
            NameR = "1800";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low =  1844.9;
            NOffs_DL = 3800;
            BandPlusHome = 9;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low =  1749.9;
            NOffs_UL = 21800;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (4150 <= EARFCNHome && EARFCNHome <= 4749) {
            NameR = "AWS-3";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 2110;
            NOffs_DL = 4150;
            BandPlusHome = 10;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1710;
            NOffs_UL = 22150;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (4750 <= EARFCNHome && EARFCNHome <= 4949) {
            NameR = "1500 Lower";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low =  1475.9;
            NOffs_DL = 4750;
            BandPlusHome = 11;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low =  1427.9;
            NOffs_UL = 22750;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5010 <= EARFCNHome && EARFCNHome <= 5179) {
            NameR = "700 a";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 729;
            NOffs_DL = 5010;
            BandPlusHome = 12;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 699;
            NOffs_UL = 23010;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5180 <= EARFCNHome && EARFCNHome <= 5279) {
            NameR = "700 c";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 746;
            NOffs_DL = 5180;
            BandPlusHome = 13;
            FDLHome = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low =  777;
            NOffs_UL = 23180;
            FULHome = (double) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5280 <= EARFCNHome && EARFCNHome <= 5379) {
            NameR = "700 PS";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 758;
            NOffs_DL = 5280;
            BandPlusHome = 14;
            FDLHome =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 788;
            NOffs_UL = 23280;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5730 <= EARFCNHome && EARFCNHome <= 5849) {
            NameR = "700 b";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 734;
            NOffs_DL = 5730;
            BandPlusHome = 17;
            FDLHome =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 704;
            NOffs_UL = 23730;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5850 <= EARFCNHome && EARFCNHome <= 5999) {
            NameR = "800 Lower";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 860;
            NOffs_DL = 5850;
            BandPlusHome = 18;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 815;
            NOffs_UL = 23850;
            FULHome =  (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6000 <= EARFCNHome && EARFCNHome <= 6149) {
            NameR = "800 Upper";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 875;
            NOffs_DL = 6000;
            BandPlusHome = 19;
            FDLHome =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 830;
            NOffs_UL = 24000;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6150 <= EARFCNHome && EARFCNHome <= 6449) {
            NameR = "800 DD";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 791;
            NOffs_DL = 6150;
            BandPlusHome = 20;
            FDLHome =  (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 832;
            NOffs_UL = 24150;
            FULHome =  (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6450 <= EARFCNHome && EARFCNHome <= 6599) {
            NameR = "1500 Upper";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 1495.9;
            NOffs_DL = 6450;
            BandPlusHome = 21;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome+ 18000;
            FUL_low = 1447.9;
            NOffs_UL = 24450;
            FULHome =  (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6600 <= EARFCNHome && EARFCNHome <= 7399) {
            NameR = "3500";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 3510;
            NOffs_DL = 6600;
            BandPlusHome = 22;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 3410;
            NOffs_UL = 24600;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (7700 <= EARFCNHome && EARFCNHome <= 8039) {
            NameR = "1600 L-band";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 1525;
            NOffs_DL = 7700;
            BandPlusHome = 24;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1626.5;
            NOffs_UL = 25700;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (8040 <= EARFCNHome && EARFCNHome <= 8689) {
            NameR = "1900+";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 1930;
            NOffs_DL = 8040;
            BandPlusHome = 25;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 1850;
            NOffs_UL = 26040;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (8690 <= EARFCNHome && EARFCNHome <= 9039) {
            NameR = "850+";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 859;
            NOffs_DL = 8690;
            BandPlusHome = 26;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 814;
            NOffs_UL = 26690;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9040 <= EARFCNHome && EARFCNHome <= 9209) {
            NameR = "800 SMR";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 852;
            NOffs_DL = 8690;
            BandPlusHome = 27;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 814;
            NOffs_UL = 26690;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9210 <= EARFCNHome && EARFCNHome <= 9659) {
            NameR = "700 APT";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 758;
            NOffs_DL = 9210;
            BandPlusHome = 28;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 703;
            NOffs_UL = 27210;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9660 <= EARFCNHome && EARFCNHome <= 9769) {
            NameR = "700 d";
            Mode = "SDL";
            NDL = EARFCNHome;
            FDL_low = 717;
            NOffs_DL = 9660;
            BandPlusHome = 29;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));

        }
        if (9770 <= EARFCNHome && EARFCNHome <= 9869) {
            NameR = "2300 WCS";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 2350;
            NOffs_DL = 9770;
            BandPlusHome = 30;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 2305;
            NOffs_UL = 27660;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9870 <= EARFCNHome && EARFCNHome <= 9919) {
            NameR = "450";
            Mode = "FDD";
            NDL = EARFCNHome;
            FDL_low = 462.5;
            NOffs_DL = 9870;
            BandPlusHome = 31;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCNHome + 18000;
            FUL_low = 452.5;
            NOffs_UL = 27760;
            FULHome = (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9920 <= EARFCNHome && EARFCNHome <= 10359) {
            NameR = "1500 L-band";
            Mode = "SDL";
            NDL = EARFCNHome;
            FDL_low = 1452;
            NOffs_DL = 9920;
            BandPlusHome = 32;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
        if (36000 <= EARFCNHome && EARFCNHome <= 36199) {
            NameR = "TD 1900";
            Mode = "TDD";
            NDL = EARFCNHome;
            FDL_low = 1900;
            NOffs_DL = 36000;
            BandPlusHome = 33;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
        if (36200 <= EARFCNHome && EARFCNHome <= 36349) {
            NameR = "TD 2000";
            Mode = "TDD";
            NDL = EARFCNHome;
            FDL_low = 2010;
            NOffs_DL = 36200;
            BandPlusHome = 34;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
        if (36200 <= EARFCNHome && EARFCNHome <= 36349) {
            NameR = "TD PCS Lower";
            Mode = "TDD";
            NDL = EARFCNHome;
            FDL_low = 1850;
            NOffs_DL = 36350;
            BandPlusHome = 35;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
        if (36950 <= EARFCNHome && EARFCNHome <= 37549) {
            NameR = "TD PCS Upper";
            Mode = "TDD";
            NDL = EARFCNHome;
            FDL_low = 1930;
            NOffs_DL = 36950;
            BandPlusHome = 36;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
        if (37550 <= EARFCNHome && EARFCNHome <= 37749) {
            NameR = "TD PCS Center gap";
            Mode = "TDD";
            NDL = EARFCNHome;
            FDL_low = 1910;
            NOffs_DL = 37550;
            BandPlusHome = 37;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
        if (37750 <= EARFCNHome && EARFCNHome <= 38249) {
            NameR = "TD 2600";
            Mode = "TDD";
            NDL = EARFCNHome;
            FDL_low = 2570;
            NOffs_DL = 37750;
            BandPlusHome = 38;
            FDLHome = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FULHome = 0.0;
        }
    }
    private void calcUmts() {
        double  NDL, NOffs_DL, NUL, NOffs_UL;
        if (10562 <= UARFCNHome && UARFCNHome <= 10838) {
            NameR = "2100";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 0;
            BandPlusHome = 1;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 950;
            NOffs_UL = 0;
            FULHome = NOffs_UL+NUL/5;

        }
        if (9662 <= UARFCNHome && UARFCNHome <= 9938) {
            NameR = "1900 PCS";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 0;
            BandPlusHome = 2;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 400;
            NOffs_UL = 0;
            FULHome = NOffs_UL+NUL/5;
        }
        if (1162 <= UARFCNHome && UARFCNHome <= 1513) {
            NameR = "1800 DCS";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 1575;
            BandPlusHome = 3;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 1525;
            FULHome = NOffs_UL+NUL/5;
        }
        if (1537 <= UARFCNHome && UARFCNHome <= 1738) {
            NameR = "AWS-1";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 1805;
            BandPlusHome = 4;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 1450;
            FULHome = NOffs_UL+NUL/5;
        }
        if (4357 <= UARFCNHome && UARFCNHome <= 4458) {
            NameR = "850";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 0;
            BandPlusHome = 5;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 0;
            FULHome = NOffs_UL+NUL/5;
        }
        if (2237 <= UARFCNHome && UARFCNHome <= 2563) {
            NameR = "2600";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 2175;
            BandPlusHome = 7;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 2100;
            FULHome = NOffs_UL+NUL/5;
        }
        if (2237 <= UARFCNHome && UARFCNHome <= 2563) {
            NameR = "900 GSM";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 340;
            BandPlusHome = 8;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 340;
            FULHome = NOffs_UL+NUL/5;
        }
        if (3112 <= UARFCNHome && UARFCNHome <= 3388) {
            NameR = "AWS-1+";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 1490;
            BandPlusHome = 10;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 1135;
            FULHome = NOffs_UL+NUL/5;
        }
        if (3712 <= UARFCNHome && UARFCNHome <= 3787) {
            NameR = "1500 Lower";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = 736;
            BandPlusHome = 11;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = 733;
            FULHome = NOffs_UL+NUL/5;
        }
        if (3842 <= UARFCNHome && UARFCNHome <= 3903) {
            NameR = "700 a";
            Mode = "FDD";
            NDL = UARFCNHome;
            NOffs_DL = -37;
            BandPlusHome = 12;
            FDLHome = NOffs_DL+NDL/5;
            NUL = UARFCNHome - 225;
            NOffs_UL = -22;
            FULHome = NOffs_UL+NUL/5;
        }
    }
    private void calcArfcn() {
        if (0 <= ARFCNHome && ARFCNHome <= 124) {
            NameR = "E-GSM";
            FULHome = 890+0.2*ARFCNHome;
            FDLHome = FULHome + 45;
        }
        if (512 <= ARFCNHome && ARFCNHome <= 885) {
            NameR = "DCS 1800";
            FULHome = 1710.2+0.2*(ARFCNHome-512);
            FDLHome = FULHome + 95;
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
 private class CallListHome extends PhoneStateListener
 {

     @Override
     public void onCallStateChanged(int state, String incomingNumber) {
         ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
         switch (state) {
             case TelephonyManager.CALL_STATE_IDLE:
                 call = "IDLE";
                 break;
             case TelephonyManager.CALL_STATE_RINGING:
                 call = "RINGING";
                 break;
             case TelephonyManager.CALL_STATE_OFFHOOK:
                 call = "OFFHOOK";
                 break;
         }
         TA.setText("TA:   " + (TAaHome) + "  RRC:  " + call);
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
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (cellInfoLte.isRegistered()) {
                            Log.d("Check",cellInfoLte.getCellIdentity().toString());
                            calc();
                            mcc = cellInfoLte.getCellIdentity().getMccString();
                            mnc = cellInfoLte.getCellIdentity().getMncString();
                            ul_dl.setText("DL:  "+FDLHome+" МГц    " +"UL:  "+FULHome+" МГц");
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoLte.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + "  4G");
                            lac_tac.setText("TAC:   " + cellInfoLte.getCellIdentity().getTac());
                            CELLIDHome = cellInfoLte.getCellIdentity().getCi();
                            cid.setText("Cell ID:  " + CELLIDHome + "  PCI:  "+PCIHome);
                            earfcn_uarfcn_aerfcn.setText("Earfcn:   " + (cellInfoLte.getCellIdentity().getEarfcn()));
                            String cellidHex = DecToHex(CELLIDHome);
                            String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                            eNBHome = HexToDec(eNBHex);
                            enb_rnc_bsic.setText("eNB:   " + eNBHome);
                            PCIHome = cellInfoLte.getCellIdentity().getPci();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                int[] bands = cellInfoLte.getCellIdentity().getBands();
                                if (bands.length > 0) {
                                    bandHome = bands[0];
                                    band_pci_psc.setText("Band:  " + bandHome+ (" ("+NameR+") ")+ "  Режим:  "+Mode);
                                }
                                else
                                {
                                    band_pci_psc.setText("Band:  " + BandPlusHome+(" ("+NameR+") ")+ "  Режим:  "+Mode );
                                }
                            }
                            else
                            {
                                band_pci_psc.setText("Band:  " + BandPlusHome +(" ("+NameR+") ")+ "  Режим:  "+Mode);
                            }

                            TACHome = cellInfoLte.getCellIdentity().getTac();
                            EARFCNHome = cellInfoLte.getCellIdentity().getEarfcn();
                        }

                    }
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
                            CELLIDHome = cellInfoWcdma.getCellIdentity().getCid();
                            cid.setText("Cell ID:  " + CELLIDHome);
                            earfcn_uarfcn_aerfcn.setText("Uarfcn:   " + (cellInfoWcdma.getCellIdentity().getUarfcn()));
                            RNCIDHome = CELLIDHome / 65536;
                            enb_rnc_bsic.setText("Rnc:   " + RNCIDHome);
                            PSCHome = cellInfoWcdma.getCellIdentity().getPsc();
                            band_pci_psc.setText("Psc:   " + PSCHome);
                            mode_name.setText("Band:  " + BandPlusHome +(" ("+NameR+") ")+ "  Режим:  "+Mode);
                            ul_dl.setText("DL:  "+FDLHome+" МГц    " +"UL:  "+FULHome+" МГц");
                            LACHome = cellInfoWcdma.getCellIdentity().getLac();
                            UARFCNHome = cellInfoWcdma.getCellIdentity().getUarfcn();
                        }
                    }
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered()) {
                            calcArfcn();
                            ul_dl.setText("DL:  "+FDLHome+" МГц    " +"UL:  "+FULHome+" МГц");
                            mcc = cellInfoGsm.getCellIdentity().getMccString();
                            mnc = cellInfoGsm.getCellIdentity().getMncString();
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoGsm.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + " 2G");
                            lac_tac.setText("LAC:   " + cellInfoGsm.getCellIdentity().getLac());
                            CELLIDHome = cellInfoGsm.getCellIdentity().getCid();
                            cid.setText("Cell ID:  " + CELLIDHome);
                            earfcn_uarfcn_aerfcn.setText("Arfcn:   " + (cellInfoGsm.getCellIdentity().getArfcn()));
                            mode_name.setText("Band:  " + BandPlusHome +(" ("+NameR+") ")+ "  Режим:  "+Mode);
//                            RNCID = CELLID / 65536;
                            enb_rnc_bsic.setText("Bcis:  " + cellInfoGsm.getCellIdentity().getBsic() + "   Rnc: ");
                            LACHome = cellInfoGsm.getCellIdentity().getLac();
                            ARFCNHome = cellInfoGsm.getCellIdentity().getArfcn();
                            BSICHome = cellInfoGsm.getCellIdentity().getBsic();
                        }
                    }
            }
        }

    private class BWListenerHome extends PhoneStateListener
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
    private class CellInfoIDListenerHome extends PhoneStateListener {
        @Override
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            startCell(cellInfoList);
            super.onCellInfoChanged(cellInfoList);
        }
    }

    private class SignalStrengthListenerHome extends PhoneStateListener {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {

            List<CellSignalStrength> strengthAmplitude = signalStrength.getCellSignalStrengths();
            for (CellSignalStrength cellSignalStrength : strengthAmplitude) {
                        if (cellSignalStrength instanceof CellSignalStrengthLte) {
                            Log.d("Check SIGN",((CellSignalStrengthLte) cellSignalStrength).toString() );
                            snrHome = ((CellSignalStrengthLte) cellSignalStrength).getRssnr();
                            rssiHome = ((CellSignalStrengthLte) cellSignalStrength).getRssi();
                            rsrpHome = ((CellSignalStrengthLte) cellSignalStrength).getRsrp();
                            rsrqHome = ((CellSignalStrengthLte) cellSignalStrength).getRsrq();
                            CqiHome = ((CellSignalStrengthLte) cellSignalStrength).getCqi();
                            dBmHome = cellSignalStrength.getDbm();
                            if (CqiHome != Integer.MAX_VALUE) {
                                CQiHome = CqiHome;
                                cqi_dBm.setText("dBm: " + dBmHome + "  Cqi: " + CQiHome);
                            } else {

                                cqi_dBm.setText("dBm: " + dBmHome + "  Cqi:  N/A");
                            }
                            AsuLevelHome = cellSignalStrength.getAsuLevel();
                            LevelHome = cellSignalStrength.getLevel();
                            taHome = ((CellSignalStrengthLte) cellSignalStrength).getTimingAdvance();
                            if (taHome != Integer.MAX_VALUE) {
                                TAaHome = taHome;
                                TA.setText("TA:   " + (TAaHome) + "  RRC:  " + call);
                            } else {
                                TA.setText("TA:   N/A"+ "  RRC:  " + call);
                            }
                            RSRQ_SNR_ECNO.setText("RSRQ: " + rsrqHome + "  дБ" + "  SNR: " + snrHome + "  дБ");
                            if (rssiHome != Integer.MAX_VALUE)
                            {
                                RSSI_RSRP.setText("RSSI: " + rssiHome + "  дБм" + "   RSRP: " + rsrpHome + "  дБм");
                            }
                            else {
                                RSSI_RSRP.setText("RSSI: " +  " N/A " + "   RSRP: " + rsrpHome + "  дБм");
                            }
                            level.setText("Level:  " + LevelHome);
                            asulevel.setText("Asulevel:  " + AsuLevelHome + "  дБм");
                        }
                        if (cellSignalStrength instanceof CellSignalStrengthWcdma) {
                            Log.d("Check",cellSignalStrength.toString());
                            AsuLevelHome = cellSignalStrength.getAsuLevel();
                            LevelHome = cellSignalStrength.getLevel();
                            level.setText("Level:  " + LevelHome);
                            asulevel.setText("Asulevel:  " + AsuLevelHome + "  дБм");
                            dBmHome = cellSignalStrength.getDbm();
                            cqi_dBm.setText("RSCP: " + dBmHome );
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                EcNoHome = ((CellSignalStrengthWcdma) cellSignalStrength).getEcNo();
                            }
                            RSRQ_SNR_ECNO.setText("EcNo:   " + EcNoHome + "  дБ");

                            String[] CellSignalStrengthArr = cellSignalStrength.toString().split(" ");
                            ssHome = 0;
                            if(CellSignalStrengthArr.length>1) {
                                String[] elem = CellSignalStrengthArr[1].split("=");
                                if (elem[0].contains("ss")) {
                                    ssHome = Integer.parseInt(elem[1]);
                                }
                            }
                            TA.setText("RRC:  " + call);
                            RSSI_RSRP.setText("RSSI: " + ssHome + "  дБм");

                        }
                        if (cellSignalStrength instanceof CellSignalStrengthGsm) {
                            cqi_dBm.setText("dBm:   " + cellSignalStrength.getDbm());
                            AsuLevelHome = cellSignalStrength.getAsuLevel();
                            LevelHome = cellSignalStrength.getLevel();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rssiHome = ((CellSignalStrengthGsm) cellSignalStrength).getRssi();
                            }
                            berHome = ((CellSignalStrengthGsm) cellSignalStrength).getBitErrorRate();
                            if (berHome != Integer.MAX_VALUE)
                            {
                                BERTHome = berHome;
                                RSRQ_SNR_ECNO.setText("Bit Error Rate:  " + BERTHome);
                            } else
                            {
                                RSRQ_SNR_ECNO.setText("Bit Error Rate:  " +"   N/A");
                            }
                            dBmHome = cellSignalStrength.getDbm();
                            taHome = ((CellSignalStrengthGsm) cellSignalStrength).getTimingAdvance();
                            if (taHome != Integer.MAX_VALUE) {
                                TAaHome = taHome;
                                TA.setText("TA:   " + (TAaHome) + "  RRC:  " + call);
                            } else {
                                TA.setText("TA:   N/A"+ "  RRC:  " + call);
                            }
                            RSSI_RSRP.setText("RSSI:   " + rssiHome + "  дБм");
                            level.setText("Level:  " + LevelHome);
                            asulevel.setText("Asulevel:  " + AsuLevelHome + "  дБм");
                            cqi_dBm.setText("dBm: " + dBmHome);
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        mTrafficSpeedMeasurer.stopMeasuring();
            }
    @Override
    public void onPause() {
        super.onPause();
        mTrafficSpeedMeasurer.removeListener(mStreamSpeedListener);
    }
    @Override
    public void onResume() {
        super.onResume();
        mTrafficSpeedMeasurer.registerListener(mStreamSpeedListener);

    }
    private ITrafficSpeedListener mStreamSpeedListener = new ITrafficSpeedListener() {
        @Override
        public void onTrafficSpeedMeasured(final double upStream, final double downStream) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    upStreamSpeed = Utils.parseSpeed(upStream, SHOW_SPEED_IN_BITS);
                    downStreamSpeed = Utils.parseSpeed(downStream, SHOW_SPEED_IN_BITS);
                    speed.setText("UL:  " + upStreamSpeed + "   DL:   " + downStreamSpeed);

                }
            });
        }
    };
}