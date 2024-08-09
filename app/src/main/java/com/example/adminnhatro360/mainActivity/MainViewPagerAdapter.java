package com.example.adminnhatro360.mainActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.adminnhatro360.mainActivity.dashboard.DashboardFragment;
import com.example.adminnhatro360.mainActivity.manageRoomFragment.ManageRoomFragment;
import com.example.adminnhatro360.mainActivity.manageUserFragment.ManageUserFragment;

//import com.example.adminnhatro360.controller.PostFragment;
//import com.example.adminnhatro360.controller.mainActivity.fragmentAccount.AccountFragment;
//import com.example.adminnhatro360.controller.mainActivity.fragmentSearch.DetailListFragment;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment[] fragments;

    public MainViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        fragments = new Fragment[]{
                new DashboardFragment(),
                new ManageRoomFragment(),
                new ManageUserFragment()
//                new AccountFragment()
        };
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    public void reloadFragment(int position) {
        fragments[position] = createFragment(position);
        notifyDataSetChanged();
    }

    private Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DashboardFragment();
            case 1:
                return new ManageRoomFragment();
            case 2:
                return new ManageUserFragment();
//            case 3:
//                return new AccountFragment();
            default:
                return new DashboardFragment();
        }
    }
}
