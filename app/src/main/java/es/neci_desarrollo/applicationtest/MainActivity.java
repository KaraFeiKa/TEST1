package es.neci_desarrollo.applicationtest;

import androidx.annotation.RequiresApi;
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
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoTdscdma;
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
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;
    SignalStrengthListener signalStrengthListener;
    private MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mnc_Mcc, RSSI_RSRP, RSRQ_SNR_ECNO, text, earfcn_uarfcn_aerfcn,
            lac_tac, cid, band_pci_psc, TA, OPerator, cqi_dBm, asulevel, level, enb_rnc_bsic;
    Button LogStart;
    int rssi, rsrq, rsrp, snr, Cqi, dBm, Level, AsuLevel, ta = 0;
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
        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);


        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        View.OnClickListener Log = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNeedWrite) {
                    text.setText("Идет запись");
                    try {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                        LocalDateTime now = LocalDateTime.now();
                        writer = new CSVWriter(new FileWriter(csv + dtf.format(now) + ".csv"));
                        List<String[]> data = new ArrayList<String[]>();
                        data.add(new String[]{"lat", "lng", "Operator", "Network", "mnc", "mcc", "CID", "eNB", "Band", "Earfcn/Uarfcn/Arfcn", "PCI/PSC/BSIC", "RSSI", "RSRP", "RSRQ", "SNR"});
                        writer.writeAll(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isNeedWrite = true;
                } else {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    text.setText("Запись сохранена");
                    isNeedWrite = false;
                }

            }
        };
        LogStart.setOnClickListener(Log);
    }


    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 4, myLocationListener);

        } catch (Exception ignored) {

        }
    }

    private void init() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        latitude_res = findViewById(R.id.Latitude);
        longitude_res = findViewById(R.id.Longitude);
        Mnc_Mcc = findViewById(R.id.MCC_MNC);
        RSSI_RSRP = findViewById(R.id.RSRP_RSSI);
        RSRQ_SNR_ECNO = findViewById(R.id.RSRQ_SNR_EcNo);
        earfcn_uarfcn_aerfcn = findViewById(R.id.Earfcn_Uarfcn_Aerfcn);
        lac_tac = findViewById(R.id.LAC_TAC);
        cid = findViewById(R.id.CID);
        band_pci_psc = findViewById(R.id.Band_Pci_Psc);
        TA = findViewById(R.id.TA);
        OPerator = findViewById(R.id.Operator);
        cqi_dBm = findViewById(R.id.Cqi_dBm);
        level = findViewById(R.id.Level);
        asulevel = findViewById(R.id.AsuLevel);
        enb_rnc_bsic = findViewById(R.id.eNB_Rnc_Bsic);
        text = findViewById(R.id.text);
        LogStart = findViewById(R.id.button);
    }

    private String DecToHex(int dec) {
        return String.format("%x", dec);
    }

    public int HexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }

    private class SignalStrengthListener extends PhoneStateListener {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            List<CellSignalStrength> strengthAmplitude = signalStrength.getCellSignalStrengths();
            for (CellSignalStrength cellSignalStrength : strengthAmplitude) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
                switch (telephonyManager.getDataNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        if (cellSignalStrength instanceof CellSignalStrengthLte) {
                            snr = ((CellSignalStrengthLte) cellSignalStrength).getRssnr();
                            rssi = ((CellSignalStrengthLte) cellSignalStrength).getRssi();
                            rsrp = ((CellSignalStrengthLte) cellSignalStrength).getRsrp();
                            rsrq = ((CellSignalStrengthLte) cellSignalStrength).getRsrq();
                            Cqi = ((CellSignalStrengthLte) cellSignalStrength).getCqi();
                            dBm = cellSignalStrength.getDbm();
                            if (Cqi != Integer.MAX_VALUE) {
                                cqi_dBm.setText("dBm: " + dBm + "  Cqi: " + Cqi);
                            } else {
                                cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");
                            }
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            ta = ((CellSignalStrengthLte) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TA.setText("TA   " + (ta));
                            } else {
                                TA.setText("TA   _");
                            }

                        }
                        RSRQ_SNR_ECNO.setText("RSRQ: " + rsrq + "  SNR: " + snr);
                        RSSI_RSRP.setText("RSSI: " + rssi + "  dBm" + "   RSRP: " + rsrp + "  dBm");
                        level.setText("Level:  " + Level);
                        asulevel.setText("Asulevel:  " + AsuLevel);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        if (cellSignalStrength instanceof CellSignalStrengthWcdma) {
                            RSSI_RSRP.setText("RSSI:  " + "  dBm" + "   RSRP: " + "  dBm");
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel);
                            dBm = cellSignalStrength.getDbm();
                            cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");
                            int EcNo = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                EcNo = ((CellSignalStrengthWcdma) cellSignalStrength).getEcNo();
                            }
                            RSRQ_SNR_ECNO.setText("EcNo:   " + EcNo);
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
                            RSRQ_SNR_ECNO.setText("Bit Error Rate:  " + ((CellSignalStrengthGsm) cellSignalStrength).getBitErrorRate());
                            dBm = cellSignalStrength.getDbm();
                            ta = ((CellSignalStrengthGsm) cellSignalStrength).getTimingAdvance();
                            if (ta != Integer.MAX_VALUE) {
                                TA.setText("Ta:   " + ta);
                            } else {
                                TA.setText(("TA:   -"));
                            }
                        }
                        RSSI_RSRP.setText("RSSI:   " + rssi);
                        level.setText("Level:  " + Level);
                        asulevel.setText("Asulevel:  " + AsuLevel);
                        cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");

                        break;
                    default:
                        OPerator.setText("Необработано");
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
            latitude_res.setText("latitude   " + location.getLatitude());
            longitude_res.setText("latitude   " + location.getLongitude());
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 10);
            }
            List<CellInfo> cellInfoList;
            cellInfoList = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList){
                switch (telephonyManager.getDataNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        if (cellInfo instanceof CellInfoLte) {
                            CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                            if (cellInfoLte.isRegistered()) {
                                mcc = cellInfoLte.getCellIdentity().getMccString();
                                mnc = cellInfoLte.getCellIdentity().getMncString();
                                Mnc_Mcc.setText("MCC: " + mcc + "  MNC: " + mnc);
                                Operator = (String) cellInfoLte.getCellIdentity().getOperatorAlphaLong();
                                OPerator.setText("Operator  " + Operator + "  4G");
                                lac_tac.setText("TAC:   " + cellInfoLte.getCellIdentity().getTac());
                                int CELLID = cellInfoLte.getCellIdentity().getCi();
                                cid.setText("Cell ID:  " + CELLID);
                                earfcn_uarfcn_aerfcn.setText("Earfcn:   " + (cellInfoLte.getCellIdentity().getEarfcn()));
                                String cellidHex = DecToHex(CELLID);
                                String eNBHex = cellidHex.substring(0, cellidHex.length() - 2);
                                int eNB = HexToDec(eNBHex);
                                enb_rnc_bsic.setText("eNB:   " + eNB);
                                int PCI = cellInfoLte.getCellIdentity().getPci();
                                int band = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    int[] bands = cellInfoLte.getCellIdentity().getBands();
                                    band_pci_psc.setText("Band  " + Arrays.stream(bands).mapToObj(String::valueOf)
                                            .collect(Collectors.joining(", ")) + "   Pci:  " + PCI);
                                    if (bands.length > 0) {
                                        band = bands[0];
                                    }
                                }

                                if (isNeedWrite) {
                                    String[] str = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(Operator), "4G",
                                            String.valueOf(mnc), String.valueOf(mcc), String.valueOf(CELLID), String.valueOf(eNB),
                                            String.valueOf(cellInfoLte.getCellIdentity().getTac()), String.valueOf(band), String.valueOf(cellInfoLte.getCellIdentity().getEarfcn()),
                                            String.valueOf(PCI), String.valueOf(rssi), String.valueOf(rsrp),
                                            String.valueOf(rsrq), String.valueOf(snr), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(Cqi), String.valueOf(ta)};
                                    writer.writeNext(str, false);
                                }
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
                                OPerator.setText("Operator  " + Operator + " 3G");
                                lac_tac.setText("LAC:   " + cellInfoWcdma.getCellIdentity().getLac());
                                int CELLID = cellInfoWcdma.getCellIdentity().getCid();
                                cid.setText("Cell ID:  " + CELLID);
                                earfcn_uarfcn_aerfcn.setText("Uarfcn:   " + (cellInfoWcdma.getCellIdentity().getUarfcn()));
                                int RNCID = CELLID / 65536;
                                enb_rnc_bsic.setText("Rnc:   " + RNCID);
                                int PSC = cellInfoWcdma.getCellIdentity().getPsc();
                                band_pci_psc.setText("Psc:   " + PSC);

                                if (isNeedWrite) {
                                    String[] str = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(Operator), "3G",
                                            String.valueOf(mnc), String.valueOf(mcc), String.valueOf(CELLID), String.valueOf(RNCID),
                                            String.valueOf(cellInfoWcdma.getCellIdentity().getLac()), String.valueOf(PSC), String.valueOf(cellInfoWcdma.getCellIdentity().getUarfcn()),
                                            "_", String.valueOf(rssi), String.valueOf(rsrp),
                                            String.valueOf(rsrq), String.valueOf(snr), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(Cqi), String.valueOf(ta)};
                                    writer.writeNext(str, false);
                                }
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
                                OPerator.setText("Operator  " + Operator + " 2G");
                                lac_tac.setText("LAC:   " + cellInfoGsm.getCellIdentity().getLac());
                                int CELLID = cellInfoGsm.getCellIdentity().getCid();
                                cid.setText("Cell ID:  " + CELLID);
                                earfcn_uarfcn_aerfcn.setText("Uarfcn:   " + (cellInfoGsm.getCellIdentity().getArfcn()));
                                int RNCID = CELLID / 65536;
                                enb_rnc_bsic.setText("Bcis:  " + cellInfoGsm.getCellIdentity().getBsic() + "   Rnc: " + RNCID);

                                if (isNeedWrite) {
                                    String[] str = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(Operator), "3G",
                                            String.valueOf(mnc), String.valueOf(mcc), String.valueOf(CELLID), String.valueOf(RNCID),
                                            String.valueOf(cellInfoGsm.getCellIdentity().getLac()), "_", String.valueOf(cellInfoGsm.getCellIdentity().getArfcn()),
                                            "_", String.valueOf(rssi), String.valueOf(rsrp),
                                            String.valueOf(rsrq), String.valueOf(snr), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(Cqi), String.valueOf(ta)};
                                    writer.writeNext(str, false);
                                }
                            }
                        }
                                break;
                                default:
                                    OPerator.setText("Необработано");
                        }

                }
            }
        }
