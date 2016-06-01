package org.disciplestoday.disciplestoday.data;

import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.disciplestoday.disciplestoday.Article;
import org.disciplestoday.disciplestoday.R;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by neil on 5/25/16.
 */

public class FeedLoaderAsyncTask extends AsyncTask<Void,Void, Feed> {

    public static final String BASE_URL = "http://www.disciplestoday.org";
    private static final String TAG = FeedLoaderAsyncTask.class.getSimpleName() ;
    private MenuItem menuItem;


    public interface OnTaskCompleted{
        void onTaskCompleted();
    }

    private OnTaskCompleted listener;

    private List<Article> mArticles;

    public FeedLoaderAsyncTask(OnTaskCompleted listener, MenuItem menuItem){
        super();
        Log.e("NJW", "in constructor");
        this.listener = listener;
        this.menuItem = menuItem;
    }

    @Override
    protected Feed doInBackground(Void... params) {

        Call<Feed> call = getCall();
        try {
            Response<Feed> feedResponse = call.execute();
            Feed feed = feedResponse.body();
            return feed;
        } catch (IOException e) {
            Log.e("NJW", e.getMessage());
            e.printStackTrace();
            return null;

        }
    }

    /**
     * get call based on mItemId
     * @return
     */
    public Call<Feed> getCall() {
        Log.i(TAG, "in doonbackgrond");
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
        @IdRes int itemId;
        if (menuItem == null) {
            itemId = R.id.nav_highlighted;
            Log.i(TAG, "getCall() - HIGHLIGHTED");
        } else {
            itemId = menuItem.getItemId();
            Log.i(TAG, "getCall() - MenuItem Title="+ menuItem.getTitle());
        }
        switch (itemId) {
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
                moduleId = "281";
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

    protected void onPostExecute(Feed feed ) {
        mArticles = Article.getArticles(feed);
        listener.onTaskCompleted();
    }

    public List<Article> getItems() {
        return mArticles;
    }
}
