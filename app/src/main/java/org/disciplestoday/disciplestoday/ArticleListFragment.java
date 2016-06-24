package org.disciplestoday.disciplestoday;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.disciplestoday.disciplestoday.provider.FeedContract;

import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.disciplestoday.disciplestoday.Article.TRACK_TYPE_ARTICLE;

public class ArticleListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListFragment.class.getSimpleName();
    private static final String ARG_NAV_ID = "arg_nav_item_id";
    private static final int LOADER_ID = 100;
    private List<Article> mArticles;
    private RecyclerView recyclerView;

    private String mModuleId; //"353" highlighted, "288" campus

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleListFragment() {
    }


    /**
     *
     * @param moduleId - ModuleId for newsFeed for query string param
     * @return Instance of fragment to display list of articles
     */
    public static ArticleListFragment newInstance(String moduleId) {
        Log.i(TAG, "newInstance ArticlelistFragment with Navigation Menu Item:" + moduleId);
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAV_ID, moduleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mModuleId = getArguments().getString(ARG_NAV_ID);
            Log.i(TAG, "Setting Module Id " + mModuleId);
        } else {
            mModuleId = "353"; // Highlighted feed
        }

        SyncUtils.CreateSyncAccount(this.getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.article_list, container, false);

        if (root instanceof RecyclerView) {
            recyclerView = (RecyclerView) root;
            recyclerView.setNestedScrollingEnabled(true);

            getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
        }

        return root;
    }
    //using https://github.com/aegis123/Bettyskitchen-app/blob/master/BettysKitchen-app/src/main/java/com/bettys/kitchen/recipes/app/activities/MainActivity.java
    @Override
    public Loader<Cursor> onCreateLoader(int loader_id, Bundle bundle) {
        Log.d(TAG, "Creating loader for:" + mModuleId);
        String[] selectionArgs = new String[] {mModuleId};
        final String selection = "moduleId = ?";
        return new CursorLoader(getActivity(),      // Context
                FeedContract.Entry.CONTENT_URI,     // URI
                null,                               // Projection
                selection,                          // Selection
                selectionArgs,                      // Selection args
                null); // Sort string is optional
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Loader finished for:" + mModuleId);

        mArticles = cupboard().withCursor(data).list(Article.class);
        Log.i(TAG, "just finished loading articles, count=" + mArticles.size());
        if (mArticles.size() == 0) {
            Log.e(TAG, "NO Articles in the database - trigger first sync for this moduleId");
            progressDialog = new ProgressDialog(this.getContext());
            progressDialog.setTitle(R.string.fetching_articles);
            progressDialog.setMessage(getString(R.string.fetching_articles_message));
            progressDialog.show();
            SyncUtils.TriggerRefresh(mModuleId);
        }

        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // anything to do here?
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_NAV_ID, mModuleId);
        super.onSaveInstanceState(outState);
    }
        //using https://github.com/aegis123/Bettyskitchen-app/blob/master/BettysKitchen-app/src/main/java/com/bettys/kitchen/recipes/app/activities/MainActivity.java

    private void updateUI() {
        Log.i(TAG, "in updateUI");
        if (mArticles.isEmpty()) {
            return;
        }

        setupRecyclerView(recyclerView);

        if (progressDialog!= null && progressDialog.isShowing()) {
            Log.i(TAG, "****** Dismissing spinner.");
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (getActivity() != null) {
            getActivity().findViewById(R.id.content_main).setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(mArticles));
        recyclerView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("NJW", "event=" + event.toString());
                if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                    // Add your code here
                    Log.i("NJW", "dpad center");
                }

                if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    // Add your code here
                    Log.i("NJW", "dpad down or up");
                }

                if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    // Add your code here
                    Log.i("NJW", "dpad right or left");
                }

                return true;
            }
        });

        recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean currFocus) {
                Log.i("ONFOCUSCHANGE- reclist", "focus has changed I repeat the focus has changed! current focus = " + currFocus);

                    recyclerView.getChildAt(0).requestFocus();

            }
        });
    }

    ProgressDialog progressDialog;

    //credit to https://github.com/antoniolg/MaterializeYourApp/blob/master/app/src/main/java/com/antonioleiva/materializeyourapp/DetailActivity.java
    private void updateTextView(TextView view, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int darkVibrantColor = palette.getDarkVibrantColor(getResources().getColor(android.R.color.black));

        view.setBackgroundColor(lightVibrantColor);
        view.setTextColor(darkVibrantColor);
    }

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

                Picasso.with(holder.mImageView.getContext())
                        .load(Uri.parse(imageUrl))
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(R.mipmap.ic_launcher)
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
                                Log.e(TAG, "Picasso:Error loading:" + Uri.parse(imageUrl) + "; using launcher as 'other image'");
                            }
                        });

            }

            holder.mContentView.setText(item.getTitle());
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

    private void showArticle(Article article) {
        trackContentSelection(article);
        //simple and speedy...
        Intent intent = new Intent(this.getActivity(), ArticleDetailActivity.class);

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
    // https://developers.google.com/android/reference/com/google/firebase/analytics/FirebaseAnalytics.Event.html#constants

}