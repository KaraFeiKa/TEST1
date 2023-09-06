package es.neci_desarrollo.applicationtest.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
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
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.Fragments.HomeFragment;
import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.Store;

public class MyService extends Service implements LocationListenerInterface{
    public static final String CHANNEL_ID = "AAAA";
    private static LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private TelephonyManager tm;
    SignalStrengthListener signalStrengthListener;
    CellInfoIDListener cellInfoIDListener;
    BWListener bwListener;
    private CSVWriter writer;
    private Double lat,lot;

    int rssi;    int rsrq;    int rsrp;    int snr;    int Cqi;    int dBm;    int Level;    int AsuLevel;    int ta;    int EcNo;
    int ber;    int eNB;    int TAC;    int band;    int EARFCN;    int CELLID;    int PCI;    int LAC;
    int UARFCN;    int PSC;    int RNCID;    int ARFCN;    int BSIC;    int CQi;    int TAa;
    int BERT;    int BandPlus;
    int FUL; int ss;
    int FDL;
    int [] convertedBands = new int[]{0};
    int [] bandwidnths = new int[]{0};
    String mcc = "";
    String NameR = "";
    String Mode = "";
    String mnc = "";
    String Operator;
    Button LogStart;

    String nocProjectDirInDownload = "noc-project";
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nocProjectDirInDownload;
    public MyService() {
    }
    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    Store.range,
                    myLocationListener);

        } catch (Exception ignored) {

        }
    }
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lot = location.getLongitude();
        if(writer!=null) {
            String bandwidth = "";
            if(convertedBands != null){
                bandwidth = String.valueOf(Arrays.stream(convertedBands).mapToObj(String::valueOf).collect(Collectors.joining("/")));
            }
            String[] str = new String[]{String.valueOf(lat), String.valueOf(lot),
                    String.valueOf(Operator), "4G", String.valueOf(mcc), String.valueOf(mnc),String.valueOf(Mode),
                    String.valueOf(TAC), String.valueOf(CELLID), String.valueOf(eNB),
                    (band+" ("+NameR+")"),bandwidth, String.valueOf(EARFCN), "", "",String.valueOf(FUL),String.valueOf(FDL), String.valueOf(PCI)
                    , "", "", "", String.valueOf(rssi), String.valueOf(rsrp),
                    String.valueOf(rsrq),
                    String.valueOf(snr), "", "", String.valueOf(CQi), String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(TAa),};
            writer.writeNext(str, false);
        }
    }
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void onCreate(){
        super.onCreate();
        Log.d("BackG","Create");
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build();
        startForeground(1, notification);
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        cellInfoIDListener = new CellInfoIDListener();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListener, CellInfoIDListener.LISTEN_CELL_INFO);
        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
        bwListener = new BWListener();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(bwListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        startCell(cellInfoList);
        getLocation();
        calc();



        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            writer = new CSVWriter(new FileWriter(csv + "/" + dtf.format(now) + "_Main_BG.csv"));
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"lat", "log", "Operator", "Network", "mcc", "mnc","Mode",
                    "TAC/LAC", "CID", "eNB", "Band","Bandwidnths, MHz", "Earfcn",
                    "Uarfcn", "Arfcn","UL, MHz","DL, MHz", "PCI", "PSC", "RNC",
                    "BSIC", "RSSI, dBm", "RSRP, dBm", "RSRQ, dB",
                    "SNR, dB", "EcNo, dB", "BER", "Cqi", "dBm", "Level", "Asulevel", "Ta"});
            writer.writeAll(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("BackG","Destroy");
    }

    private String DecToHex(int dec) {
        return String.format("%x", dec);
    }

    public int HexToDec(String hex) {
        return Integer.parseInt(hex, 16);
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
                            Operator = (String) cellInfoLte.getCellIdentity().getOperatorAlphaLong();
                           CELLID = cellInfoLte.getCellIdentity().getCi();
                            String cellidHex = DecToHex(CELLID);
                            String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                            eNB = HexToDec(eNBHex);
                            PCI = cellInfoLte.getCellIdentity().getPci();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                int[] bands = cellInfoLte.getCellIdentity().getBands();
                                if (bands.length > 0) {
                                    band = bands[0];
                                }
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

                            mcc = cellInfoWcdma.getCellIdentity().getMccString();
                            mnc = cellInfoWcdma.getCellIdentity().getMncString();
                            Operator = (String) cellInfoWcdma.getCellIdentity().getOperatorAlphaLong();
                            CELLID = cellInfoWcdma.getCellIdentity().getCid();
                            RNCID = CELLID / 65536;
                            PSC = cellInfoWcdma.getCellIdentity().getPsc();
                            LAC = cellInfoWcdma.getCellIdentity().getLac();
                            UARFCN = cellInfoWcdma.getCellIdentity().getUarfcn();
                        }
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_GSM:
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered()) {
                            mcc = cellInfoGsm.getCellIdentity().getMccString();
                            mnc = cellInfoGsm.getCellIdentity().getMncString();
                            Operator = (String) cellInfoGsm.getCellIdentity().getOperatorAlphaLong();
                            CELLID = cellInfoGsm.getCellIdentity().getCid();
//                           RNCID = CELLID / 65536;
                            LAC = cellInfoGsm.getCellIdentity().getLac();
                            ARFCN = cellInfoGsm.getCellIdentity().getArfcn();
                            BSIC = cellInfoGsm.getCellIdentity().getBsic();
                        }
                    }
                    break;
                default:
            }
        }
    }

