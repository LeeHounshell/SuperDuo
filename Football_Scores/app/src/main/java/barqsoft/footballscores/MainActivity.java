package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "LEE: <" + MainActivity.class.getSimpleName() + ">";
    private final static String SAVE_TAG = "Save Test";
    private PagerFragment myMain;

    public static int sSelectedMatchId = 0;
    public static int sCurrentFragment = 2; // today
    public static boolean sOpenSelectedMatchId = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.d(TAG, "Reached MainActivity onCreate");

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            myMain = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, myMain)
                    .commit();
        }

        // check if a widget list item was clicked..
        sOpenSelectedMatchId = false;
        if (getIntent().getExtras() != null) {
            String widgetClickItem = getIntent().getExtras().getString("OPEN_SELECTED_GAME");
            if (widgetClickItem != null) {
                Log.v(TAG, "--> OPEN_SELECTED_GAME: " + widgetClickItem);
                sSelectedMatchId = Integer.valueOf(widgetClickItem);
                sCurrentFragment = 1; // yesterday
                sOpenSelectedMatchId = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as a parent activity is in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent startAbout = new Intent(this, AboutActivity.class);
            startActivity(startAbout);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState");
        Log.v(SAVE_TAG, "will save");
        Log.v(SAVE_TAG, "fragment: " + String.valueOf(myMain.mPagerHandler.getCurrentItem()));
        Log.v(SAVE_TAG, "selected id: " + sSelectedMatchId);
        outState.putInt("Pager_Current", myMain.mPagerHandler.getCurrentItem());
        outState.putInt("Selected_match", sSelectedMatchId);
        getSupportFragmentManager().putFragment(outState, "myMain", myMain);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(TAG, "onRestoreInstanceState");
        Log.v(SAVE_TAG, "fragment: " + String.valueOf(savedInstanceState.getInt("Pager_Current")));
        Log.v(SAVE_TAG, "selected id: " + savedInstanceState.getInt("Selected_match"));
        sCurrentFragment = savedInstanceState.getInt("Pager_Current");
        sSelectedMatchId = savedInstanceState.getInt("Selected_match");
        myMain = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, "myMain");
        super.onRestoreInstanceState(savedInstanceState);
    }

}
