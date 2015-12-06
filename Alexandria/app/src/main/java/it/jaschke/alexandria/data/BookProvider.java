package it.jaschke.alexandria.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;


public class BookProvider extends ContentProvider {
    private final static String TAG = "LEE: <" + BookProvider.class.getSimpleName() + ">";

    private static final int BOOK_ID = 100;
    private static final int BOOK = 101;

    private static final int AUTHOR_ID = 200;
    private static final int AUTHOR = 201;

    private static final int CATEGORY_ID = 300;
    private static final int CATEGORY = 301;

    private static final int BOOK_FULL = 500;
    private static final int BOOK_FULLDETAIL = 501;

    // FIXED: no network initializer on main thread
    private static UriMatcher sUriMatcher;
    private static SQLiteQueryBuilder mBookFull;

    private DbHelper mDbHelper;

    public BookProvider() {
        super();
        Log.v(TAG, "BookProvider");
    }

    private static UriMatcher buildUriMatcher() {
        Log.v(TAG, "buildUriMatcher");

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AlexandriaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AlexandriaContract.PATH_BOOKS + "/#", BOOK_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_AUTHORS + "/#", AUTHOR_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_CATEGORIES + "/#", CATEGORY_ID);

        matcher.addURI(authority, AlexandriaContract.PATH_BOOKS, BOOK);
        matcher.addURI(authority, AlexandriaContract.PATH_AUTHORS, AUTHOR);
        matcher.addURI(authority, AlexandriaContract.PATH_CATEGORIES, CATEGORY);

