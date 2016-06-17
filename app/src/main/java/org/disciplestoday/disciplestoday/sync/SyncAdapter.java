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

package org.disciplestoday.disciplestoday.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.disciplestoday.disciplestoday.Article;
import org.disciplestoday.disciplestoday.R;
import org.disciplestoday.disciplestoday.data.DTContentProvider;
import org.disciplestoday.disciplestoday.data.DTService;
import org.disciplestoday.disciplestoday.data.Feed;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

// from https://developer.android.com/training/sync-adapters/creating-sync-adapter.html

public class SyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String TAG = SyncAdapter.class.getSimpleName();
    public static final String BASE_URL = "http://www.disciplestoday.org";


    // Global variables
    // Define a variable to contain a content resolver instance
   // ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
      //  mContentResolver = context.getContentResolver();
        Log.i("NJW", "inSyncAdadpter constructor");

    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
       // mContentResolver = context.getContentResolver();
        Log.i("NJW", "inSyncAdadpter constructor");
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {
    /*
     * Put the data transfer code here.
     */
        Log.d("NJW", "******in onPerformSync");
        Call<Feed> call = getCall();
        try {
            Log.d("NJW", "have call, about to execute.");
            Response<Feed> feedResponse = call.execute();
            Log.d("NJW", "hjust executed.");

            Feed feed = feedResponse.body();
            List<Article> articles = Article.getArticles(feed);
            Uri uri = DTContentProvider.CONTENT_URI;
            uri.buildUpon().appendPath(DTContentProvider.CONTENT_TYPE);
            if (articles != null) {
                Log.e("NJW", "have articecount:" + articles.size());
                Log.e("NJW", "about to do uri:" + uri.toString());
                cupboard().withContext(getContext()).put(uri, Article.class, articles);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();

        }


    }

    /**
     * get call based on mItemId
     * @return
     */
    public Call<Feed> getCall()
    {
        Log.d("NJW", "in getcall");
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gson);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build();

        DTService service = retrofit.create(DTService.class);
        String moduleId = "";
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
        int itemId = R.id.nav_highlighted;
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
        }

        //TODO: Eventually this can go somewhere better...

        //NOTE: This is actually just highlights of these feeds, we can get a lot more categories if we want to build the UI to support them... tabs?
        // the simple way to do it is to have a tab taht says subcategories or 'other' searched by subcategories or something.
        // Start simple,but downloading them is straightforward just by doing all numbers in the background...
        return service.listFeed(moduleId);
    }


}
