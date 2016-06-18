package org.disciplestoday.disciplestoday;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.disciplestoday.disciplestoday.data.CupboardSQLiteOpenHelper;
import org.disciplestoday.disciplestoday.data.DisciplesTodayContentProvider;
import org.disciplestoday.disciplestoday.data.FeedLoaderAsyncTask;
import org.disciplestoday.disciplestoday.data.Item;
import org.disciplestoday.disciplestoday.provider.FeedContract;
import org.disciplestoday.disciplestoday.provider.FeedProvider;

import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.disciplestoday.disciplestoday.Article.TRACK_TYPE_ARTICLE;


public class ArticleListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

   // private static final String TAG = ArticleListFragment.class.getSimpleName();
    public static final String TAG = "NJW";
    private static final String ARG_NAV_ID = "arg_nav_item_id";
    private static final int LOADER_ID = 100;
    private List<Article> mArticles;
    private FeedLoaderAsyncTask asyncTask;
    private RecyclerView recyclerView;
    private WebView webviewLocator;

    private int mNavItemId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleListFragment() {
    }


    /**
     *
     * @param menuItem - NavigationDrawer Menu Item
     * @return Instance of fragment to display list of articles
     */
    public static ArticleListFragment newInstance(MenuItem menuItem) {
        Log.i("NJW", "newInstance ArticlelistFragment with Navigation Menu Item:" + menuItem.getTitle());
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NAV_ID, menuItem.getItemId());
        fragment.setArguments(args);
        return fragment;
    }


    public static ArticleListFragment newInstance() {
        Log.i("NJW", "New Instance of ArticleListFragment with no paramter - app open");
        ArticleListFragment fragment = new ArticleListFragment();

        return fragment;
    }
    private CupboardSQLiteOpenHelper mCupboardSQLiteOpenHelper;
    private SQLiteDatabase mDb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCupboardSQLiteOpenHelper = new CupboardSQLiteOpenHelper(this.getActivity().getApplicationContext());
        mDb = mCupboardSQLiteOpenHelper.getWritableDatabase();
        if (getArguments() != null) {
            mNavItemId = getArguments().getInt(ARG_NAV_ID);
            Log.i("NJW", "Setting mNavItemId" + mNavItemId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.article_list, container, false);

        // TODO: instance state
        Log.i("NJW", "in oncreateview, just inflated xml");
        // Set the adapter
        if (root instanceof RecyclerView) {
            recyclerView = (RecyclerView) root;
           // recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setNestedScrollingEnabled(true);


            /*
            if (getArguments() == null) {
                Log.e("NJW", "onCreateView args=null");

                showNews();
            } else {
                Log.e("NJW", "onCreateView args!=null");

                mNavItemId = getArguments().getInt(ARG_NAV_ID);
                if (getActivity() != null) {
                    showNews(mNavItemId);
                }

            }
            */

            getLoaderManager().initLoader(LOADER_ID, getArguments(), this);

        }


        return root;
    }

    /**
     * Create SyncAccount at launch, if needed.
     *
     * <p>This will create a new account with the system for our application, register our
     * {@link SyncService} with it, and establish a sync schedule.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach() called with: activity = [" + activity + "]");
        // Create account, if needed
        SyncUtils.CreateSyncAccount(activity);
    }



    /*
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String moduleId = "";
        *//*
        if (args == null) {
            //moduleId = "353"; //HIGHLIGHTED
            mNavItemId = R.id.nav_highlighted;
        } else {
            mNavItemId = getArguments().getInt(ARG_NAV_ID);
        }
        *//*

        Log.i("NJW", "in onCreateLoader");
        String[] projection = { Article.FIELD_ID, Article.FIELD_TITLE };
        projection = null;
        //TODO: remove projection if it is not neede.
        CursorLoader cursorLoader = new CursorLoader(this.getContext(),
                FeedProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }
*/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(getActivity(),  // Context
                FeedContract.Entry.CONTENT_URI, // URI
                null,                // Projection
                null,                           // Selection
                null,                           // Selection args
                null); // Sort string is optional
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mArticles = cupboard().withCursor(data).list(Article.class);
        if (mArticles == null) {
            Log.e("NJW", "marticles=null, no articles in db?");
        } else {
            Log.e("NJW", "just finished loading articles, count=" + mArticles.size());
        }
        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // anything to do here?
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_NAV_ID, mNavItemId);
        super.onSaveInstanceState(outState);
    }
        //using https://github.com/aegis123/Bettyskitchen-app/blob/master/BettysKitchen-app/src/main/java/com/bettys/kitchen/recipes/app/activities/MainActivity.java



    private void updateUI() {
        Log.i("NJW", "in updateUI");
        if (mArticles.isEmpty()) {
            return;
        }

        setupRecyclerView(recyclerView);

        if (progressDialog!= null && progressDialog.isShowing()) {
            Log.i("NJW", "****** Dismissing spinner.");
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (getActivity() != null) {
            getActivity().findViewById(R.id.content_main).setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(mArticles));
    }

    ProgressDialog progressDialog;

    //credit to https://github.com/antoniolg/MaterializeYourApp/blob/master/app/src/main/java/com/antonioleiva/materializeyourapp/DetailActivity.java
    private void updateTextView(TextView view, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int darkVibrantColor = palette.getDarkVibrantColor(getResources().getColor(android.R.color.black));

        view.setBackgroundColor(lightVibrantColor);
        view.setTextColor(darkVibrantColor);
    }


    /**
     * Show the default news feed (highlighted/featured) via syncadapter/content provider.
     */
/*
    private void showNews() {
        // TODO: Update so that db can save feed type and do more than just highlighted.
        Log.i("NJW", "in showNews()");

        if (mArticles.isEmpty()) {
            Log.e("NJW", "articles=empty");
            showNews(null);
        } else {
            updateUI();
        }
    }
    */
/*
    private void showNews(MenuItem menuItem) {
        Log.i("NJW", "in showNews:" + menuItem.getTitle());
        if (mArticles.isEmpty()) {
            Log.i("NJW", "Show Spinner;Loading via network");

            progressDialog = new ProgressDialog(this.getActivity());
            progressDialog.setTitle(R.string.fetching_articles);
            progressDialog.show();
            progressDialog.setMessage(getString(R.string.fetching_articles_message));
            asyncTask = new FeedLoaderAsyncTask(this, menuItem);
            asyncTask.execute();
        } else {
            Log.i("NJW", "No spinner, just show from db.");

            updateUI();
        }

    }
    */

/*
    private void showNews(int menuItemId) {
        Log.e("NJW", "aha, showNes with menuItemId");
            Log.e("NJW", "empty after checking db.");

            progressDialog = new ProgressDialog(this.getActivity());
            progressDialog.setTitle(R.string.fetching_articles);
            progressDialog.show();
            progressDialog.setMessage(getString(R.string.fetching_articles_message));
            asyncTask = new FeedLoaderAsyncTask(this, menuItemId);
            asyncTask.execute();
        } else {
            Log.e("NJW", "Found:" + mArticles.size() + " in db.");

            updateUI();
        }

    }
       */


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Article> mValues;

        public SimpleItemRecyclerViewAdapter(List<Article> items) {
            mValues = items;
        }

        @Override
        public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.featured_article, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.article_list_content, parent, false);
            }

            return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
            final Article item = mArticles.get(position);
            final String imageUrl = item.getImageLink();
            if (!imageUrl.isEmpty())
            {

                Picasso.with(holder.mImageView.getContext()).load(imageUrl)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .into(holder.mImageView, new Callback() {
                            @Override public void onSuccess() {
                                Bitmap bitmap = ((BitmapDrawable) holder.mImageView.getDrawable()).getBitmap();
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    public void onGenerated(Palette listViewPallette) {
                                        int lightVibrantColorList = listViewPallette.getLightVibrantColor(getResources().getColor(android.R.color.white));

                                        holder.mImageView.setBackgroundColor(lightVibrantColorList);
                                        if (holder.getAdapterPosition() == 0) {
                                            updateTextView(holder.mContentView, listViewPallette);
                                        }
                                    }
                                });
                            }

                            @Override public void onError() {
                                Log.e("NJW", "Picasso:Error loading:" + imageUrl);
                            }
                        });
            }


            holder.mContentView.setText(Html.fromHtml(item.getTitle()));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showArticle(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        @Override
        public int getItemViewType(int position) {
            int viewType = 1; //Default is 1
            if (position == 0) viewType = 0; //if zero, it will be a header view
            return viewType;
        }

        //http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public final TextView mContentView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.imageView);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    //TODO: FIX FOR TABLET MUCH LATER, just cleanup code
    private void showArticle(Article article) {
        trackContentSelection(article);

        Intent intent = new Intent(this.getActivity(), ArticleDetailActivity.class);
        //TODO: Just pass via parcelable due to speed eventually, for now use articleId due to deep links

        intent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, article.getId());
        intent.putExtra(ArticleDetailFragment.ARG_ITEM_TITLE, article.getTitle());
        intent.putExtra(ArticleDetailFragment.ARG_ITEM_LINK, article.getLink());

        intent.putExtra(ArticleDetailFragment.ARG_ITEM_FULLTEXT, article.getFullText());
        intent.putExtra(ArticleDetailFragment.ARG_ITEM_IMAGE_URL, article.getDetailImageLink());
        startActivity(intent);
    }

    private void trackContentSelection(Article article) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, article.getId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, article.getTitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, TRACK_TYPE_ARTICLE);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    //TODO: track view list and duration from selection to view.
    // https://developers.google.com/android/reference/com/google/firebase/analytics/FirebaseAnalytics.Event.html#constants
/*
    public void storeArticles(List<Article> articles) {
        Log.i("NJW", "***in storeArticles");
        for (Article article : articles) {
            storeArticle(mDb, article);
        }
    }
    public static long storeArticle(SQLiteDatabase database, Article article) {
        Log.e("NJW", "Storing to db article:" + article.getTitle());
        return cupboard().withDatabase(database).put(article);
    }
    */

}