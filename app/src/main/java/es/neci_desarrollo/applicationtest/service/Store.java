package es.neci_desarrollo.applicationtest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.opencsv.CSVWriter;

import es.neci_desarrollo.applicationtest.Fragments.HomeFragment;

public class Store  {
    public Store() {

    }
    public static boolean isWriteNeighbors;
    public static boolean isWriteWorking;
    public static int range;
    public static CSVWriter writerN;

    public static void setRange(int range) {
        Store.range = range;
    }
    public static void enableWriteNeighbors() {
        Store.isWriteNeighbors= true;
    }
    public static void disableWriteNeighbors() {
        Store.isWriteNeighbors= false;
    }
    public static void enableWrite() {
        Store.isWriteWorking= true;
    }
    public static void disableWrite() {
        Store.isWriteWorking= false;
    }

}