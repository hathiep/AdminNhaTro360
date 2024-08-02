package com.example.adminnhatro360.controller.mainActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.adminnhatro360.controller.mainActivity.manageRoomFragment.ManageRoomFragment;

//import com.example.adminnhatro360.controller.PostFragment;
//import com.example.adminnhatro360.controller.mainActivity.fragmentAccount.AccountFragment;
//import com.example.adminnhatro360.controller.mainActivity.fragmentSearch.DetailListFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment[] fragments;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        fragments = new Fragment[]{
                new ManageRoomFragment()
//                new SearchFragment(),
//                new PostFragment(),
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
                return new ManageRoomFragment();
//            case 1:
//                return new SearchFragment();
//            case 2:
//                return new PostFragment();
//            case 3:
//                return new AccountFragment();
            default:
                return new ManageRoomFragment();
        }
    }
}
