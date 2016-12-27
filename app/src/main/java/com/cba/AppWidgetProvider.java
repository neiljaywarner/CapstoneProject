package com.cba;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import com.cba.provider.FeedContract;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by neil on 6/23/16.
 */

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                    Article article = cupboard().withContext(context).query(FeedContract.Entry.CONTENT_URI, Article.class).get();
            views.setTextViewText(R.id.widget_article_title, article.getTitle());
            views.setOnClickPendingIntent(R.id.viewGroupArticleListContent, pendingIntent);
            Picasso.with(context)
                    .load(article.getImageLink())
                    .into(views, R.id.imageView, new int[] {appWidgetId});

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
