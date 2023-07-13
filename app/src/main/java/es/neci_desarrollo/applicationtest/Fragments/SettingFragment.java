package es.neci_desarrollo.applicationtest.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.Store;


public class SettingFragment extends Fragment {


    SeekBar seekBar;
    Switch WriteNeighbors;
    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        WriteNeighbors = view.findViewById(R.id.WriteNeighbors);
        seekBar = view.findViewById(R.id.seekBar);
        textView = view.findViewById(R.id.seekBarValue);
        textView.setText("7");
        View.OnClickListener writeNeighborsInSore = v -> {
            if (Store.isWriteNeighbors) {
                Store.disableWriteNeighbors();
            } else {
                Store.enableWriteNeighbors();
            }
        };
        WriteNeighbors.setOnClickListener(writeNeighborsInSore);
        seekBar.setProgress(Store.range);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Store.setRange(seekBar.getProgress());
                Log.d("Settings","Bar: "+Store.range);
            }
        });
        return view;

    }


}