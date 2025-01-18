package com.example.cloudcloset;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.cloudcloset.fragments.CalendarFragment;
import com.example.cloudcloset.fragments.CameraFragment;
import com.example.cloudcloset.fragments.GalleryFragment;
import com.example.cloudcloset.fragments.HomeFragment;
import com.example.cloudcloset.fragments.TryOutFragment;
import com.example.cloudcloset.fragments.WardrobeFragment;


public class MyViewPagerAdapter extends FragmentStateAdapter {

    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new CameraFragment();
            case 2:
                return new GalleryFragment();
            case 3:
                return new WardrobeFragment();
            case 4:
                return new CalendarFragment();
            case 5:
                return new TryOutFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6; // Total number of tabs
    }
}