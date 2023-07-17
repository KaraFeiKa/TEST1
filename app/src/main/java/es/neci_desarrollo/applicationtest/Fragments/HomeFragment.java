package es.neci_desarrollo.applicationtest.Fragments;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;
import es.neci_desarrollo.applicationtest.location.MyLocationListener;
import es.neci_desarrollo.applicationtest.Store;


public class HomeFragment extends Fragment implements LocationListenerInterface {
    private static LocationManager locationManager;
    String nocProjectDirInDownload = "noc-project";
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nocProjectDirInDownload;
    private TelephonyManager tm;
    SignalStrengthListener signalStrengthListener;
    CellInfoIDListener cellInfoIDListener;
    private static MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mnc_Mcc, RSSI_RSRP, RSRQ_SNR_ECNO, text, earfcn_uarfcn_aerfcn,
            lac_tac, cid, band_pci_psc, TA, OPerator, cqi_dBm, asulevel, level, enb_rnc_bsic;
    Button LogStart;
    double lat, lot = 0;
    int rssi, rsrq, rsrp, snr, Cqi, dBm, Level, AsuLevel, ta, EcNo, ber, eNB, TAC, band, EARFCN, CELLID, PCI, LAC,
            UARFCN, PSC, RNCID, ARFCN, BSIC, CQi, TAa, BERT,BANDTRUE = 0;
    String mcc = "";
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
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        getLocation();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        File appDir = new File(csv);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        latitude_res = view.findViewById(R.id.Latitude);
        longitude_res = view.findViewById(R.id.Longitude);
        Mnc_Mcc = view.findViewById(R.id.MCC_MNC);
        RSSI_RSRP = view.findViewById(R.id.RSRP_RSSI);
        RSRQ_SNR_ECNO = view.findViewById(R.id.RSRQ_SNR_EcNo);
        earfcn_uarfcn_aerfcn = view.findViewById(R.id.Earfcn_Uarfcn_Aerfcn);
        lac_tac = view.findViewById(R.id.LAC_TAC);
        cid = view.findViewById(R.id.CID);
        band_pci_psc = view.findViewById(R.id.Band_Pci_Psc);
        TA = view.findViewById(R.id.TA);
        OPerator = view.findViewById(R.id.Operator);
        cqi_dBm = view.findViewById(R.id.Cqi_dBm);
        level = view.findViewById(R.id.Level);
        asulevel = view.findViewById(R.id.AsuLevel);
        enb_rnc_bsic = view.findViewById(R.id.eNB_Rnc_Bsic);
        text = view.findViewById(R.id.text);
        LogStart = view.findViewById(R.id.button);

