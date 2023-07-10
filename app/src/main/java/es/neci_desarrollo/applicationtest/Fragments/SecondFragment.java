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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.MainActivity;
import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.location.LocationListenerInterface;
import es.neci_desarrollo.applicationtest.location.MyLocationListener;


public class SecondFragment extends Fragment implements LocationListenerInterface {
    private TelephonyManager tm;
    private TableLayout tableLayout;
    private MyLocationListener myLocationListener;
    private LocationManager locationManager;
    CellInfoIDListener cellInfoIDListener;

    public SecondFragment(TelephonyManager tm) {
        this.tm = tm;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout);
        myLocationListener = new MyLocationListener();
        myLocationListener.setLocationListenerInterface(this);
        init();
        getLocation();
        cellInfoIDListener = new CellInfoIDListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListener, CellInfoIDListener.LISTEN_CELL_INFO);
        Log.d("NCI", "Listener activate");

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        Neiborhood(cellInfoList);

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, myLocationListener);

        } catch (Exception ignored) {

        }
    }

    private void Neiborhood(List<CellInfo> cellInfoList) {
        if (ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) SecondFragment.this.getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        tableLayout.removeAllViews();
        int currRow = 0;
        TableRow tableRow = new TableRow(this.getContext());
        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        TextView tvPci = new TextView(this.getContext());
        tvPci.setText("PCI");
        tableRow.addView(tvPci, 0);
        TextView tvRssi = new TextView(this.getContext());
        tvRssi.setText("RSSI");
        tableRow.addView(tvRssi, 1);

        tableLayout.addView(tableRow, currRow);
        currRow++;

        for (CellInfo cellInfo : cellInfoList) {
            switch (tm.getDataNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE:
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (cellInfoLte.isRegistered() == false) {
                            cellInfoLte.getCellIdentity().getPci();
                            cellInfoLte.getCellIdentity().getEarfcn();
                            int band = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                int[] bands = cellInfoLte.getCellIdentity().getBands();
//                                test.setText(Arrays.stream(bands).mapToObj(String::valueOf)
//                                        .collect(Collectors.joining(", ")));
                                if (bands.length > 0) {
                                    band = bands[0];
                                }
                            }
                            cellInfoLte.getCellSignalStrength().getRssi();
                            cellInfoLte.getCellSignalStrength().getRsrp();
                            cellInfoLte.getCellSignalStrength().getRsrq();
                            cellInfoLte.getCellSignalStrength().getTimingAdvance();
                            tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvPciVal = new TextView(this.getContext());
                            tvPciVal.setText(String.valueOf(cellInfoLte.getCellIdentity().getPci()));
                            tableRow.addView(tvPciVal, 0);
                            TextView tvRssiVal = new TextView(this.getContext());
                            tvRssiVal.setText(String.valueOf(cellInfoLte.getCellSignalStrength().getRssi()));
                            tableRow.addView(tvRssiVal, 1);

                            tableLayout.addView(tableRow, currRow);
                            currRow++;
//                            test.setText("");
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
                        if (cellInfoWcdma.isRegistered() == false) {
                            cellInfoWcdma.getCellIdentity().getPsc();
                            cellInfoWcdma.getCellIdentity().getUarfcn();
                            cellInfoWcdma.getCellSignalStrength().getDbm();
                        }
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_GSM:
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered() == false) {
                            Log.d("LTE ALL", cellInfoGsm.toString());
                            cellInfoGsm.getCellIdentity().getLac();
                            cellInfoGsm.getCellIdentity().getCid();
                            cellInfoGsm.getCellIdentity().getArfcn();
                            cellInfoGsm.getCellIdentity().getBsic();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                cellInfoGsm.getCellSignalStrength().getRssi();
                            }
                        }
                    }
                    break;
                default:
//                    OPerator.setText("Необработано");
            }
        }
    }

    private void init() {


    }

    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            Log.d("NCI", "Changed");
            Neiborhood(cellInfoList);
            if (ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SecondFragment.this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) SecondFragment.this.getContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            super.onCellInfoChanged( cellInfoList);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}