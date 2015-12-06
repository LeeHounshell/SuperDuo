package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import it.jaschke.alexandria.api.NavigationDrawerAdapter;


/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
    private final static String TAG = "LEE: <" + NavigationDrawerFragment.class.getSimpleName() + ">";

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = RecyclerView.NO_POSITION;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
        Log.v(TAG, "NavigationDrawerFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mCurrentSelectedPosition = prefs.getInt("pref_startFragment", 0);
            selectItem(mCurrentSelectedPosition);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

        // FIXED: set up the drawer.
        MainActivity activity = (MainActivity) getActivity();
        DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            drawerLayout = activity.getDrawerLayout();
        }
        if (drawerLayout != null) {
            setUp(R.id.navigation_drawer, drawerLayout);
        } else {
            Log.e(TAG, "*** UNABLE TO GET DRAWER LAYOUT ***");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        // 1. get a reference to the drawer recyclerView
        RecyclerView mDrawerRecyclerView = (RecyclerView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mDrawerRecyclerView.setHasFixedSize(true);
        TypedArray drawerMenuIcons = getResources().obtainTypedArray(R.array.navDrawerIcons);
        String drawerMenuItems[] = getResources().getStringArray(R.array.navDrawerItems);

        // initialize menu data for the RecyclerView
        DrawerItemData drawerItemsData[] = new DrawerItemData[drawerMenuItems.length];

        int index = 0;
        for (String menuItem : drawerMenuItems) {
            int menuIcon = drawerMenuIcons.getResourceId(index, 0);
            drawerItemsData[index] = new DrawerItemData(menuItem, menuIcon);
            ++index;
        }

        // 2. set layoutManger
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 3. create an adapter
        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(drawerItemsData);
        // 4. set adapter
        mDrawerRecyclerView.setAdapter(navigationDrawerAdapter);
        // 5. set item animator to DefaultAnimator
        mDrawerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 6. set the fragment for click processing
        navigationDrawerAdapter.setNavigationDrawerFragment(this);
        // 7. cleanup
        drawerMenuIcons.recycle();

        return mDrawerRecyclerView;
    }

    public boolean isDrawerOpen() {
        Log.v(TAG, "isDrawerOpen");
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        Log.v(TAG, "--> Drawer setUp <--");

        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // FIXED: popdown the keyboard
        hide_keyboard();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        // FIXED: provide up navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        // FIXED: convert deprecated v4 ActionBarDrawerToggle to v7
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close         /* "close drawer" description for accessibility */
        ) {

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.v(TAG, "onDrawerClosed");
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    Log.v(TAG, "!isAdded");
                    return;
                }
                Log.v(TAG, "isAdded ok");
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.v(TAG, "onDrawerOpened");

                // FIXED: popdown the keyboard
                hide_keyboard();

                super.onDrawerOpened(drawerView);

                if (!isAdded()) {
                    Log.v(TAG, "!isAdded");
                    return;
                }
                Log.v(TAG, "isAdded ok");
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                    Log.v(TAG, "user learned about the drawer");
                }
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            Log.v(TAG, "user has NOT learned about the drawer yet");
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "mDrawerToggle.syncState();");
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void hide_keyboard() {
        Log.v(TAG, "hide_keyboard");
        mCallbacks.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "HIDE THE KEYBOARD");
                mCallbacks.hide_keyboard();
            }
        });
    }

    public void selectItem(int position) {
        Log.v(TAG, "selectItem");
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    // FIXED: onAttach(Activity activity) is deprecated
    @Override
    public void onAttach(Context context) {
        Log.v(TAG, "onAttach");
        super.onAttach(context);
        try {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                mCallbacks = (NavigationDrawerCallbacks) activity;
            }
            else {
                Log.w(TAG, "expected context instanceof Activity");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        Log.v(TAG, "onDetach");
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.v(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu");
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.main, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        Log.v(TAG, "showGlobalContextActionBar");
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        // FIXED: remove deprecated ActionBar.NAVIGATION_MODE_STANDARD
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        Log.v(TAG, "getActionBar");
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        Handler getHandler();
        void onNavigationDrawerItemSelected(int position);
        void hide_keyboard();
    }

}
