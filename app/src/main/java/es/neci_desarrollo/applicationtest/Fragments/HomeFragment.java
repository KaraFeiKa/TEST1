package es.neci_desarrollo.applicationtest.Fragments;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.telephony.PhysicalChannelConfig;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.MainActivity;
import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;
import es.neci_desarrollo.applicationtest.location.MyLocationListener;


public class HomeFragment extends Fragment implements LocationListenerInterface {
    String csv = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
    private TelephonyManager tm;
    private LocationManager locationManager;
    SignalStrengthListener signalStrengthListener;
    CellInfoIDListener cellInfoIDListener;
    private MyLocationListener myLocationListener;
    TextView latitude_res, longitude_res, Mnc_Mcc, RSSI_RSRP, RSRQ_SNR_ECNO, text, earfcn_uarfcn_aerfcn,
            lac_tac, cid, band_pci_psc, TA, OPerator, cqi_dBm, asulevel, level, enb_rnc_bsic, test;
    Button LogStart;
    float lat, lot = 0;
    int rssi, rsrq, rsrp, snr, Cqi, dBm, Level, AsuLevel, ta, EcNo, ber = 0;
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
        getLocation();
        cellInfoIDListener = new CellInfoIDListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListener, CellInfoIDListener.LISTEN_CELL_INFO);
        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
        super.onViewCreated(view, savedInstanceState);
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
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
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        tm = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        startCell(cellInfoList);
        LogStart.setBackgroundColor(0xFF00FF00);
        Button.OnClickListener Log = v -> {
            if (!isNeedWrite) {
                LogStart.setText("Отсановить запись");
                LogStart.setBackgroundColor(0xFFFF0000);
                text.setText("Идет запись");
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                    LocalDateTime now = LocalDateTime.now();
                    writer = new CSVWriter(new FileWriter(csv + dtf.format(now) + ".csv"));
                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"lat", "log", "Operator", "Network", "mcc", "mnc",
                            "TAC/LAC", "CID", "eNB", "Band", "Earfcn",
                            "Uarfcn", "Arfcn", "PCI", "PSC", "RNC",
                            "BSIC", "RSSI", "RSRP", "RSRQ",
                            "SNR", "EcNo", "BER", "Cqi", "dBm", "Level", "Asulevel", "Ta"});
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
                LogStart.setBackgroundColor(0xFF00FF00);
                LogStart.setText("Начать запись");
                text.setText("Запись сохранена");
                isNeedWrite = false;
            }

        };
        LogStart.setOnClickListener(Log);
        return view;
    }

    private String DecToHex(int dec) {
        return String.format("%x", dec);
    }

    public int HexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }


    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, myLocationListener);

        } catch (Exception ignored) {

        }
    }

    @SuppressLint("SetTextI18n")
    private void startCell(List<CellInfo> cellInfoList) {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        for (CellInfo cellInfo : cellInfoList) {
            switch (tm.getDataNetworkType()) {
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
                                String[] str = new String[]{String.valueOf(lat), String.valueOf(lot),
                                        String.valueOf(Operator), "4G", String.valueOf(mcc), String.valueOf(mnc),
                                        String.valueOf(cellInfoLte.getCellIdentity().getTac()), String.valueOf(CELLID), String.valueOf(eNB),
                                        String.valueOf(band), String.valueOf(cellInfoLte.getCellIdentity().getEarfcn()), "", "", String.valueOf(PCI)
                                        , "", "", "", String.valueOf(rssi), String.valueOf(rsrp),
                                        String.valueOf(rsrq),
                                        String.valueOf(snr), "", "", String.valueOf(Cqi), String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(ta)};
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
                                String[] str = new String[]{
                                        String.valueOf(lat), String.valueOf(lot), String.valueOf(Operator), "3G",
                                        String.valueOf(mcc), String.valueOf(mnc),
                                        String.valueOf(cellInfoWcdma.getCellIdentity().getLac()), String.valueOf(CELLID), "", "", "",
                                        String.valueOf(cellInfoWcdma.getCellIdentity().getUarfcn()), "", "", String.valueOf(PSC), String.valueOf(RNCID),
                                        "", String.valueOf(rssi), String.valueOf(rsrp), String.valueOf(rsrq),
                                        String.valueOf(snr), String.valueOf(EcNo), "", String.valueOf(Cqi),
                                        String.valueOf(dBm), String.valueOf(Level), String.valueOf(AsuLevel), String.valueOf(ta)
                                };
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
                            earfcn_uarfcn_aerfcn.setText("Arfcn:   " + (cellInfoGsm.getCellIdentity().getArfcn()));
                            int RNCID = CELLID / 65536;
                            enb_rnc_bsic.setText("Bcis:  " + cellInfoGsm.getCellIdentity().getBsic() + "   Rnc: " + RNCID);

                            if (isNeedWrite) {
                                String[] str = new String[]{
                                        String.valueOf(lat), String.valueOf(lot), String.valueOf(Operator), "2G",
                                        String.valueOf(mcc), String.valueOf(mnc),
                                        String.valueOf(cellInfoGsm.getCellIdentity().getLac()), String.valueOf(CELLID), "", "", "",
                                        "", String.valueOf(cellInfoGsm.getCellIdentity().getArfcn()), "", "",
                                        String.valueOf(RNCID), String.valueOf(cellInfoGsm.getCellIdentity().getBsic()), String.valueOf(rssi), String.valueOf(rsrp),
                                        String.valueOf(rsrq), String.valueOf(snr), "", String.valueOf(ber), String.valueOf(Cqi), String.valueOf(dBm), String.valueOf(Level),
                                        String.valueOf(AsuLevel), String.valueOf(ta)};
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

    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            startCell(cellInfoList);
            if (ActivityCompat.checkSelfPermission(HomeFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(HomeFragment.this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
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
                if (ActivityCompat.checkSelfPermission(HomeFragment.this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                }
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
                            RSSI_RSRP.setText("RSSI:  " + "   RSRP: ");
                            AsuLevel = cellSignalStrength.getAsuLevel();
                            Level = cellSignalStrength.getLevel();
                            level.setText("Level:  " + Level);
                            asulevel.setText("Asulevel:  " + AsuLevel);
                            dBm = cellSignalStrength.getDbm();
                            cqi_dBm.setText("dBm: " + dBm + "  Cqi:  _");
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
                            ber = ((CellSignalStrengthGsm) cellSignalStrength).getBitErrorRate();
                            RSRQ_SNR_ECNO.setText("Bit Error Rate:  " + ber);
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

    @Override
    public void onLocationChanged(Location location) {
        lat = (float) location.getLatitude();
        lot = (float) location.getLongitude();
        latitude_res.setText("latitude   " + lat);
        longitude_res.setText("Longitude   " + lot);
    }
}