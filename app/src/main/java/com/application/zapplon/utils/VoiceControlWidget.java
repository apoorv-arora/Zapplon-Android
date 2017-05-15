package com.application.zapplon.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.application.zapplon.R;
import com.application.zapplon.views.CheckNetworkDialog;


/**
 * Created by Harsh on 5/26/2016.
 */
public class VoiceControlWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                VoiceControlWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            Intent intent = new Intent(context, CheckNetworkDialog.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to it
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.backgroundImage, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app
            // widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }
}
