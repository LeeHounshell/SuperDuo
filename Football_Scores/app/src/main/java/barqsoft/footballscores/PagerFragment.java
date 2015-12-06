package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class PagerFragment extends Fragment {
    private final static String TAG = "LEE: <" + PagerFragment.class.getSimpleName() + ">";

    private static final int NUM_PAGES = 5;
    private final MainScreenFragment[] mViewFragments = new MainScreenFragment[5];

    public ViewPager mPagerHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        MyPageAdapter mPagerAdapter;
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new MyPageAdapter(getChildFragmentManager());
        Locale locale = getResources().getConfiguration().locale;
        for (int i = 0; i < NUM_PAGES; i++) {
            Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);
            mViewFragments[i] = new MainScreenFragment();
            mViewFragments[i].setFragmentDate(format.format(fragmentDate));
        }
        mPagerHandler.setAdapter(mPagerAdapter);
        Log.v(TAG, "--> mPagerHandler.setCurrentItem(MainActivity.sCurrentFragment="+MainActivity.sCurrentFragment+");");
        mPagerHandler.setCurrentItem(MainActivity.sCurrentFragment);
        return rootView;
    }

    private class MyPageAdapter extends FragmentStatePagerAdapter {
        private final String TAG = "LEE: <" + MyPageAdapter.class.getSimpleName() + ">";

        @Override
        public Fragment getItem(int i) {
            //Log.v(TAG, "getItem");
            return mViewFragments[i];
        }

        @Override
        public int getCount() {
            //Log.v(TAG, "getCount");
            return NUM_PAGES;
        }

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
            Log.v(TAG, "MyPageAdapter");
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            Log.v(TAG, "getPageTitle");
            return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * 86400000));
        }

        public String getDayName(Context context, long dateInMillis) {
            Log.v(TAG, "getDayName");
            // If the date is today, return the localized version of "Today" instead of the actual day name.
            // FIXED: replace deprecated Time with GregorianCalendar
            GregorianCalendar gc = new GregorianCalendar();
            int currentJulianDay = gc.get(GregorianCalendar.DAY_OF_YEAR);
            gc.setTimeInMillis(dateInMillis);
            int julianDay = gc.get(GregorianCalendar.DAY_OF_YEAR);
            Log.v(TAG, "TIME: julianDay="+julianDay+", currentJulianDay="+currentJulianDay);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                return context.getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                return context.getString(R.string.yesterday);
            } else {
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                String[] weekdays = new DateFormatSymbols().getWeekdays(); // Get day names
                return weekdays[gc.get(GregorianCalendar.DAY_OF_WEEK)];
            }
        }
    }

}
