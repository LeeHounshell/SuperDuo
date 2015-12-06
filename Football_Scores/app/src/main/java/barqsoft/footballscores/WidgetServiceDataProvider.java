package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


//@SuppressLint("NewApi")
// from: http://dharmangsoni.blogspot.com/2014/03/collection-widget-with-event-handling.html
public class WidgetServiceDataProvider implements RemoteViewsFactory {
    private final static String TAG = "LEE: <" + WidgetServiceDataProvider.class.getSimpleName() + ">";

    private Context mContext = null;
    private Cursor cursor = null;

    @SuppressWarnings("unused")
    public WidgetServiceDataProvider(Context context, Intent intent) {
        Log.v(TAG, "WidgetServiceDataProvider");
        mContext = context;
    }

    @Override
    public int getCount() {
        Log.v(TAG, "getCount");
        return (cursor != null) ? cursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        Log.v(TAG, "getLoadingView");
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.v(TAG, "getViewAt: position="+position);
        if (position == AdapterView.INVALID_POSITION) {
            Log.w(TAG, "AdapterView.INVALID_POSITION");
            return null;
        }
        if (cursor == null || ! cursor.moveToPosition(position)) {
            Log.w(TAG, "invalid cursor");
            return null;
        }

        String homeTeam = cursor.getString(cursor.getColumnIndex(DatabaseContract.ScoresTable.HOME_COL));
        int homeScore =  cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresTable.HOME_GOALS_COL));
        String awayTeam = cursor.getString(cursor.getColumnIndex(DatabaseContract.ScoresTable.AWAY_COL));
        int awayScore =  cursor.getInt(cursor.getColumnIndex(DatabaseContract.ScoresTable.AWAY_GOALS_COL));
        String vs = mContext.getResources().getString(R.string.vs);
        String colon = mContext.getResources().getString(R.string.colon);
        String gameInfo = homeTeam + " " + vs + " " + awayTeam;
        String scoreInfo = Utilities.getScores(homeScore, awayScore) + colon + " ";
        Log.v(TAG, "scoreInfo=" + scoreInfo + ", gameInfo=" + gameInfo);

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), android.R.layout.simple_list_item_1);
        remoteViews.setTextViewText(android.R.id.text1, scoreInfo + " " + gameInfo);
        remoteViews.setTextColor(android.R.id.text1, Color.WHITE);

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(FootballScoresWidgetProvider.ACTION_WIDGET_ITEM_CLICK);
        final Bundle bundle = new Bundle();
        bundle.putString(FootballScoresWidgetProvider.EXTRA_STRING, String.valueOf(position));
        fillInIntent.putExtras(bundle);
        remoteViews.setOnClickFillInIntent(android.R.id.text1, fillInIntent);
        return remoteViews;
    }

    @Override
    public int getViewTypeCount() {
        Log.v(TAG, "getViewTypeCount");
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        Log.v(TAG, "hasStableIds");
        return true;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG, "onDataSetChanged");
        if (cursor != null) {
            cursor.close();
        }

        // from: http://stackoverflow.com/questions/21275898/securityexception-with-granturipermission-when-sharing-a-file-with-fileprovider
        final long token = Binder.clearCallingIdentity();
        try {
            Log.v(TAG, "--> QUERY FOR WIDGET DATA <--");
            Uri scoreUriForDate = DatabaseContract.ScoresTable.buildScoreWithDate();
            Log.v(TAG, "scoreUriForDate=" + scoreUriForDate);
            long timeToday = System.currentTimeMillis();
            long timeYesterday = timeToday - TimeUnit. MILLISECONDS.convert(1, TimeUnit. DAYS);
            Date yesterday = new Date(timeYesterday);
            Locale locale = mContext.getResources().getConfiguration().locale;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", locale);
            String[] queryForDate = new String[1];
            queryForDate[0] = dateFormat.format(yesterday);
            cursor = mContext.getContentResolver()
                    .query(scoreUriForDate,
                            DatabaseContract.ScoresTable.getScoresTableColumnsForWidget(),
                            DatabaseContract.getPathDate(),
                            queryForDate,
                            null
                    );
            Log.v(TAG, "cursor=" + cursor);
        }
        finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

}
