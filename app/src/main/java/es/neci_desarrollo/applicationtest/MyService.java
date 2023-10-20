package es.neci_desarrollo.applicationtest;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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

import androidx.core.app.NotificationCompat;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;
import es.neci_desarrollo.applicationtest.location.MyLocationListener;
import es.neci_desarrollo.applicationtest.speed.ITrafficSpeedListener;
import es.neci_desarrollo.applicationtest.speed.TrafficSpeedMeasurer;
import es.neci_desarrollo.applicationtest.speed.Utils;

public class MyService extends Service implements LocationListenerInterface {
    private static final boolean SHOW_SPEED_IN_BITS = false;
    private TrafficSpeedMeasurer mTrafficSpeedMeasurer;
    public static final String CHANNEL_ID = "AAAA";
    private static LocationManager locationManager;
    private MyLocationListener myLocationListener;

    private TelephonyManager tm;
    SignalStrengthListener signalStrengthListener;
    CellInfoIDListener cellInfoIDListener;
    BWListener bwListener;
    CallList callList;
    private CSVWriter writer;
    private CSVWriter writerN;
    private Double lat,lot;


    int rssi,rssi_N;    int rsrq,rsrq_N;    int rsrp,rsrp_N;    int snr;    int Cqi;    int dBm;    int Level;    int AsuLevel;
    int ta,ta_N;    int EcNo;
    int ber;    int eNB;    int TAC,LAC_N;    int band,band_N;    int EARFCN,EARFCN_N;    int CELLID,CELLID_N;
    int PCI,PCI_N;    int LAC;
    int UARFCN,UARFCN_N;    int PSC,PSC_N;    int RNCID;    int ARFCN,ARFCN_N;    int BSIC,BSIC_N;
    int CQi;    int TAa;
    int BERT;    int BandPlus;
    Double FUL; int ss,ss_N;
    Double FDL;
    int [] convertedBands = new int[]{0};
    int [] bandwidnths = new int[]{0};
    String Tech = "";
    String upStreamSpeed;
    String downStreamSpeed;
    String call = "";
    String mcc = "";
    String NameR = "";
    String Mode = "";
    String mnc = "";
    String Operator;
    String LastFileName;
    String nocProjectDirInDownload = "noc-project";
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nocProjectDirInDownload;
    private Networks currentNetwork = Networks.LTE;

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
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                if (cellInfoLte.isRegistered()) {
                    currentNetwork = Networks.LTE;
                    WriteLteInfo();
                }
            }
            if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                if (cellInfoWcdma.isRegistered()) {
                    currentNetwork = Networks.UMTS;
                    WriteUMTSInfo();
                }
            }
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                if (cellInfoGsm.isRegistered()) {
                    currentNetwork = Networks.GSM;
                    WriteGSMInfo();
                }
            }
        }

        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                if (!cellInfoLte.isRegistered() && currentNetwork==Networks.LTE) {
                    WriteLteInfoN();
                }
            }
            if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                if (!cellInfoWcdma.isRegistered() && currentNetwork==Networks.UMTS) {
                    WriteUMTSInfoN();
                }
            }
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                if (!cellInfoGsm.isRegistered() && currentNetwork==Networks.GSM) {
                    WriteGSMInfoN();
                }
            }
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
        mTrafficSpeedMeasurer = new TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.ALL);
        mTrafficSpeedMeasurer.startMeasuring();
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
        callList = new CallList();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(callList, PhoneStateListener.LISTEN_CALL_STATE);
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        startCell(cellInfoList);
        Neiborhood(cellInfoList);
        getLocation();

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            this.LastFileName = csv + "/" + dtf.format(now) + "_Main_"+Tech+".csv";
            writer = new CSVWriter(new FileWriter(this.LastFileName));
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"lat", "log", "Operator", "Network", "mcc", "mnc","Mode",
                    "TAC/LAC", "CID", "eNB", "Band","Bandwidnths, MHz", "Earfcn",
                    "Uarfcn", "Arfcn","UL, MHz","DL, MHz", "PCI", "PSC", "RNC",
                    "BSIC", "RSSI, dBm", "RSRP, dBm", "RSRQ, dB",
                    "SNR, dB", "EcNo, dB", "BER", "Cqi", "dBm", "Level", "Asulevel", "Ta","UP Speed","DL Speed"});
            writer.writeAll(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Store.isWriteNeighbors) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                LocalDateTime now = LocalDateTime.now();
                this.LastFileName = csv + "/" + dtf.format(now) + "_Neighbors_"+Tech+".csv";
                writerN = new CSVWriter(new FileWriter(this.LastFileName));