        matcher.addURI(authority, AlexandriaContract.PATH_FULLBOOK + "/#", BOOK_FULLDETAIL);
        matcher.addURI(authority, AlexandriaContract.PATH_FULLBOOK, BOOK_FULL);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        Log.v(TAG, "onCreate");
        // FIXED: do initialization off the main thread
        Thread setupDB = new Thread(new Runnable() {
            @Override
            public void run() {
                sUriMatcher = buildUriMatcher();
                mDbHelper = new DbHelper(getContext());
                mBookFull = new SQLiteQueryBuilder();
                mBookFull.setTables(
                        AlexandriaContract.BookEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                                AlexandriaContract.AuthorEntry.TABLE_NAME + " USING (" + AlexandriaContract.BookEntry._ID + ")" +
                                " LEFT OUTER JOIN " + AlexandriaContract.CategoryEntry.TABLE_NAME + " USING (" + AlexandriaContract.BookEntry._ID + ")");

            }
        });
        setupDB.start();
        return true;
    }

    // FIXED: Not annotated parameter overrides parameter annotated with @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "query: uri="+uri+", selection="+selection+", sortOrder="+sortOrder);
        // first ensure DB access is initialized
        if (mDbHelper == null || mBookFull == null) {
            Log.e(TAG, "query failed - null found: mDbHelper="+ mDbHelper +" OR mBookFull="+ mBookFull);
            return null;
        }
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BOOK:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selection == null ? null : selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlexandriaContract.AuthorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlexandriaContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_ID:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        projection,
                        AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR_ID:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlexandriaContract.AuthorEntry.TABLE_NAME,
                        projection,
                        AlexandriaContract.AuthorEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY_ID:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlexandriaContract.CategoryEntry.TABLE_NAME,
                        projection,
                        AlexandriaContract.CategoryEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_FULLDETAIL:
                String[] bfdProjection = {
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.TITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.SUBTITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.IMAGE_URL,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.DESC,
                        "group_concat(DISTINCT " + AlexandriaContract.AuthorEntry.TABLE_NAME + "." + AlexandriaContract.AuthorEntry.AUTHOR + ") as " + AlexandriaContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + AlexandriaContract.CategoryEntry.TABLE_NAME + "." + AlexandriaContract.CategoryEntry.CATEGORY + ") as " + AlexandriaContract.CategoryEntry.CATEGORY
                };
                retCursor = mBookFull.query(mDbHelper.getReadableDatabase(),
                        bfdProjection,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            case BOOK_FULL:
                String[] bfProjection = {
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.TITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.IMAGE_URL,
                        "group_concat(DISTINCT " + AlexandriaContract.AuthorEntry.TABLE_NAME + "." + AlexandriaContract.AuthorEntry.AUTHOR + ") as " + AlexandriaContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + AlexandriaContract.CategoryEntry.TABLE_NAME + "." + AlexandriaContract.CategoryEntry.CATEGORY + ") as " + AlexandriaContract.CategoryEntry.CATEGORY
                };
                retCursor = mBookFull.query(mDbHelper.getReadableDatabase(),
                        bfProjection,
                        null,
                        selectionArgs,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        try {
            Log.v(TAG, "QUERY FOUND "+retCursor.getCount()+" MATCHES");
            //noinspection ConstantConditions
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        catch (NullPointerException e) {
            Log.e(TAG, "got NullPointerException e="+e);
        }

        return retCursor;
    }

    // FIXED: Not annotated parameter overrides parameter annotated with @NonNull
    @Override
    public String getType(@NonNull Uri uri) {
        Log.v(TAG, "getType");
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOK_FULLDETAIL:
                return AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE;
            case BOOK_ID:
                return AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE;
            case AUTHOR_ID:
                return AlexandriaContract.AuthorEntry.CONTENT_ITEM_TYPE;
            case CATEGORY_ID:
                return AlexandriaContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case BOOK:
                return AlexandriaContract.BookEntry.CONTENT_TYPE;
            case AUTHOR:
                return AlexandriaContract.AuthorEntry.CONTENT_TYPE;
            case CATEGORY:
                return AlexandriaContract.CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    // FIXED: Not annotated parameter overrides parameter annotated with @NonNull
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.v(TAG, "insert");
        // first ensure DB access is initialized
        if (mDbHelper == null || mBookFull == null) {
            Log.e(TAG, "insert failed - null found: mDbHelper="+ mDbHelper +" OR mBookFull="+ mBookFull);
            return null;
        }
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case BOOK: {
                long _id = db.insert(AlexandriaContract.BookEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = AlexandriaContract.BookEntry.buildBookUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                try {
                    //noinspection ConstantConditions
                    getContext().getContentResolver().notifyChange(AlexandriaContract.BookEntry.buildFullBookUri(_id), null);
                }
                catch (NullPointerException e) {
                    Log.e(TAG, "got NullPointerException e="+e);
                }
                break;
            }
            case AUTHOR: {
                long _id = db.insert(AlexandriaContract.AuthorEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AlexandriaContract.AuthorEntry.buildAuthorUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(AlexandriaContract.CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AlexandriaContract.CategoryEntry.buildCategoryUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    // FIXED: Not annotated parameter overrides parameter annotated with @NonNull
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete");
        // first ensure DB access is initialized
        if (mDbHelper == null || mBookFull == null) {
            Log.e(TAG, "delete failed - null found: mDbHelper="+ mDbHelper +" OR mBookFull="+ mBookFull);
            return 0;
        }
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case BOOK:
                rowsDeleted = db.delete(AlexandriaContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsDeleted = db.delete(AlexandriaContract.AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(AlexandriaContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                rowsDeleted = db.delete(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            try {
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(uri, null);
            }
            catch (NullPointerException e) {
                Log.e(TAG, "got NullPointerException e="+e);
            }
        }
        return rowsDeleted;
    }

    // FIXED: Not annotated parameter overrides parameter annotated with @NonNull
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(TAG, "update");
        // first ensure DB access is initialized
        if (mDbHelper == null || mBookFull == null) {
            Log.e(TAG, "update failed - null found: mDbHelper="+ mDbHelper +" OR mBookFull="+ mBookFull);
            return 0;
        }
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case BOOK:
                rowsUpdated = db.update(AlexandriaContract.BookEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsUpdated = db.update(AlexandriaContract.AuthorEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(AlexandriaContract.CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            try {
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(uri, null);
            }
            catch (NullPointerException e) {
                Log.e(TAG, "got NullPointerException e="+e);
            }
        }
        return rowsUpdated;
    }

}