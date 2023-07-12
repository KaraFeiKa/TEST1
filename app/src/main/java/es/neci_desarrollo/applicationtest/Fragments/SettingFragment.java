package es.neci_desarrollo.applicationtest.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import es.neci_desarrollo.applicationtest.R;
import es.neci_desarrollo.applicationtest.Store;


public class SettingFragment extends Fragment {

    Switch WriteNeighbors;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        WriteNeighbors = view.findViewById(R.id.WriteNeighbors);

        View.OnClickListener writeNeighborsInSore = v -> {
            if (Store.isWriteNeighbors) {
                Store.disableWriteNeighbors();
            } else {
                Store.enableWriteNeighbors();
            }
        };
        WriteNeighbors.setOnClickListener(writeNeighborsInSore);
        return view;
    }


}