package rmit.ad.rmitrides;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private ViewPager pager;
    private TextView titleText;
    private Toolbar toolbar;

    public HistoryFragment() {
        // Required empty public constructor
    }

    //Adapter of the pager
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PassengerHistoryFragment();
                case 1:
                    return new DriverHistoryFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Passenger Rides";
                case 1:
                    return "Driver Rides";
            }
            return null;
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_history, container, false);

        //Initialize views
        titleText = layout.findViewById(R.id.title_text);
        toolbar = layout.findViewById(R.id.toolbar);
        pager = layout.findViewById(R.id.pager);

        //Set title for toolbar
        toolbar.setTitle("");
        titleText.setText("History");

        //Attach the SectionsPagerAdapter to the ViewPager
        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        pager = layout.findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);

        //Attach the ViewPager to the TabLayout
        TabLayout tabLayout = layout.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);

        return layout;
    }

}
