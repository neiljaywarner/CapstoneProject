package org.disciplestoday.disciplestoday.data;

import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.util.Log;

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
    private @IdRes int menuItemId;

    public interface OnTaskCompleted{
        void onTaskCompleted();
    }

    private OnTaskCompleted listener;

    private List<Article> mArticles;


    public FeedLoaderAsyncTask(OnTaskCompleted listener, @IdRes int menuItemId){
        super();
        Log.e("NJW", "in constructor");
        this.listener = listener;
        this.menuItemId = menuItemId;
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
        switch (menuItemId) {


            case R.id.nav_singles:
                Log.i(TAG, "SINGLES FEED");
                moduleId = "273";
                break;
            case R.id.nav_kingdom_kids:
                Log.i(TAG, "KINGDOM KIDS FEED");
                moduleId = "281";
                break;
            case R.id.nav_campus:
                Log.i(TAG, "CAMPUS FEED");
                moduleId = "285";
                break;
            case R.id.nav_youth_and_family:
                Log.i(TAG, "Y&F Feed");
                moduleId = "271";
                break;
            default:
                Log.i(TAG, "DEFAULT FEED");
                moduleId = "353";
        }

        return service.listFeed(moduleId);
    }

    protected void onPostExecute(Feed feed   ) {
        mArticles = Article.getArticles(feed);
        listener.onTaskCompleted();
    }

    public List<Article> getItems() {
        return mArticles;
    }
}
