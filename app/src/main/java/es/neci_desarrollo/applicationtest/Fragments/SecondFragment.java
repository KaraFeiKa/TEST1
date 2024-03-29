package es.neci_desarrollo.applicationtest.Fragments;

import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import es.neci_desarrollo.applicationtest.R;


public class SecondFragment extends Fragment {
    private TelephonyManager tm;
    private TableLayout tableLayout;
    CellInfoIDListener cellInfoIDListener;
    int rssi, rsrq, rsrp, ta, band, EARFCN, CELLID, PCI, LAC,
            UARFCN, PSC, ARFCN, BSIC ,ss,TAa= 0;

    public SecondFragment(TelephonyManager tm) {
        this.tm = tm;
    }
    @Override
    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        Neiborhood(cellInfoList);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        tableLayout = (TableLayout) view.findViewById(R.id.tableLayout);
        cellInfoIDListener = new CellInfoIDListener();
        ((TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE)).listen(cellInfoIDListener, CellInfoIDListener.LISTEN_CELL_INFO);

        tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return view;
    }


enum Networks {
        LTE,
    UMTS,
    GSM,
}
    @SuppressLint({"ResourceAsColor", "MissingPermission", "SetTextI18n"})

    private void Neiborhood(List<CellInfo> cellInfoList) {
        tableLayout.removeAllViews();
        int currRow = 0;
        Networks networkHeaders  = Networks.LTE;
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoLte) {
                if (getContext() != null) {
                    TableRow tableRowLte = new TableRow(this.getContext());
                    tableRowLte.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    TextView tvPci = new TextView(this.getContext());
                    tvPci.setTextSize(20);

                    tvPci.setText("PCI   ");
                    tableRowLte.addView(tvPci, 0);

                    TextView tvEarfcn = new TextView(this.getContext());
                    tvEarfcn.setTextSize(20);

                    tvEarfcn.setText("Earfcn   ");
                    tableRowLte.addView(tvEarfcn, 1);

                    TextView tvBand = new TextView(this.getContext());
                    tvBand.setTextSize(20);

                    tvBand.setText("Band  ");
                    tableRowLte.addView(tvBand, 2);

                    TextView tvRssi = new TextView(this.getContext());
                    tvRssi.setTextSize(20);

                    tvRssi.setText("RSSI  ");
                    tableRowLte.addView(tvRssi, 3);

                    TextView tvRsrp = new TextView(this.getContext());
                    tvRsrp.setTextSize(20);

                    tvRsrp.setText("RSRP  ");
                    tableRowLte.addView(tvRsrp, 4);

                    TextView tvRsrq = new TextView(this.getContext());
                    tvRsrq.setTextSize(20);

                    tvRsrq.setText("RSRQ  ");
                    tableRowLte.addView(tvRsrq, 5);

                    TextView tvTa = new TextView(this.getContext());
                    tvTa.setTextSize(20);

                    tvTa.setText("Ta  ");
                    tableRowLte.addView(tvTa, 6);

                    tableLayout.addView(tableRowLte, currRow);
                    currRow++;
                    networkHeaders  = Networks.LTE;
                    break;
                }
            }
                if (cellInfo instanceof CellInfoWcdma) {
                    if (getContext() != null) {
                        TableRow tableRowUMTS = new TableRow(this.getContext());
                        tableRowUMTS.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));

                        TextView tvPsc = new TextView(this.getContext());
                        tvPsc.setTextSize(20);

                        tvPsc.setText("PSC   ");
                        tableRowUMTS.addView(tvPsc, 0);

                        TextView tvUarfcn = new TextView(this.getContext());
                        tvUarfcn.setTextSize(20);

                        tvUarfcn.setText("Uarfcn   ");
                        tableRowUMTS.addView(tvUarfcn, 1);

                        TextView tvDbm = new TextView(this.getContext());
                        tvDbm.setTextSize(20);

                        tvDbm.setText("dBm  ");
                        tableRowUMTS.addView(tvDbm, 2);

                        tableLayout.addView(tableRowUMTS, currRow);
                        currRow++;
                        networkHeaders  = Networks.UMTS;
                        break;
                    }

                }
                    if (cellInfo instanceof CellInfoGsm) {
                        if (getContext() != null) {
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
                            tableRow.addView(tvBsic, 3);

                            TextView tvRSSI = new TextView(this.getContext());
                            tvRSSI.setTextSize(20);

                            tvRSSI.setText("RSSI    ");
                            tableRow.addView(tvRSSI, 4);

                            tableLayout.addView(tableRow, currRow);
                            currRow++;
                            networkHeaders  = Networks.GSM;
                            break;
                        }
                    }
                }

        for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte && networkHeaders==Networks.LTE) {
                        CellInfoLte cellInfoLte = ((CellInfoLte) cellInfo);
                        if (!cellInfoLte.isRegistered()) {
                            if (getContext() != null)
                            {
                                TableRow tableRowValues = new TableRow(this.getContext());
                                tableRowValues.setLayoutParams(new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        TableLayout.LayoutParams.WRAP_CONTENT));

                                TextView tvPciVal = new TextView(this.getContext());
                                tvPciVal.setTextSize(20);

                                PCI = cellInfoLte.getCellIdentity().getPci();
                                tvPciVal.setText(String.valueOf(PCI));
                                tableRowValues.addView(tvPciVal, 0);

                                TextView tvEarfcnVal = new TextView(this.getContext());
                                tvEarfcnVal.setTextSize(20);

                                EARFCN = cellInfoLte.getCellIdentity().getEarfcn();
                                tvEarfcnVal.setText(String.valueOf(EARFCN));
                                tableRowValues.addView(tvEarfcnVal, 1);



                                band = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    int[] bands = cellInfoLte.getCellIdentity().getBands();
                                    TextView tvBandVal = new TextView(this.getContext());
                                    tvBandVal.setTextSize(20);

                                    tvBandVal.setText(Arrays.stream(bands).mapToObj(String::valueOf)
                                            .collect(Collectors.joining(", ")));
                                    if (bands.length > 0) {
                                        band = bands[0];
                                    }
                                    tableRowValues.addView(tvBandVal, 2);
                                }
                                else {
                                    TextView tvBandVal = new TextView(this.getContext());
                                    tvBandVal.setTextSize(20);

                                    tvBandVal.setText("N/a");
                                    tableRowValues.addView(tvBandVal, 2);
                                }


                                TextView tvRssiVal = new TextView(this.getContext());
                                tvRssiVal.setTextSize(20);

                                rssi = cellInfoLte.getCellSignalStrength().getRssi();
                                if (rssi != Integer.MAX_VALUE)
                                {
                                    tvRssiVal.setText(String.valueOf(rssi));
                                }else {
                                    tvRssiVal.setText("N/a");
                                }

                                tableRowValues.addView(tvRssiVal, 3);

                                TextView tvRsrpVal = new TextView(this.getContext());
                                tvRsrpVal.setTextSize(20);

                                rsrp = cellInfoLte.getCellSignalStrength().getRsrp();
                                if (rsrp != Integer.MAX_VALUE)
                                {
                                    tvRsrpVal.setText(String.valueOf(rsrp));
                                }else {
                                    tvRsrpVal.setText("N/a");
                                }
                                tableRowValues.addView(tvRsrpVal, 4);

                                TextView tvRsrqVal = new TextView(this.getContext());
                                tvRsrqVal.setTextSize(20);

                                rsrq = cellInfoLte.getCellSignalStrength().getRsrq();
                                if (rsrq != Integer.MAX_VALUE)
                                {
                                    tvRsrqVal.setText(String.valueOf(rsrq));
                                }else {
                                    tvRsrqVal.setText("N/a");
                                }

                                tableRowValues.addView(tvRsrqVal, 5);
                                TextView tvTaVal = new TextView(this.getContext());
                                tvTaVal.setTextSize(20);

                                ta = cellInfoLte.getCellSignalStrength().getTimingAdvance();
                                if (ta != Integer.MAX_VALUE) {
                                    TAa = ta;
                                    tvTaVal.setText(String.valueOf(TAa));
                                } else {
                                    tvTaVal.setText("N/a");
                                }
                                tableRowValues.addView(tvTaVal, 6);
                                tableLayout.addView(tableRowValues, currRow);
                                currRow++;
                            }
                        }
                    }
                    if (cellInfo instanceof CellInfoWcdma && networkHeaders==Networks.UMTS) {
                        CellInfoWcdma cellInfoWcdma = ((CellInfoWcdma) cellInfo);
                        if (cellInfoWcdma.isRegistered() == false) {
                            if (getContext() != null)
                            {
                                TableRow tableRowValues = new TableRow(this.getContext());
                                tableRowValues.setLayoutParams(new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        TableLayout.LayoutParams.WRAP_CONTENT));

                                TextView tvPscVal = new TextView(this.getContext());
                                tvPscVal.setTextSize(20);

                                PSC =cellInfoWcdma.getCellIdentity().getPsc();
                                tvPscVal.setText(String.valueOf(PSC));
                                tableRowValues.addView(tvPscVal, 0);

                                TextView tvUarfcnVal = new TextView(this.getContext());
                                tvUarfcnVal.setTextSize(20);

                                UARFCN = cellInfoWcdma.getCellIdentity().getUarfcn();
                                tvUarfcnVal.setText(String.valueOf(UARFCN));
                                tableRowValues.addView(tvUarfcnVal, 1);

                                TextView tvdBmVal = new TextView(this.getContext());
                                String[] CellSignalStrengthArr = cellInfoWcdma.getCellSignalStrength().toString().split(" ");
                                ss = 0;
                                if(CellSignalStrengthArr.length>1) {
                                    String[] elem = CellSignalStrengthArr[1].split("=");
                                    if (elem[0].contains("ss")) {
                                        ss = Integer.parseInt(elem[1]);
                                    }
                                }
                                tvdBmVal.setTextSize(20);

                                tvdBmVal.setText(String.valueOf(ss));
                                tableRowValues.addView(tvdBmVal, 2);
                                tableLayout.addView(tableRowValues, currRow);
                                currRow++;
                            }
                        }
                    }
                    if (cellInfo instanceof CellInfoGsm && networkHeaders==Networks.GSM) {
                        CellInfoGsm cellInfoGsm = ((CellInfoGsm) cellInfo);
                        if (cellInfoGsm.isRegistered() == false) {
                            if (getContext() != null)
                            {
                                TableRow tableRow = new TableRow(this.getContext());
                                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.MATCH_PARENT,
                                        TableLayout.LayoutParams.WRAP_CONTENT));

                                TextView tvPscVal = new TextView(this.getContext());
                                tvPscVal.setTextSize(20);

                                LAC = cellInfoGsm.getCellIdentity().getLac();
                                tvPscVal.setText(String.valueOf(LAC));
                                tableRow.addView(tvPscVal, 0);

                                TextView tvUarfcnVal = new TextView(this.getContext());
                                tvUarfcnVal.setTextSize(20);

                                CELLID = (cellInfoGsm.getCellIdentity().getCid());
                                tvUarfcnVal.setText("  "+CELLID+"  ");
                                tableRow.addView(tvUarfcnVal, 1);

                                TextView tvdBmVal = new TextView(this.getContext());
                                tvdBmVal.setTextSize(20);

                                ARFCN = cellInfoGsm.getCellIdentity().getArfcn();
                                tvdBmVal.setText("  "+ARFCN);
                                tableRow.addView(tvdBmVal, 2);


                                TextView tvBsicVal = new TextView(this.getContext());

                                tvBsicVal.setTextSize(20);
                                BSIC = cellInfoGsm.getCellIdentity().getBsic();
                                tvBsicVal.setText("  "+(BSIC));
                                tableRow.addView(tvBsicVal, 3);


                                TextView tvRssiVal = new TextView(this.getContext());
                                tvRssiVal.setTextSize(20);


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    rssi = cellInfoGsm.getCellSignalStrength().getRssi();
                                    tvRssiVal.setText("  "+( rssi));
                                }
                                tableRow.addView(tvRssiVal, 4);
                                tableLayout.addView(tableRow, currRow);
                                currRow++;
                            }
                        }
                    }
        }
    }

    private class CellInfoIDListener extends PhoneStateListener {
        @Override
        @SuppressLint({"SetTextI18n", "MissingPermission"})
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            Neiborhood(cellInfoList);
            super.onCellInfoChanged(cellInfoList);
        }
    }
}