package barqsoft.footballscores.service;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.WidgetServiceDataProvider;

//@SuppressLint("NewApi")
public class WidgetService extends RemoteViewsService {
    private final static String TAG = "LEE: <" + WidgetService.class.getSimpleName() + ">";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.v(TAG, "onGetViewFactory");
        return new WidgetServiceDataProvider(getApplicationContext(), intent);
    }

}
