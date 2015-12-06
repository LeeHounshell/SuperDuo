package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.service.WidgetService;


public class FootballScoresWidgetProvider extends AppWidgetProvider {
    private final static String TAG = "LEE: <" + FootballScoresWidgetProvider.class.getSimpleName() + ">";

    public static final String ACTION_WIDGET_ITEM_CLICK = "barqsoft.footballscores.ACTION_WIDGET_ITEM_CLICK";
    public static final String EXTRA_STRING = "barqsoft.footballscores.EXTRA_STRING";

    @Override
    @SuppressWarnings("deprecation")
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(TAG, "onUpdate");

        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_provider_layout);
            Intent widgetOpenAppOnClickIntent = new Intent(context, MainActivity.class);

            // update the widget list by using the WidgetService
            Intent serviceIntent = new Intent(context, WidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                remoteViews.setRemoteAdapter(R.id.widget_football_scores_collection_list, serviceIntent);
            }
            else {
                remoteViews.setRemoteAdapter(widgetId, R.id.widget_football_scores_collection_list, serviceIntent);
            }

            // add the collection list item handler
            final Intent onListItemClick = new Intent(context, FootballScoresWidgetProvider.class);
            onListItemClick.setAction(ACTION_WIDGET_ITEM_CLICK);
            onListItemClick.setData(Uri.parse(onListItemClick.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onListItemClickPendingIntent = PendingIntent
                .getBroadcast(context, 0, onListItemClick, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_football_scores_collection_list, onListItemClickPendingIntent);

            // also open the app when the widget header is clicked..
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, widgetOpenAppOnClickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_football_scores, pendingIntent);

            // update this widget
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent broadIntent) {
        Log.v(TAG, "onReceive");
        if (broadIntent.getAction().equals(ACTION_WIDGET_ITEM_CLICK)) {
            // open the app and goto the clicked item..
            String item = broadIntent.getExtras().getString(EXTRA_STRING);
            Log.v(TAG, "CLICK!: item=" + item);
            Intent intent = new Intent();
            intent.setClassName("barqsoft.footballscores", "barqsoft.footballscores.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("OPEN_SELECTED_GAME", item);
            context.startActivity(intent);
        }
        super.onReceive(context, broadIntent);
    }

}

