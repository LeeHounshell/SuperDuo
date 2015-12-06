package it.jaschke.alexandria;

import android.app.Application;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.util.Log;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ALL")
public class AlexandriaApplication extends Application {
    private final static String TAG = "LEE: <" + AlexandriaApplication.class.getSimpleName() + ">";

    private static Context sContext;

    public AlexandriaApplication() {
        Log.v(TAG, "AlexandriaApplication");
    }

    public void onCreate() {
        Log.v(TAG, "onCreate");
        super.onCreate();
        AlexandriaApplication.sContext = getApplicationContext();
        enableHttpCaching();
    }

    public static Context getAppContext() {
        return AlexandriaApplication.sContext;
    }

    // from: http://practicaldroid.blogspot.com/2013/01/utilizing-http-response-cache.html
    private void enableHttpCaching()
    {
        Log.v(TAG, "enableHttpCaching");
        try {
            File httpCacheDir = new File(getApplicationContext().getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i(TAG, "HTTP response cache failed:" + e);
        }
    }

}
