package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.service.MyFetchService;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + MainScreenFragment.class.getSimpleName() + ">";

    private static final int SCORES_LOADER = 0;
    private final String[] mFragmentDate = new String[1];
    private ScoresAdapter mAdapter;
    private ListView mScoreList;

    public MainScreenFragment() {
        Log.v(TAG, "MainScreenFragment");
    }

    private void updateScores() {
        Log.v(TAG, "updateScores");
        Intent service_start = new Intent(getActivity(), MyFetchService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        Log.v(TAG, "setFragmentDate");
        mFragmentDate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        updateScores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mScoreList = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        mScoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.mDetailMatchId = MainActivity.sSelectedMatchId;
        mScoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "---> CLICK! position="+position);
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.mDetailMatchId = selected.matchId;
                MainActivity.sSelectedMatchId = (int) selected.matchId;
                mAdapter.notifyDataSetChanged();
                // FIXED: scroll to the selected item (the first or last item's detail may be hidden)
                mScoreList.smoothScrollToPosition(position);
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "onCreateLoader");
        return new CursorLoader(getActivity(), DatabaseContract.ScoresTable.buildScoreWithDate(),
                null, null, mFragmentDate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(TAG, "onLoadFinished");
        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);

        // from: http://stackoverflow.com/questions/9820175/android-how-to-tap-listview-item-programmatically
        if (mScoreList != null && MainActivity.sOpenSelectedMatchId) {
            Log.v(TAG, "---> OPEN SELECTED MATCH FROM WIDGET CLICK: "+MainActivity.sSelectedMatchId+" <---");
            MainActivity.sOpenSelectedMatchId = false;
            final int position = MainActivity.sSelectedMatchId;
            mScoreList.performItemClick(
                    mScoreList.getAdapter().getView(position, null, null),
                    position,
                    mScoreList.getAdapter().getItemId(position));
            // try to scroll to the selected item
            // see: http://stackoverflow.com/questions/11431832/android-smoothscrolltoposition-not-working-correctly
            if (mScoreList.getTop() != 0) {
                mScoreList.post(new Runnable() {
                    @Override
                    public void run() {
                        mScoreList.smoothScrollToPositionFromTop(position, 0);
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }

}
