/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.disciplestoday.disciplestoday;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.disciplestoday.disciplestoday.data.DTService;
import org.disciplestoday.disciplestoday.data.Feed;
import org.disciplestoday.disciplestoday.provider.FeedContract;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    //public static final String TAG = "SyncAdapter";
    public static final String TAG = "NJW";


    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[] {
            FeedContract.Entry._ID,
            FeedContract.Entry.COLUMN_NAME_ARTICLE_ID,
            FeedContract.Entry.COLUMN_NAME_TITLE,
            FeedContract.Entry.COLUMN_NAME_LINK};

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_LINK = 3;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");



        // TODO: Fix magic #s, but they correspond to values in MainActivity.getModuleId() and the query parameters of the feeds
        // Download all feeds
        syncDownloadFeed(syncResult, "353"); // should we?
        syncDownloadFeed(syncResult, "288");
        syncDownloadFeed(syncResult, "273");
        syncDownloadFeed(syncResult, "270");
        syncDownloadFeed(syncResult, "347");
        syncDownloadFeed(syncResult, "289");
        syncDownloadFeed(syncResult, "271");
        syncDownloadFeed(syncResult, "334");
        syncDownloadFeed(syncResult, "272");
        syncDownloadFeed(syncResult, "359");
        syncDownloadFeed(syncResult, "358");


        Log.i(TAG, "Network synchronization complete");
    }

    private void syncDownloadFeed(SyncResult syncResult, String moduleId) {
        Call<Feed> call = getCall(moduleId);
        try {
            Response<Feed> feedResponse = call.execute();
            Log.e("NJW", "Just executed call. Returned code:" + feedResponse.code());
            Feed feed = feedResponse.body();
            Log.d(TAG, "site=" + feed.getSite().getName());
            updateLocalFeedData(moduleId,feed, syncResult);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    public void updateLocalFeedData(String moduleId, Feed feed, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {


        List<Article> articles = Article.getArticles(moduleId, feed);

        Log.i(TAG, "***ModuoleId:"  + moduleId + " Parsing complete. " + articles.size() + " articles");

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming articles
        HashMap<String, Article> entryMap = new HashMap<String, Article>();
        for (Article e : articles) {
            entryMap.put(e.getId(), e);
        }




        // Add new items
        for (Article e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.getId());
            batch.add(ContentProviderOperation.newInsert(FeedContract.Entry.CONTENT_URI)
                    .withValue(FeedContract.Entry.COLUMN_NAME_ARTICLE_ID, e.getId())
                    .withValue(FeedContract.Entry.COLUMN_NAME_MODULE_ID, e.getModuleId())
                    .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, e.getTitle())
                    .withValue(FeedContract.Entry.COLUMN_NAME_IMAGE_LINK, e.getImageLink())
                    .withValue(FeedContract.Entry.COLUMN_NAME_FULL_TEXT, e.getFullText())
                    .withValue(FeedContract.Entry.COLUMN_NAME_AUTHOR, e.getAuthor())
                    .withValue(FeedContract.Entry.COLUMN_NAME_SUMMARY, e.getSummary())
                    .withValue(FeedContract.Entry.COLUMN_NAME_LINK, e.getLink())
                    .build());
            syncResult.stats.numInserts++;
        }

        mContentResolver.applyBatch(FeedContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                FeedContract.Entry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }


    /**
     * get call based on moduleid
     * @return
     */
    public Call<Feed> getCall(String moduleId) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DTService.DISCIPLES_TODAY_BASE_URL)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build();

        DTService service = retrofit.create(DTService.class);
        return service.listFeed(moduleId);

        /*
        @IdRes int itemId;
        if (menuItem == null) {
            itemId = R.id.nav_highlighted;
            Log.i(TAG, "getCall() - HIGHLIGHTED");
        } else {
            itemId = menuItem.getItemId();
            Log.i(TAG, "getCall() - MenuItem Title="+ menuItem.getTitle());
        }
        */
        /*
        @IdRes int itemId = menuItemId;
        switch (itemId) {
            case R.id.nav_campus:
                moduleId = "288";
                break;
            case R.id.nav_singles:
                moduleId = "273";
                break;
            case R.id.nav_bible_study:
                moduleId = "270";
                break;
            case R.id.nav_commentary:
                moduleId = "347";
                break;
            case R.id.nav_kingdom_kids:
                moduleId = "289";
                break;
            case R.id.nav_youth_and_family:
                moduleId = "271";
                break;
            case R.id.nav_missions:
                moduleId = "334";
                break;
            case R.id.nav_man_up:
                moduleId = "272";
                break;
            case R.id.nav_specialty_ministries:
                moduleId = "359";
                break;
            case R.id.nav_regional_news:
                moduleId = "358";
                break;
            default:
                moduleId = "353";
                */
       // }

    }
}
