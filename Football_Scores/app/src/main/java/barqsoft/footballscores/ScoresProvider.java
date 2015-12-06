package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;


public class ScoresProvider extends ContentProvider {
    //private final static String TAG = "LEE: <" + ScoresProvider.class.getSimpleName() + ">";

    private static ScoresDBHelper sOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private final UriMatcher mUriMatcher = buildUriMatcher();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.ScoresTable.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE = DatabaseContract.ScoresTable.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID = DatabaseContract.ScoresTable.MATCH_ID + " = ?";

    private static UriMatcher buildUriMatcher() {
        //Log.v(TAG, "buildUriMatcher");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.BASE_CONTENT_URI.toString();
        matcher.addURI(authority, null, MATCHES);
        matcher.addURI(authority, "league", MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, "id", MATCHES_WITH_ID);
        matcher.addURI(authority, "date", MATCHES_WITH_DATE);
        return matcher;
    }

    private int match_uri(Uri uri) {
        //Log.v(TAG, "match_uri");
        String link = uri.toString();
        {
            if (link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString())) {
                return MATCHES;
            } else if (link.contentEquals(DatabaseContract.ScoresTable.buildScoreWithDate().toString())) {
                return MATCHES_WITH_DATE;
            } else if (link.contentEquals(DatabaseContract.ScoresTable.buildScoreWithId().toString())) {
                return MATCHES_WITH_ID;
            } else if (link.contentEquals(DatabaseContract.ScoresTable.buildScoreWithLeague().toString())) {
                return MATCHES_WITH_LEAGUE;
            }
        }
        return -1;
    }

    @Override
    public boolean onCreate() {
        //Log.v(TAG, "onCreate");
        sOpenHelper = new ScoresDBHelper(getContext());
        return false;
    }

    // FIXED: Not annotated parameter overrides @NonNull parameter
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //Log.v(TAG, "update");
        return 0;
    }

    // FIXED: Not annotated parameter overrides @NonNull parameter
    @Override
    public String getType(@NonNull Uri uri) {
        //Log.v(TAG, "getType");
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    // FIXED: Not annotated parameter overrides @NonNull parameter
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Log.v(TAG, "query");
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        int match = match_uri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (match) {
            case MATCHES:
                retCursor = sOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[1]);
                //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[2]);
                retCursor = sOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_DATE, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_ID:
                retCursor = sOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_LEAGUE:
                retCursor = sOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        try {
            //noinspection ConstantConditions
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return retCursor;
    }

    // FIXED: Not annotated parameter overrides @NonNull parameter
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        //Log.v(TAG, "insert");
        return null;
    }

    // FIXED: Not annotated parameter overrides @NonNull parameter
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        //Log.v(TAG, "bulkInsert");
        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(mUriMatcher.match(uri)));
        switch (match_uri(uri)) {
            case MATCHES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                try {
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(uri, null);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // FIXED: Not annotated parameter overrides @NonNull parameter
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        //Log.v(TAG, "delete");
        return 0;
    }

}
