package it.jaschke.alexandria.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import it.jaschke.alexandria.AlexandriaApplication;
import it.jaschke.alexandria.R;


public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    private final static String TAG = "LEE: <" + DownloadImage.class.getSimpleName() + ">";

    private final ImageView mBmImage;

    public DownloadImage(ImageView mBmImage) {
        this.mBmImage = mBmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        Log.v(TAG, "doInBackground");
        String imageUrl = urls[0];
        Bitmap coverImageBitmap = null;
        if (imageUrl != null && imageUrl.length() > 0) {
            try {
                Log.v(TAG, "download the cover image.. coverImageUrl=" + imageUrl);
                URL coverImageUrl = new URL(imageUrl);
                URLConnection connection = coverImageUrl.openConnection();
                // FIXED: use image caching
                // from: http://stackoverflow.com/questions/1945201/android-image-caching
                connection.setUseCaches(true);

                coverImageBitmap = BitmapFactory.decodeStream((InputStream)connection.getContent());
                if (coverImageBitmap != null) {
                    Log.v(TAG, "we have the cover image");
                }
            } catch (Exception e) {
                Log.e("InputStream Error", e.getMessage());
            }
        }
        else {
            // FIXED: use a place holder image if the URL does not exist
            Log.w(TAG, "UNABLE TO LOCATE URL FOR BOOK: USE PLACE HOLDER");
            Context context = AlexandriaApplication.getAppContext();
            coverImageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_placeholder);
        }
        return coverImageBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        Log.v(TAG, "onPostExecute: set the book image bitmap");
        mBmImage.setImageBitmap(result);
    }

}