//                Store.writerN = new CSVWriter(new FileWriter(csv + "/" + dtf.format(now) + "_Neighbors_"+Tech+".csv"));
                List<String[]> dataN = new ArrayList<String[]>();
                dataN.add(new String[]{"lat", "log", "Network",
                        "TAC/LAC", "CID", "Band", "Earfcn",
                        "Uarfcn", "Arfcn", "PCI", "PSC",
                        "BSIC", "RSSI", "RSRP", "RSRQ", "Ta"});
                writerN.writeAll(dataN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTrafficSpeedMeasurer.stopMeasuring();
        try {
            writer.close();
            if(Store.isWriteNeighbors){
                writerN.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Store.setLastNameFile(this.LastFileName);
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
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (cellInfoLte.isRegistered()) {
                            calc();
                            Tech = "4G";
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
                                }else {
                                    band = BandPlus;
                                }
                            }
                            TAC = cellInfoLte.getCellIdentity().getTac();
                            EARFCN = cellInfoLte.getCellIdentity().getEarfcn();
                        }
                    }
                    if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                        if (cellInfoWcdma.isRegistered()) {
                            calcUmts();
                            Tech = "3G";
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
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered()) {
                            calcArfcn();
                            Tech = "2G";
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
        }
    }
    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            startCell(cellInfoList);
            Neiborhood(cellInfoList);
            super.onCellInfoChanged(cellInfoList);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTrafficSpeedMeasurer.registerListener(mStreamSpeedListener);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        mTrafficSpeedMeasurer.removeListener(mStreamSpeedListener);
        Log.d("BackG","onBind");

        return null;
    }

    private class SignalStrengthListener extends PhoneStateListener {
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            List<CellSignalStrength> strengthAmplitude = signalStrength.getCellSignalStrengths();
            for (CellSignalStrength cellSignalStrength : strengthAmplitude) {
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
                        }
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

    private class CallList extends PhoneStateListener
    {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
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
        }
        public void onHandoverComplete(Connection connection) {
        }
        public void onHandoverFailed(Connection connection, int error) {
            // Handover failed, do something
        }
    }

    private void calc() {
        double FDL_low, NDL, NOffs_DL, FUL_low, NUL, NOffs_UL;
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
            FDL_low =  1844.9;
            NOffs_DL = 3800;
            BandPlus = 9;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low =  1749.9;
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
            FDL_low =  1475.9;
            NOffs_DL = 4750;
            BandPlus = 11;
            FDL = (double) (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low =  1427.9;
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
            FDL_low = 1495.9;
            NOffs_DL = 6450;
            BandPlus = 21;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 1447.9;
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
            FUL_low = 1626.5;
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
            FDL_low = 462.5;
            NOffs_DL = 9870;
            BandPlus = 31;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            NUL = EARFCN + 18000;
            FUL_low = 452.5;
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
            FUL = 0.0;
        }
        if (36000 <= EARFCN && EARFCN <= 36199) {
            NameR = "TD 1900";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1900;
            NOffs_DL = 36000;
            BandPlus = 33;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0.0;
        }
        if (36200 <= EARFCN && EARFCN <= 36349) {
            NameR = "TD 2000";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 2010;
            NOffs_DL = 36200;
            BandPlus = 34;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0.0;
        }
        if (36200 <= EARFCN && EARFCN <= 36349) {
            NameR = "TD PCS Lower";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1850;
            NOffs_DL = 36350;
            BandPlus = 35;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0.0;
        }
        if (36950 <= EARFCN && EARFCN <= 37549) {
            NameR = "TD PCS Upper";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1930;
            NOffs_DL = 36950;
            BandPlus = 36;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0.0;
        }
        if (37550 <= EARFCN && EARFCN <= 37749) {
            NameR = "TD PCS Center gap";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 1910;
            NOffs_DL = 37550;
            BandPlus = 37;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0.0;
        }
        if (37750 <= EARFCN && EARFCN <= 38249) {
            NameR = "TD 2600";
            Mode = "TDD";
            NDL = EARFCN;
            FDL_low = 2570;
            NOffs_DL = 37750;
            BandPlus = 38;
            FDL = (FDL_low + 0.1 * (NDL - NOffs_DL));
            FUL = 0.0;
        }
    }
    private void calcUmts() {
        double  NDL, NOffs_DL, NUL, NOffs_UL;
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
            FDL = FUL + 95.0;
        }
    }
    public void WriteLteInfo (){
        if(writer!=null) {
            String bandwidth = "";
            if(convertedBands != null){
                bandwidth = String.valueOf(Arrays.stream(convertedBands).mapToObj(String::valueOf).collect(Collectors.joining("/")));
            }
            String[] str = new String[]{String.valueOf(lat), String.valueOf(lot),
                    String.valueOf(Operator), "4G", String.valueOf(mcc), String.valueOf(mnc),String.valueOf(Mode),
                    String.valueOf(TAC), String.valueOf(CELLID), String.valueOf(eNB),
                    (band+" ("+NameR+")"),bandwidth, String.valueOf(EARFCN), "", "", String.valueOf(FUL),String.valueOf(FDL), String.valueOf(PCI)
                    , "", "", "", String.valueOf(rssi), String.valueOf(rsrp),
                    String.valueOf(rsrq),
                    String.valueOf(snr), "", "", String.valueOf(CQi), String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(TAa),upStreamSpeed,downStreamSpeed};
            writer.writeNext(str, false);
        }
    }
    private void WriteUMTSInfo()
    {
        if(writer!=null) {
            String[] str = new String[]{
                    String.valueOf(lat), String.valueOf(lot), String.valueOf(Operator), "3G",
                    String.valueOf(mcc), String.valueOf(mnc),String.valueOf(Mode),
                    String.valueOf(LAC), String.valueOf(CELLID), "",(BandPlus+" ("+NameR+")"),"","",
                    String.valueOf(UARFCN), "",
                    String.valueOf(FUL),String.valueOf(FDL), "", String.valueOf(PSC), String.valueOf(RNCID),
                    "", String.valueOf(ss), "", "",
                    "", String.valueOf(EcNo), "", "",
                    String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), "",String.valueOf(upStreamSpeed),String.valueOf(downStreamSpeed)};
            writer.writeNext(str, false);
        }
    }
    private void WriteGSMInfo()
    {
        if(writer!=null){
            String[] str = new String[]{
                    String.valueOf(lat), String.valueOf(lot), String.valueOf(Operator), "2G",
                    String.valueOf(mcc), String.valueOf(mnc),"",
                    String.valueOf(LAC), String.valueOf(CELLID), "", NameR, "","",
                    "", String.valueOf(ARFCN), String.valueOf(FUL),String.valueOf(FDL), "", "",
                    String.valueOf(RNCID), String.valueOf(BSIC), String.valueOf(rssi), "",
                    "", "", "", String.valueOf(BERT), String.valueOf(Cqi), String.valueOf(dBm), String.valueOf(Level),
                    String.valueOf(AsuLevel), String.valueOf(TAa),String.valueOf(upStreamSpeed),String.valueOf(downStreamSpeed)};
            writer.writeNext(str, false);
        }
    }


    private ITrafficSpeedListener mStreamSpeedListener = new ITrafficSpeedListener() {
        @Override
        public void onTrafficSpeedMeasured(final double upStream, final double downStream) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    upStreamSpeed = Utils.parseSpeed(upStream, SHOW_SPEED_IN_BITS);
                    downStreamSpeed = Utils.parseSpeed(downStream, SHOW_SPEED_IN_BITS);
                }
            });
        }
    };

    enum Networks {
        LTE,
        UMTS,
        GSM,
    }
    @SuppressLint({"ResourceAsColor", "MissingPermission", "SetTextI18n"})
    private void Neiborhood(List<CellInfo> cellInfoList) {

        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoLte ) {
                CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                if (!cellInfoLte.isRegistered() ) {
                                                PCI_N = cellInfoLte.getCellIdentity().getPci();
                        EARFCN_N = cellInfoLte.getCellIdentity().getEarfcn();
                        band_N = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            int[] bands = cellInfoLte.getCellIdentity().getBands();

                            if (bands.length > 0) {
                                band_N = bands[0];
                            }
                                                    }
                        rssi_N = cellInfoLte.getCellSignalStrength().getRssi();
                        rsrp_N = cellInfoLte.getCellSignalStrength().getRsrp();
                        rsrq_N = cellInfoLte.getCellSignalStrength().getRsrq();
                        ta_N = cellInfoLte.getCellSignalStrength().getTimingAdvance();
                                    }

            }
            if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                if (!cellInfoWcdma.isRegistered()) {
                        PSC_N =cellInfoWcdma.getCellIdentity().getPsc();
                     UARFCN_N = cellInfoWcdma.getCellIdentity().getUarfcn();
                   String[] CellSignalStrengthArr = cellInfoWcdma.getCellSignalStrength().toString().split(" ");
                        ss_N = 0;
                        if(CellSignalStrengthArr.length>1) {
                            String[] elem = CellSignalStrengthArr[1].split("=");
                            if (elem[0].contains("ss")) {
                                ss_N = Integer.parseInt(elem[1]);
                            }
                        }
                }

            }
            if (cellInfo instanceof CellInfoGsm ) {
                CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                if (!cellInfoGsm.isRegistered()) {
                        LAC_N = cellInfoGsm.getCellIdentity().getLac();
                        CELLID_N = (cellInfoGsm.getCellIdentity().getCid());
                        ARFCN_N = cellInfoGsm.getCellIdentity().getArfcn();
                        BSIC_N = cellInfoGsm.getCellIdentity().getBsic();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            rssi_N = cellInfoGsm.getCellSignalStrength().getRssi();
                        }
                }

            }
        }
    }


    public void WriteLteInfoN () {
        if (writerN != null) {
            String[] str = new String[]{String.valueOf(lat),
                    String.valueOf(lot),
                    "4G", "", "", String.valueOf(band_N),
                    String.valueOf(EARFCN_N),
                    "", "",
                    String.valueOf(PCI_N), "", "",
                    String.valueOf(rssi_N),
                    String.valueOf(rsrp_N)
                    , String.valueOf(rsrq_N),
                    String.valueOf(ta_N)};
            writerN.writeNext(str, false);
        }
    }

    private void WriteUMTSInfoN(){
        if (writerN != null) {
            String[] str = new String[]{String.valueOf(lat),
                    String.valueOf(lot),
                    "3G","","","","",
                    String.valueOf(UARFCN_N),
                    "","",
                    String.valueOf(PSC_N),"",
                    "",
                    String.valueOf(ss_N)
                    ,"",
                    ""};
            writerN.writeNext(str, false);
        }
    }
    private void WriteGSMInfoN()
    {
        if (writerN != null) {
            String[] str = new String[]{String.valueOf(lat),
                    String.valueOf(lot),
                    "2G",
                    String.valueOf(LAC_N),
                    String.valueOf(CELLID_N),
                    "",
                    "",
                    "",
                    String.valueOf( ARFCN_N),
                    "","",
                    String.valueOf(BSIC_N),
                    String.valueOf( rssi_N),
                    "","",""};
            writerN.writeNext(str, false);
        }

    }
}