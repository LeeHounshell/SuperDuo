package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

class ScoresAdapter extends CursorAdapter {
    private final static String TAG = "LEE: <" + ScoresAdapter.class.getSimpleName() + ">";

    private static final int COL_HOME = 3;
    private static final int COL_AWAY = 4;
    private static final int COL_HOME_GOALS = 6;
    private static final int COL_AWAY_GOALS = 7;
    private static final int COL_LEAGUE = 5;
    private static final int COL_MATCHDAY = 9;
    private static final int COL_ID = 8;
    private static final int COL_MATCHTIME = 2;
    public double mDetailMatchId = 0;

    @SuppressWarnings("unused")
    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        //Log.v(TAG, "ScoresAdapter");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Log.v(TAG, "newView");
        View item = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder holder = new ViewHolder(item);
        item.setTag(holder);
        return item;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //Log.v(TAG, "bindView");
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.mHomeName.setText(cursor.getString(COL_HOME));
        holder.mAwayName.setText(cursor.getString(COL_AWAY));
        holder.mDate.setText(cursor.getString(COL_MATCHTIME));
        holder.mScore.setText(Utilities.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        holder.matchId = cursor.getDouble(COL_ID);
        holder.mHomeCrest.setImageResource(Utilities.getTeamCrestByTeamName(cursor.getString(COL_HOME)));
        holder.mAwayCrest.setImageResource(Utilities.getTeamCrestByTeamName(cursor.getString(COL_AWAY)
        ));
        //Log.v(FetchScoreTask.LOG_TAG,holder.mHomeName.getText() + " Vs. " + holder.mAwayName.getText() +" id " + String.valueOf(holder.matchId));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(mDetailMatchId));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        View v;
        try {
            v = vi.inflate(R.layout.detail_fragment, null);
        }
        catch (InflateException e) {
            // FIXED: don't crash on older devices
            // this might happen on older devices that don't support widgets properly
            // for example: Motorola Droid Razr API 16 - 4.1.1
            // the rest of the app will still work.  But the widget won't be available.
            Log.e(TAG, "UNABLE TO INFLATE! - e="+e);
            e.printStackTrace();
            return;
        }
        if (holder.matchId == mDetailMatchId) {
            //Log.v(TAG, "will insert extraView after container.removeAllViews();");
            // FIXED: on rotate, we need to remove any extraView before adding a new one
            container.removeAllViews();
            container.addView(v, 0, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            TextView matchDay = (TextView) v.findViewById(R.id.matchday_textview);
            matchDay.setText(Utilities.getMatchDay(cursor.getInt(COL_MATCHDAY), cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(cursor.getInt(COL_LEAGUE)));
            Button shareButton = (Button) v.findViewById(R.id.share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(holder.mHomeName.getText() + " "
                            + holder.mScore.getText() + " " + holder.mAwayName.getText() + " "));
                }
            });
        }
        else {
            //Log.v(TAG, "container.removeAllViews();");
            container.removeAllViews();
        }
    }

    private Intent createShareForecastIntent(String ShareText) {
        //Log.v(TAG, "createShareForecastIntent");
        final String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        else {
            // from: http://stackoverflow.com/questions/32941254/is-there-anything-similar-to-flag-activity-new-document-for-older-apis
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
