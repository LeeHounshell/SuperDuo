package barqsoft.footballscores;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


class ViewHolder {
    //private final static String TAG = "LEE: <" + ViewHolder.class.getSimpleName() + ">";

    public final TextView mHomeName;
    public final TextView mAwayName;
    public final TextView mScore;
    public final TextView mDate;
    public final ImageView mHomeCrest;
    public final ImageView mAwayCrest;
    public double matchId;

    public ViewHolder(View view) {
        //Log.v(TAG, "ViewHolder");
        mHomeName = (TextView) view.findViewById(R.id.home_name);
        mAwayName = (TextView) view.findViewById(R.id.away_name);
        mScore = (TextView) view.findViewById(R.id.score_textview);
        mDate = (TextView) view.findViewById(R.id.gametime_textview);
        mHomeCrest = (ImageView) view.findViewById(R.id.home_crest);
        mAwayCrest = (ImageView) view.findViewById(R.id.away_crest);
    }

}