        tm = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        LogStart.setBackgroundColor(0xFF00FF00);
        LogStart.setBackgroundColor(0xFF00FF00);
        Button.OnClickListener LogB = v -> {


            if (!Store.isWriteWorking) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, Store.range, myLocationListener);
                LogStart.setText("Отсановить запись");
                LogStart.setBackgroundColor(0xFFFF0000);
                text.setText("Идет запись");
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                    LocalDateTime now = LocalDateTime.now();
                    writer = new CSVWriter(new FileWriter(csv + "/" + dtf.format(now) + "_Main.csv"));
                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"lat", "log", "Operator", "Network", "mcc", "mnc",
                            "TAC/LAC", "CID", "eNB", "Band", "Earfcn",
                            "Uarfcn", "Arfcn", "PCI", "PSC", "RNC",
                            "BSIC", "RSSI", "RSRP", "RSRQ",
                            "SNR", "EcNo", "BER", "Cqi", "dBm", "Level", "Asulevel", "Ta"});
                    writer.writeAll(data);
                    switch (tm.getDataNetworkType()) {
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            WriteLteInfo();
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            WriteUMTSInfo();
                            break;
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_GSM:
                            WriteGSMInfo();
                            break;
                        default:
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Соседние БС
                if (Store.isWriteNeighbors) {
                    try {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                        LocalDateTime now = LocalDateTime.now();
                        Store.writerN = new CSVWriter(new FileWriter(csv + "/" + dtf.format(now) + "_Neighbors.csv"));
                        List<String[]> dataN = new ArrayList<String[]>();
                        dataN.add(new String[]{"lat", "log", "Network",
                                "TAC/LAC", "CID", "Band", "Earfcn",
                                "Uarfcn", "Arfcn", "PCI", "PSC",
                                "BSIC", "RSSI", "RSRP", "RSRQ", "Ta"});
                        Store.writerN.writeAll(dataN);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isNeedWrite = true;
                Store.enableWrite();
            } else {
                try {
                    writer.close();
                    if (Store.isWriteNeighbors) {
                        Store.writerN.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LogStart.setBackgroundColor(0xFF00FF00);
                LogStart.setText("Начать запись");
                text.setText("Запись сохранена");
                Store.disableWrite();
            }
        };
        LogStart.setOnClickListener(LogB);
        Log.d("HOME FRAGMENT", "onCreateView 2");
        return view;
    }

        @Override
    public void onResume() {
        super.onResume();
        cellInfoIDListener = new CellInfoIDListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListener, CellInfoIDListener.LISTEN_CELL_INFO);
        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        startCell(cellInfoList);
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

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, Store.range, myLocationListener);

        } catch (Exception ignored) {

        }
    }

    @SuppressLint("SetTextI18n")
    private void startCell(List<CellInfo> cellInfoList) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        for (CellInfo cellInfo : cellInfoList) {
            switch (tm.getDataNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE:
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (cellInfoLte.isRegistered()) {
                            Log.d("LTE ALL", ((CellInfoLte) cellInfo).getCellSignalStrength().toString());
                            mcc = cellInfoLte.getCellIdentity().getMccString();
                            mnc = cellInfoLte.getCellIdentity().getMncString();
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoLte.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + "  4G");
                            lac_tac.setText("TAC:   " + cellInfoLte.getCellIdentity().getTac());
                            CELLID = cellInfoLte.getCellIdentity().getCi();
                            cid.setText("Cell ID:  " + CELLID);
                            earfcn_uarfcn_aerfcn.setText("Earfcn:   " + (cellInfoLte.getCellIdentity().getEarfcn()));
                            String cellidHex = DecToHex(CELLID);
                            String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                            eNB = HexToDec(eNBHex);
                            enb_rnc_bsic.setText("eNB:   " + eNB);
                            PCI = cellInfoLte.getCellIdentity().getPci();
                            band = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                int[] bands = cellInfoLte.getCellIdentity().getBands();
                                band_pci_psc.setText("Band:  " + Arrays.stream(bands).mapToObj(String::valueOf)
                                        .collect(Collectors.joining(", ")) + "   Pci:  " + PCI);
                                if (bands.length > 0) {
                                    band = bands[0];
                                    BANDTRUE = band;
                                }
                            }
                            else
                            {
                                band_pci_psc.setText("Band:  " + "N/A"  +"   Pci:  " + PCI);
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
                    if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                        if (cellInfoWcdma.isRegistered()) {
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
                            Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                            Operator = (String) cellInfoGsm.getCellIdentity().getOperatorAlphaLong();
                            OPerator.setText("Оператор:  " + Operator + " 2G");
                            lac_tac.setText("LAC:   " + cellInfoGsm.getCellIdentity().getLac());
                            CELLID = cellInfoGsm.getCellIdentity().getCid();
                            cid.setText("Cell ID:  " + CELLID);
                            earfcn_uarfcn_aerfcn.setText("Arfcn:   " + (cellInfoGsm.getCellIdentity().getArfcn()));
                            RNCID = CELLID / 65536;
                            enb_rnc_bsic.setText("Bcis:  " + cellInfoGsm.getCellIdentity().getBsic() + "   Rnc: " + RNCID);
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

    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            startCell(cellInfoList);
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            super.onCellInfoChanged(cellInfoList);
        }
    }

    private class SignalStrengthListener extends PhoneStateListener {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            List<CellSignalStrength> strengthAmplitude = signalStrength.getCellSignalStrengths();
            for (CellSignalStrength cellSignalStrength : strengthAmplitude) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                }
                switch (tm.getDataNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        Log.d("LTE ALL 1", ((CellSignalStrengthLte)cellSignalStrength).toString());
                        if (cellSignalStrength instanceof CellSignalStrengthLte) {
                            snr = ((CellSignalStrengthLte) cellSignalStrength).getRssnr();
                            rssi = ((CellSignalStrengthLte) cellSignalStrength).getRssi();
                            rsrp = ((CellSignalStrengthLte) cellSignalStrength).getRsrp();
                            rsrq = ((CellSignalStrengthLte) cellSignalStrength).getRsrq();
                            Cqi = ((CellSignalStrengthLte) cellSignalStrength).getCqi();
                            dBm = cellSignalStrength.getDbm();
                            if (Cqi != Integer.MAX_VALUE) {
                                CQi = Cqi;
                                cqi_dBm.setText("dBm: " + dBm + "  Cqi: " + Cqi);
                            } else {

                                cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");
                            }
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            ta = ((CellSignalStrengthLte) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TAa = ta;
                                TA.setText("TA   " + (ta));
                            } else {
                                TA.setText("TA   _");
                            }
                            RSRQ_SNR_ECNO.setText("RSRQ: " + rsrq + "  дБ" + "  SNR: " + snr + "  дБ");
                            RSSI_RSRP.setText("RSSI: " + rssi + "  дБм" + "   RSRP: " + rsrp + "  дБм");
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
                        if (cellSignalStrength instanceof CellSignalStrengthWcdma) {
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel + "  дБм");
                            dBm = cellSignalStrength.getDbm();
                            cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                EcNo = ((CellSignalStrengthWcdma) cellSignalStrength).getEcNo();
                            }
                            RSRQ_SNR_ECNO.setText("EcNo:   " + EcNo + "  дБ");
                        }
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_GSM:
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
                                RSRQ_SNR_ECNO.setText("Bit Error Rate:  " + ber);
                            } else
                            {
                                RSRQ_SNR_ECNO.setText("Bit Error Rate:  " +"   _");
                            }
                            dBm = cellSignalStrength.getDbm();
                            ta = ((CellSignalStrengthGsm) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TAa = ta;
                                TA.setText("Ta:   " + ta);
                            } else {
                                TA.setText(("TA:   -"));
                            }
                            RSSI_RSRP.setText("RSSI:   " + rssi + "  дБм");
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel + "  дБм");
                            cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");
                        }

                        break;
                    default:
                        OPerator.setText("Необработано");
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lot = location.getLongitude();
        latitude_res.setText("Широта:   " + lat);
        longitude_res.setText("Долгота:   " + lot);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        switch (tm.getDataNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_LTE:
                WriteLteInfo();
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                WriteUMTSInfo();
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_GSM:
                WriteGSMInfo();
                break;
            default:
        }
    }

    public void WriteLteInfo (){
        if (Store.isWriteWorking) {
            String[] str = new String[]{String.valueOf(lat), String.valueOf(lot),
                    String.valueOf(Operator), "4G", String.valueOf(mcc), String.valueOf(mnc),
                    String.valueOf(TAC), String.valueOf(CELLID), String.valueOf(eNB),
                    String.valueOf(BANDTRUE), String.valueOf(EARFCN), "", "", String.valueOf(PCI)
                    , "", "", "", String.valueOf(rssi), String.valueOf(rsrp),
                    String.valueOf(rsrq),
                    String.valueOf(snr), "", "", String.valueOf(CQi), String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(TAa)};
            writer.writeNext(str, false);
        }
    }

    private void WriteUMTSInfo()
    {
        if (Store.isWriteWorking) {
            String[] str = new String[]{
                    String.valueOf(lat), String.valueOf(lot), String.valueOf(Operator), "3G",
                    String.valueOf(mcc), String.valueOf(mnc),
                    String.valueOf(LAC), String.valueOf(CELLID), "", "", "",
                    String.valueOf(UARFCN), "", "", String.valueOf(PSC), String.valueOf(RNCID),
                    "", "", "", "",
                    "", String.valueOf(EcNo), "", "",
                    String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), ""
            };
            writer.writeNext(str, false);
        }
    }

    private void WriteGSMInfo()
    {
        if (Store.isWriteWorking) {
            String[] str = new String[]{
                    String.valueOf(lat), String.valueOf(lot), String.valueOf(Operator), "2G",
                    String.valueOf(mcc), String.valueOf(mnc),
                    String.valueOf(LAC), String.valueOf(CELLID), "", "", "",
                    "", String.valueOf(ARFCN), "", "",
                    String.valueOf(RNCID), String.valueOf(BSIC), String.valueOf(rssi), "",
                    "", "", "", String.valueOf(BERT), String.valueOf(Cqi), String.valueOf(dBm), String.valueOf(Level),
                    String.valueOf(AsuLevel), String.valueOf(TAa)};
            writer.writeNext(str, false);
        }
    }
}