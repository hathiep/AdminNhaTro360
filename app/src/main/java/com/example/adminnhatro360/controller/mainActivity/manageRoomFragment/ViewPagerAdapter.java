package com.example.adminnhatro360.controller.mainActivity.manageRoomFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

//import com.example.adminnhatro360.controller.PostFragment;
//import com.example.adminnhatro360.controller.mainActivity.fragmentAccount.AccountFragment;
//import com.example.adminnhatro360.controller.mainActivity.fragmentSearch.SearchFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public ViewPagerAdapter(FragmentManager childFragmentManager, int[] imageResourcesArray) {
        super(childFragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
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

    @Override
    public int getCount() {
        return 4;
    }
}
