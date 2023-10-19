package es.neci_desarrollo.applicationtest;

import com.opencsv.CSVWriter;

import es.neci_desarrollo.applicationtest.Fragments.HomeFragment;

public class Store  {
    public Store() {

    }
    public static int selectedTab;
    public static boolean isWriteNeighbors;
    public static boolean isWriteWorking;
    public static boolean isWriteWorkingBackground;

    public static String LastNameFile;

    public static String Pass = "romaromaroman";
    public static boolean isAuth;
    public static String LastName = "roma";
    public static int range = 7;
    public static CSVWriter writerN;
    public static void setLastName(String Lastname)
    {
        Store.LastName = Lastname;
    }
    public static void setPass(String Pass)
    {
        Store.Pass = Pass;
    }
public static void setLastNameFile(String name)
{
    Store.LastNameFile = name;
}
    public static void setRange(int range) {
        Store.range = range;
        HomeFragment.updateRangeLocation();
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
    public static void successAuth(){Store.isAuth = true;}
    public static void unsuccessAuth(){Store.isAuth = false;}

}