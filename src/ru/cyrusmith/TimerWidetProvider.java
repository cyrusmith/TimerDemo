package ru.cyrusmith;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TimerWidetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int numOfWidgets = appWidgetIds.length;
		for(int i=0; i < numOfWidgets; i++){
			int appWidgetId = appWidgetIds[i];
			Intent intent = new Intent(context,TimerService.class).putExtra("duration", 666);
			PendingIntent pIntent = PendingIntent.getService(context, 0, intent, 0);
			RemoteViews wView = new RemoteViews(context.getPackageName(),R.layout.timer_appwidget);
			
			wView.setOnClickPendingIntent(R.id.ticks, pIntent);
			appWidgetManager.updateAppWidget(appWidgetId, wView);
		}
	}
	
	public static void updateWidgets(Context ctx, Integer ticks){
		RemoteViews wdgViews = new RemoteViews(ctx.getPackageName(),R.layout.timer_appwidget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
		wdgViews.setTextViewText(R.id.ticks, "Ticks: " + String.valueOf(ticks));
		appWidgetManager.updateAppWidget(new ComponentName(ctx,TimerWidetProvider.class), wdgViews);
	}

}