//    @Nullable
//    @Override
//    public ComponentName startForegroundService(Intent service) {
//        Log.d("BackG","Foregrund");
//        return super.startForegroundService(service);
//    }

    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            startCell(cellInfoList);
            super.onCellInfoChanged(cellInfoList);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BackG","StartCommand");
//        new Timer().scheduleAtFixedRate(new TimerTask(){
//            @Override
//            public void run(){
//                Log.i("interval", "This function is called every 5 seconds.");
//                String[] str = new String[]{String.valueOf(lat), String.valueOf(lot)};
//                writer.writeNext(str, false);
//            }
//        },0,5000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("BackG","onBind");
        return null;
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
                            }
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            ta = ((CellSignalStrengthLte) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TAa = ta;
                            }
                            break;
                        }
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
                            dBm = cellSignalStrength.getDbm();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                EcNo = ((CellSignalStrengthWcdma) cellSignalStrength).getEcNo();
                            }
                            String[] CellSignalStrengthArr = cellSignalStrength.toString().split(" ");
                            ss = 0;
                            if(CellSignalStrengthArr.length>1) {
                                String[] elem = CellSignalStrengthArr[1].split("=");
                                if (elem[0].contains("ss")) {
                                    ss = Integer.parseInt(elem[1]);
                                }
                            }

                        }
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_GSM:
                        if (cellSignalStrength instanceof CellSignalStrengthGsm) {
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                rssi = ((CellSignalStrengthGsm) cellSignalStrength).getRssi();
                            }
                            ber = ((CellSignalStrengthGsm) cellSignalStrength).getBitErrorRate();
                            if (ber != Integer.MAX_VALUE)
                            {
                                BERT = ber;
                            }
                            dBm = cellSignalStrength.getDbm();
                            ta = ((CellSignalStrengthGsm) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TAa = ta;
                            }
                        }

                        break;
                    default:
                        break;
                }
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
            }
        }
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
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1920;
            NOffs_UL = 18000;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));

        }
        if (600 <= EARFCN && EARFCN <= 1199) {
            NameR = "1900 PCS";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 600;
            BandPlus = 2;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1850;
            NOffs_UL = 18600;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (1200 <= EARFCN && EARFCN <= 1949) {
            NameR = "1800+";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1805;
            NOffs_DL = 1200;
            BandPlus = 3;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1710;
            NOffs_UL = 19200;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (1950 <= EARFCN && EARFCN <= 2399) {
            NameR = "AWS-1";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2110;
            NOffs_DL = 1950;
            BandPlus = 4;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1710;
            NOffs_UL = 19950;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (2400 <= EARFCN && EARFCN <= 2649) {
            NameR = "850";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 869;
            NOffs_DL = 2400;
            BandPlus = 5;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 824;
            NOffs_UL = 20400;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (2750 <= EARFCN && EARFCN <= 3449) {
            NameR = "2600";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2620;
            NOffs_DL = 2750;
            BandPlus = 7;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 2500;
            NOffs_UL = 20750;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (3450 <= EARFCN && EARFCN <= 3799) {
            NameR = "900 GSM";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 925;
            NOffs_DL = 3450;
            BandPlus = 8;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 880;
            NOffs_UL = 21450;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (3800 <= EARFCN && EARFCN <= 4149) {
            NameR = "1800";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 1844.9;
            NOffs_DL = 3800;
            BandPlus = 9;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1749.9;
            NOffs_UL = 21800;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }

        if (4150 <= EARFCN && EARFCN <= 4749) {
            NameR = "AWS-3";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2110;
            NOffs_DL = 4150;
            BandPlus = 10;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1710;
            NOffs_UL = 22150;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (4750 <= EARFCN && EARFCN <= 4949) {
            NameR = "1500 Lower";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 1475.9;
            NOffs_DL = 4750;
            BandPlus = 11;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1427.9;
            NOffs_UL = 22750;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5010 <= EARFCN && EARFCN <= 5179) {
            NameR = "700 a";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 729;
            NOffs_DL = 5010;
            BandPlus = 12;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 699;
            NOffs_UL = 23010;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5180 <= EARFCN && EARFCN <= 5279) {
            NameR = "700 c";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 746;
            NOffs_DL = 5180;
            BandPlus = 13;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 777;
            NOffs_UL = 23180;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5280 <= EARFCN && EARFCN <= 5379) {
            NameR = "700 PS";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 758;
            NOffs_DL = 5280;
            BandPlus = 14;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 788;
            NOffs_UL = 23280;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5730 <= EARFCN && EARFCN <= 5849) {
            NameR = "700 b";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 734;
            NOffs_DL = 5730;
            BandPlus = 17;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 704;
            NOffs_UL = 23730;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (5850 <= EARFCN && EARFCN <= 5999) {
            NameR = "800 Lower";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 860;
            NOffs_DL = 5850;
            BandPlus = 18;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 815;
            NOffs_UL = 23850;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6000 <= EARFCN && EARFCN <= 6149) {
            NameR = "800 Upper";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 875;
            NOffs_DL = 6000;
            BandPlus = 19;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 830;
            NOffs_UL = 24000;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6150 <= EARFCN && EARFCN <= 6449) {
            NameR = "800 DD";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 791;
            NOffs_DL = 6150;
            BandPlus = 20;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 832;
            NOffs_UL = 24150;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6450 <= EARFCN && EARFCN <= 6599) {
            NameR = "1500 Upper";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 1495.9;
            NOffs_DL = 6450;
            BandPlus = 21;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1447.9;
            NOffs_UL = 24450;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (6600 <= EARFCN && EARFCN <= 7399) {
            NameR = "3500";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 3510;
            NOffs_DL = 6600;
            BandPlus = 22;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 3410;
            NOffs_UL = 24600;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (7700 <= EARFCN && EARFCN <= 8039) {
            NameR = "1600 L-band";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1525;
            NOffs_DL = 7700;
            BandPlus = 24;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 1626.5;
            NOffs_UL = 25700;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (8040 <= EARFCN && EARFCN <= 8689) {
            NameR = "1900+";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 8040;
            BandPlus = 25;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1850;
            NOffs_UL = 26040;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (8690 <= EARFCN && EARFCN <= 9039) {
            NameR = "850+";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 859;
            NOffs_DL = 8690;
            BandPlus = 26;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 814;
            NOffs_UL = 26690;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9040 <= EARFCN && EARFCN <= 9209) {
            NameR = "800 SMR";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 852;
            NOffs_DL = 8690;
            BandPlus = 27;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 814;
            NOffs_UL = 26690;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9210 <= EARFCN && EARFCN <= 9659) {
            NameR = "700 APT";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 758;
            NOffs_DL = 9210;
            BandPlus = 28;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 703;
            NOffs_UL = 27210;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9660 <= EARFCN && EARFCN <= 9769) {
            NameR = "700 d";
            Mode = "SDL";
            NDL = EARFCN;
            FDL_low = 717;
            NOffs_DL = 9660;
            BandPlus = 29;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));

        }
        if (9770 <= EARFCN && EARFCN <= 9869) {
            NameR = "2300 WCS";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = 2350;
            NOffs_DL = 9770;
            BandPlus = 30;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 2305;
            NOffs_UL = 27660;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9870 <= EARFCN && EARFCN <= 9919) {
            NameR = "450";
            Mode = "FDD";
            NDL = EARFCN;
            FDL_low = (int) 462.5;
            NOffs_DL = 9870;
            BandPlus = 31;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = (int) 452.5;
            NOffs_UL = 27760;
            FUL = (int) (FUL_low + 0.1 * (NUL - NOffs_UL));
        }
        if (9920 <= EARFCN && EARFCN <= 10359) {
            NameR = "1500 L-band";
            Mode = "SDL";
            NDL = EARFCN;
            FDL_low = 1452;
            NOffs_DL = 9920;
            BandPlus = 32;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36000 <= EARFCN && EARFCN <= 36199) {
            NameR = "TD 1900";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1900;
            NOffs_DL = 36000;
            BandPlus = 33;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36200 <= EARFCN && EARFCN <= 36349) {
            NameR = "TD 2000";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 2010;
            NOffs_DL = 36200;
            BandPlus = 34;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36200 <= EARFCN && EARFCN <= 36349) {
            NameR = "TD PCS Lower";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1850;
            NOffs_DL = 36350;
            BandPlus = 35;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (36950 <= EARFCN && EARFCN <= 37549) {
            NameR = "TD PCS Upper";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 36950;
            BandPlus = 36;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (37550 <= EARFCN && EARFCN <= 37749) {
            NameR = "TD PCS Center gap";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1910;
            NOffs_DL = 37550;
            BandPlus = 37;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
        if (37750 <= EARFCN && EARFCN <= 38249) {
            NameR = "TD 2600";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 2570;
            NOffs_DL = 37750;
            BandPlus = 38;
            FDL = (int) (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0;
        }
    }


}