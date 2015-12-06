package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {
    private static final String TAG = "LEE: <" + BookService.class.getSimpleName() + ">";

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    public BookService() {
        super("Alexandria");
        Log.v(TAG, "BookService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                // FIXED: catch any NPE from fetchBook
                try {
                    fetchBook(ean);
                }
                catch (NullPointerException e) {
                    Log.w(TAG, "got NullPointerException e="+e);
                }
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                // FIXED: catch any NPE from deleteBook
                try {
                    deleteBook(ean);
                }
                catch (NullPointerException e) {
                    Log.w(TAG, "got NullPointerException e="+e);
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        Log.v(TAG, "deleteBook");
        if (ean != null) {
            long book_id;
            // FIXED: invalid ISBN or ISBN with text
            try {
                book_id = Long.parseLong(ean);
                getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(book_id), null, null);
            } catch (NumberFormatException e) {
                Log.v(TAG, "invalid isbn:" + ean);
            }
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {
        Log.v(TAG, "fetchBook");

        if (ean.length() != 13) {
            return;
        }

        Cursor bookEntry;
        long book_id;
        // FIXED: invalid ISBN or ISBN with text
        try {
            book_id = Long.parseLong(ean);
            bookEntry = getContentResolver().query(
                    AlexandriaContract.BookEntry.buildBookUri(book_id),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );
        } catch (NumberFormatException e) {
            Log.e(TAG, "invalid isbn:" + ean);
            return;
        }

        //noinspection ConstantConditions
        if (bookEntry.getCount() > 0) {
            bookEntry.close();
            return;
        }

        bookEntry.close();

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final int half_hour = 30 * 60;
            final String QUERY_ISBN_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";
            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(QUERY_ISBN_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());
            connection = (HttpURLConnection) url.openConnection();

            // FIXED: request to follow redirects - this seems to solve networking issues on older devices < API 21
            connection.setInstanceFollowRedirects(true);
            // FIXED: cache the request data and use cache
            connection.setUseCaches(true);
            connection.setDefaultUseCaches(true);

            connection.setRequestMethod("GET");
            connection.addRequestProperty("Cache-Control", "max-age="+half_hour);

            boolean redirect = false;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            Log.v(TAG, "===> HTTP STATUS CODE: " + status + ", redirect=" + redirect);

            connection.connect();
            Log.v(TAG, "connected!");

            InputStream inputStream = connection.getInputStream();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            // FIXED: use StringBuilder instead of StringBuffer
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            // possible UnknownHostException on older Android device?
            Log.e(TAG, "uh-oh, HttpURLConnection Exception - e=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream - e=" + e.getMessage());
                }
            }
        }

        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        if (bookJsonString != null && bookJsonString.length() > 0) {
            try {
                JSONObject bookJson = new JSONObject(bookJsonString);
                JSONArray bookArray;
                if (bookJson.has(ITEMS)) {
                    bookArray = bookJson.getJSONArray(ITEMS);
                } else {
                    Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                    messageIntent.putExtra(MainActivity.MESSAGE_KEY, getResources().getString(R.string.not_found));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                    return;
                }

                JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

                String title = bookInfo.getString(TITLE);
                String subtitle = "";
                if (bookInfo.has(SUBTITLE)) {
                    subtitle = bookInfo.getString(SUBTITLE);
                }

                String desc = "";
                if (bookInfo.has(DESC)) {
                    desc = bookInfo.getString(DESC);
                }

                String imgUrl = "";
                if (bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                    imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
                }

                writeBackBook(ean, title, subtitle, desc, imgUrl);

                if (bookInfo.has(AUTHORS)) {
                    writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS));
                }
                if (bookInfo.has(CATEGORIES)) {
                    writeBackCategories(ean, bookInfo.getJSONArray(CATEGORIES));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error ", e);
            }
        } else {
            Log.w(TAG, "INVALID bookJsonString=" + bookJsonString);
        }
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        Log.v(TAG, "writeBackBook");
        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
    }

    private void writeBackAuthors(String ean, JSONArray jsonArray) throws JSONException {
        Log.v(TAG, "writeBackAuthors");
        ContentValues values = new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private void writeBackCategories(String ean, JSONArray jsonArray) throws JSONException {
        Log.v(TAG, "writeBackCategories");
        ContentValues values = new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

}