package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


// FIXED: inherit from AppCompatActivity instead of ActionBarActivity
public class MainActivity
        extends AppCompatActivity
        implements
            NavigationDrawerFragment.NavigationDrawerCallbacks,
            ListOfBooksFragment.ListOfBooksCallbacks
{
    private final static String TAG = "LEE: <" + MainActivity.class.getSimpleName() + ">";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ListOfBooksFragment mListOfBooksFragment;
    private String drawerMenuItems[];

    /**
     * Used to store the last screen mTitle. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private BroadcastReceiver mMessageReceiver;
    private Handler mHandler;
    private DrawerLayout mDrawerLayout;
    private boolean mSelectingDrawerItem;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static boolean bookDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        drawerMenuItems = getResources().getStringArray(R.array.navDrawerItems);
        if (isTablet()) {
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark));

        // FIXED: suppress keyboard until it is used
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // FIXED: defer drawer setUp until NavigationDrawerFragment.onActivityCreated
    }

    //from: http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    public void hide_keyboard() {
        Log.v(TAG, "---> hide_keyboard <---");
        Activity activity = this;
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.v(TAG, "onNavigationDrawerItemSelected - position="+position);
        mSelectingDrawerItem = true;

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        // FIXED: make the delete button INVISIBLE until a book is selected
        if (findViewById(R.id.delete_button) != null) {
            Log.v(TAG, "make the delete button INVISIBLE");
            Log.v(TAG, "onNavigationDrawerItemSelected - findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);");
            findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
        }

        // FIXED: make the initial default SCAN instead of LIST, as no books exist yet
        switch (position) {
            default:
            case 0:
                Log.v(TAG, "SCAN");
                AddBook addBook = new AddBook();
                addBook.clearRightContainer(this);
                nextFragment = addBook;
                break;
            case 1:
                Log.v(TAG, "LIST");
                nextFragment = createListOfBooksFragment();
                break;
            case 2:
                Log.v(TAG, "ABOUT");
                About about = new About();
                about.clearRightContainer(this);
                nextFragment = about;
                break;
        }

        updateFragment(position, fragmentManager, nextFragment);
    }

    private void updateFragment(int position, FragmentManager fragmentManager, Fragment nextFragment) {
        Log.v(TAG, "updateFragment - position="+position);
        Log.v(TAG, "position=" + position + ", fragmentManager.replace(R.id.container, nextFragment)");
        /*
        // handy for debugging the back stack:
        int count = fragmentManager.getBackStackEntryCount();
        String title = "#"+count+": "+drawerMenuItems[position];
        */
        String title = drawerMenuItems[position];
        Log.v(TAG, "---> REPLACE CONTAINER: fragment title="+title);
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack(title)
                .commit();
    }

    private ListOfBooksFragment createListOfBooksFragment() {
        Log.v(TAG, "createListOfBooksFragment");
        if (mListOfBooksFragment != null) {
            mListOfBooksFragment.closeDown();
        }
        mListOfBooksFragment = new ListOfBooksFragment();
        return mListOfBooksFragment;
    }

    public void setTitle(int titleId) {
        Log.v(TAG, "setTitle");
        mTitle = getString(titleId);
    }

    private void restoreActionBar() {
        Log.v(TAG, "restoreActionBar");
        ActionBar actionBar = getSupportActionBar();
        try {
            // FIXED: remove deprecated ActionBar.NAVIGATION_MODE_STANDARD
            //noinspection ConstantConditions
            actionBar.setDisplayShowTitleEnabled(true);
            // FIXED: provide up navigation
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mTitle);
        }
        catch (NullPointerException e) {
            Log.e(TAG, "got NullPointerException e="+e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "onCreateOptionsMenu");
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            alertSettingsChoices();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // FIXED: the old Settings Activity was awkward - replace it with a simple alert dialog
    // from: https://www.codeofaninja.com/2011/07/android-alertdialog-example.html
    private void alertSettingsChoices() {

        final Activity activity = this;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int default_screen = prefs.getInt("pref_startFragment", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Set the dialog title
        builder.setTitle(getResources().getString(R.string.startscreen_settings))

            .setSingleChoiceItems(R.array.pref_start_options, default_screen, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Log.v(TAG, "CLICK! - selected index: " + arg1);
                }
            })

            .setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // user clicked OK, so save the selected choice results in SharedPreferences
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    Log.v(TAG, "selectedPosition: " + selectedPosition);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("pref_startFragment", selectedPosition);
                    editor.apply();
                }
            })

            .setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // remove the dialog from the screen
                    Log.v(TAG, "Cancel");
            }
        })

        .show();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if (mListOfBooksFragment != null) {
            mListOfBooksFragment.closeDown();
        }
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Log.v(TAG, "---> onItemSelected: ean=" + ean);
        displayBookInfo(ean);
    }

    private BookDetail createBookDetail(String ean) {
        Log.v(TAG, "createBookDetail");
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);
        BookDetail bookDetail = new BookDetail();
        bookDetail.setArguments(args);
        return bookDetail;
    }

    private void displayBookInfo(String ean) {
        Log.v(TAG, "---> displayBookInfo: ean=" + ean);
        BookDetail bookDetailFragment = createBookDetail(ean);
        int id = R.id.container;

        // RIGHT CONTAINER
        if (findViewById(R.id.right_container) != null) {
            Log.v(TAG, "RIGHT CONTAINER");
            id = R.id.right_container;
        }
        if (findViewById(R.id.right_container) != null) {
            Log.v(TAG, "displayBookInfo - set right_container VISIBLE");
            findViewById(id).setVisibility(View.VISIBLE);
        }

        // from:  http://stackoverflow.com/questions/16688900/fragment-unknown-animation-name-objectanimator
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.v(TAG, "ANIMATE: ean=" + ean + ", fragmentManager.replace(id, bookDetailFragment)");
        String book_detail = getResources().getString(R.string.misc_book_detail);
        Log.v(TAG, "---> REPLACE CONTAINER: id="+id);
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(id, bookDetailFragment)
                .addToBackStack(book_detail)
                .commit();
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive");
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();

                // also vibrate the device to signal message receipt
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500); // Vibrate for 500 milliseconds
            }
        }
    }

    private boolean isTablet() {
        Log.v(TAG, "isTablet");
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // FIXED: correctly handle onBackPressed
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Log.v(TAG, "onBackPressed - count="+count);
        if (count <= 1) {
            Log.v(TAG, "onBackPressed - finish");
            finish();
        }
        else {
            String title = fragmentManager.getBackStackEntryAt(count-2).getName();
            if (count == 2 && mNavigationDrawerFragment.getView() != null) {
                Log.v(TAG, "onBackPressed - open the drawer");
                mDrawerLayout.openDrawer(mNavigationDrawerFragment.getView());
                title = getResources().getString(R.string.app_name);
            }
            super.onBackPressed();
            Log.v(TAG, "onBackPressed - title="+title);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
        // FIXED: remove delete book from display list
        if (MainActivity.bookDeleted) {
            MainActivity.bookDeleted = false;
            // update the display list, to handle DELETE case
            int position = 1;
            String title = drawerMenuItems[position];
            Log.v(TAG, "---> REPLACE CONTAINER: fragment title=" + title);
            Fragment nextFragment = createListOfBooksFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nextFragment)
                    .addToBackStack(title)
                    .commit();
            updateFragment(position, fragmentManager, nextFragment);
        }
    }

    public DrawerLayout getDrawerLayout() {
        Log.v(TAG, "getDrawerLayout");
        return mDrawerLayout;
    }

    public void onLoaderReset() {
        Log.v(TAG, "onLoaderReset");
        if (mNavigationDrawerFragment != null
                && mNavigationDrawerFragment.getView() != null
                && mDrawerLayout != null
                && !mSelectingDrawerItem)
        {
            Log.v(TAG, "onLoaderReset - OPEN THE DRAWER");
            // FIXED: open drawer from BookDetail when onBackPressed (instead of showing empty book list or prior book detail)
            mDrawerLayout.openDrawer(mNavigationDrawerFragment.getView());
        }
        mSelectingDrawerItem = false;
    }

    public Handler getHandler() {
        return mHandler;
    }

}
