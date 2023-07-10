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

import com.google.android.material.tabs.TabLayout;

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


        for (CellInfo cellInfo : cellInfoList) {
            switch (tm.getDataNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_LTE:
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (cellInfoLte.isRegistered() == false) {

                            tableLayout.removeAllViews();
                            int currRow = 0;
                            TableRow tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvPci = new TextView(this.getContext());
                            tvPci.setTextSize(20);
                            tvPci.setText("PCI   ");
                            tableRow.addView(tvPci, 0);

                            TextView tvEarfcn = new TextView(this.getContext());
                            tvEarfcn.setTextSize(20);
                            tvEarfcn.setText("Earfcn   ");
                            tableRow.addView(tvEarfcn, 1);

                            TextView tvBand = new TextView(this.getContext());
                            tvBand.setTextSize(20);
                            tvBand.setText("Band  ");
                            tableRow.addView(tvBand, 2);

                            TextView tvRssi = new TextView(this.getContext());
                            tvRssi.setTextSize(20);
                            tvRssi.setText("RSSI  ");
                            tableRow.addView(tvRssi, 3);

                            TextView tvRsrp = new TextView(this.getContext());
                            tvRsrp.setTextSize(20);
                            tvRsrp.setText("RSRP  ");
                            tableRow.addView(tvRsrp, 4);

                            TextView tvRsrq = new TextView(this.getContext());
                            tvRsrq.setTextSize(20);
                            tvRsrq.setText("RSRQ  ");
                            tableRow.addView(tvRsrq, 5);

                            TextView tvTa = new TextView(this.getContext());
                            tvTa.setTextSize(20);
                            tvTa.setText("Ta  ");
                            tableRow.addView(tvTa, 6);

                            tableLayout.addView(tableRow, currRow);
                            currRow++;

                            tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvPciVal = new TextView(this.getContext());
                            tvPciVal.setTextSize(20);
                            tvPciVal.setText(String.valueOf(cellInfoLte.getCellIdentity().getPci()));
                            tableRow.addView(tvPciVal, 0);

                            TextView tvEarfcnVal = new TextView(this.getContext());
                            tvEarfcnVal.setTextSize(20);
                            tvEarfcnVal.setText(String.valueOf(cellInfoLte.getCellIdentity().getEarfcn()));
                            tableRow.addView(tvEarfcnVal, 1);



                            int band = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                int[] bands = cellInfoLte.getCellIdentity().getBands();
                                TextView tvBandVal = new TextView(this.getContext());
                                tvBandVal.setTextSize(20);
                                tvBandVal.setText(Arrays.stream(bands).mapToObj(String::valueOf)
                                        .collect(Collectors.joining(", ")));
                                if (bands.length > 0) {
                                    band = bands[0];
                                }
                                tableRow.addView(tvBandVal, 2);
                            }


                            TextView tvRssiVal = new TextView(this.getContext());
                            tvRssiVal.setTextSize(20);
                            tvRssiVal.setText(String.valueOf(cellInfoLte.getCellSignalStrength().getRssi()));
                            tableRow.addView(tvRssiVal, 3);

                            TextView tvRsrpVal = new TextView(this.getContext());
                            tvRsrpVal.setTextSize(20);
                            tvRsrpVal.setText(String.valueOf(cellInfoLte.getCellSignalStrength().getRsrp()));
                            tableRow.addView(tvRsrpVal, 4);

                            TextView tvRsrqVal = new TextView(this.getContext());
                            tvRsrqVal.setTextSize(20);
                            tvRsrqVal.setText(String.valueOf(cellInfoLte.getCellSignalStrength().getRsrq()));
                            tableRow.addView(tvRsrqVal, 5);

                            TextView tvTaVal = new TextView(this.getContext());
                            tvTaVal.setTextSize(20);
                            tvTaVal.setText(String.valueOf(cellInfoLte.getCellSignalStrength().getTimingAdvance()));
                            tableRow.addView(tvTaVal, 6);


                            tableLayout.addView(tableRow, currRow);
                            currRow++;
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

                            tableLayout.removeAllViews();
                            int currRow = 0;
                            TableRow tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvPsc = new TextView(this.getContext());
                            tvPsc.setTextSize(20);
                            tvPsc.setText("PSC   ");
                            tableRow.addView(tvPsc, 0);

                            TextView tvUarfcn = new TextView(this.getContext());
                            tvUarfcn.setTextSize(20);
                            tvUarfcn.setText("Uarfcn   ");
                            tableRow.addView(tvUarfcn, 1);

                            TextView tvDbm = new TextView(this.getContext());
                            tvDbm.setTextSize(20);
                            tvDbm.setText("dBm  ");
                            tableRow.addView(tvDbm, 2);

                            tableLayout.addView(tableRow, currRow);
                            currRow++;

                            tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvPscVal = new TextView(this.getContext());
                            tvPscVal.setTextSize(20);
                            tvPscVal.setText(String.valueOf(cellInfoWcdma.getCellIdentity().getPsc()));
                            tableRow.addView(tvPscVal, 0);

                            TextView tvUarfcnVal = new TextView(this.getContext());
                            tvUarfcnVal.setTextSize(20);
                            tvUarfcnVal.setText(String.valueOf(cellInfoWcdma.getCellIdentity().getUarfcn()));
                            tableRow.addView(tvUarfcnVal, 1);

                            TextView tvdBmVal = new TextView(this.getContext());
                            tvdBmVal.setTextSize(20);
                            tvdBmVal.setText(String.valueOf( cellInfoWcdma.getCellSignalStrength().getDbm()));
                            tableRow.addView(tvdBmVal, 2);
                            tableLayout.addView(tableRow, currRow);
                            currRow++;

                        }
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_GSM:
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered() == false) {

                            tableLayout.removeAllViews();
                            int currRow = 0;
                            TableRow tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvLAC = new TextView(this.getContext());
                            tvLAC.setTextSize(20);
                            tvLAC.setText("LAC     ");
                            tableRow.addView(tvLAC, 0);

                            TextView tvCid = new TextView(this.getContext());
                            tvCid.setTextSize(20);
                            tvCid.setText("   CID   ");
                            tableRow.addView(tvCid, 1);

                            TextView tvArfcn = new TextView(this.getContext());
                            tvArfcn.setTextSize(20);
                            tvArfcn.setText("ARFCN    ");
                            tableRow.addView(tvArfcn, 2);

                            TextView tvBsic = new TextView(this.getContext());
                            tvBsic.setTextSize(20);
                            tvBsic.setText("BSIC    ");
                            tableRow.addView( tvBsic, 3);

                            TextView tvRSSI = new TextView(this.getContext());
                            tvRSSI.setTextSize(20);
                            tvRSSI.setText("RSSI    ");
                            tableRow.addView(tvRSSI, 4);

                            tableLayout.addView(tableRow, currRow);
                            currRow++;

                            tableRow = new TableRow(this.getContext());
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            TextView tvPscVal = new TextView(this.getContext());
                            tvPscVal.setTextSize(20);
                            tvPscVal.setText(String.valueOf(cellInfoGsm.getCellIdentity().getLac()));
                            tableRow.addView(tvPscVal, 0);

                            TextView tvUarfcnVal = new TextView(this.getContext());
                            tvUarfcnVal.setTextSize(20);
                            tvUarfcnVal.setText("  "+String.valueOf(cellInfoGsm.getCellIdentity().getCid()+"  "));
                            tableRow.addView(tvUarfcnVal, 1);

                            TextView tvdBmVal = new TextView(this.getContext());
                            tvdBmVal.setTextSize(20);
                            tvdBmVal.setText("  "+String.valueOf( cellInfoGsm.getCellIdentity().getArfcn()));
                            tableRow.addView(tvdBmVal, 2);


                            TextView tvBsicVal = new TextView(this.getContext());
                            tvBsicVal.setTextSize(20);
                            tvBsicVal.setText("  "+String.valueOf(cellInfoGsm.getCellIdentity().getBsic()));
                            tableRow.addView(tvBsicVal, 3);


                            TextView tvRssiVal = new TextView(this.getContext());
                            tvRssiVal.setTextSize(20);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                tvRssiVal.setText("  "+String.valueOf( cellInfoGsm.getCellSignalStrength().getRssi()));
                            }
                            tableRow.addView(tvRssiVal, 4);
                            tableLayout.addView(tableRow, currRow);
                            currRow++;
                        }
                    }
                    break;
                default:
            }
        }
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