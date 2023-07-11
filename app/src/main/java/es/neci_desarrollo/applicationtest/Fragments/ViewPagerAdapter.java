package es.neci_desarrollo.applicationtest.Fragments;


import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    TelephonyManager tm;
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, TelephonyManager tm) {
        super(fragmentActivity);
        this.tm = tm;
    }

//    private HomeFragment homeFragment;
//    public void UpDate(){
//        homeFragment = null;
//    homeFragment = new HomeFragment(this.tm);
//    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
//        homeFragment = new HomeFragment(this.tm);
        switch (position)
        {
            case 0: return new HomeFragment(this.tm);
            case 1: return new SecondFragment(this.tm);
            case 2: return new SettingFragment();
            default: return new HomeFragment(this.tm);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
