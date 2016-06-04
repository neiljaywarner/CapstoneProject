package org.disciplestoday.disciplestoday;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.disciplestoday.disciplestoday.data.FeedLoaderAsyncTask;

import java.util.List;

import static org.disciplestoday.disciplestoday.Article.TRACK_TYPE_ARTICLE;


public class ArticleListFragment extends Fragment implements FeedLoaderAsyncTask.OnTaskCompleted {


    private static final String TAG = "arg_nav_item_id";

    private static final String ARG_NAV_ID = "arg_nav_item_id";
    private List<Article> mArticles;
    private FeedLoaderAsyncTask asyncTask;
    private RecyclerView recyclerView;
    private ImageView imageViewFeatured;
    private TextView textViewFeaturedTitle;
    private WebView webviewLocator;
    private View mLayoutNews;

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
        Log.i("NJW", "Navigation Menu Item:" + menuItem.getTitle());
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
          //  mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            Log.i("NJW", "MNavItemID=" + mNavItemId);
            Log.i("NJW", "singlesid=" + R.id.nav_singles);
            mNavItemId = getArguments().getInt(ARG_NAV_ID);
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
            recyclerView.setNestedScrollingEnabled(false);


            imageViewFeatured = (ImageView) getActivity().findViewById(R.id.featured_image);
            textViewFeaturedTitle = (TextView) getActivity().findViewById(R.id.featured_article_title);
            mLayoutNews = getActivity().findViewById(R.id.layout_news);



            if (getArguments() == null) {
                showNews();
            } else {
                showNews(mNavItemId);
            }
        }


        return root;
    }

    @Override
    public void onTaskCompleted() {
        Log.e("NJW", "in OnTaskCompleted");
        mArticles = asyncTask.getItems();
        Article featuredArticle = mArticles.get(0);
        //NOTE: The first (0th) article as of May 30th has right and left padding when the others don't
        // either a) they should fix or b) a color from pallette can be the background...
        mArticles.remove(0);
        setupRecyclerView(recyclerView);

        setupFeaturedArticle(featuredArticle);
        progressDialog.dismiss();
        progressDialog = null;
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
     * Show the default news feed (highlighted/featured)
     */
    private void showNews() {
        showNews(null);
    }
    private void showNews(MenuItem menuItem) {

        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setTitle(R.string.fetching_articles);
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.fetching_articles_message));
        asyncTask = new FeedLoaderAsyncTask(this, menuItem);
        asyncTask.execute();
       // webviewLocator.setVisibility(View.GONE);
       // mLayoutNews.setVisibility(View.VISIBLE);
    }

    private void showNews(int menuItemId) {

        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setTitle(R.string.fetching_articles);
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.fetching_articles_message));
        asyncTask = new FeedLoaderAsyncTask(this, menuItemId);
        asyncTask.execute();
        // webviewLocator.setVisibility(View.GONE);
        // mLayoutNews.setVisibility(View.VISIBLE);
    }


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Article> mValues;

        public SimpleItemRecyclerViewAdapter(List<Article> items) {
            mValues = items;
        }

        @Override
        public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.article_list_content, parent, false);
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public final TextView mContentView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.imageViewThumbnail);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    //TODO: FIX FOR TABLET
    private void showArticle(Article article) {
        trackContentSelection(article);
        /*
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ArticleDetailFragment.ARG_ITEM_TITLE, item.getTitle());
            arguments.putString(ArticleDetailFragment.ARG_ITEM_FULLTEXT, item.getFullText());
            arguments.putString(ArticleDetailFragment.ARG_ITEM_IMAGE_URL, item.getDetailImageLink());


            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.article_detail_container, fragment)
                    .commit();
        } else {
        */
            Intent intent = new Intent(this.getActivity(), ArticleDetailActivity.class);
        //TODO: Just pass via parcelable due to speed eventually, for now use articleId due to deep links

            intent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, article.getId());
            intent.putExtra(ArticleDetailFragment.ARG_ITEM_TITLE, article.getTitle());
            intent.putExtra(ArticleDetailFragment.ARG_ITEM_FULLTEXT, article.getFullText());
            intent.putExtra(ArticleDetailFragment.ARG_ITEM_IMAGE_URL, article.getDetailImageLink());
            startActivity(intent);
        //}
    }


    private void setupFeaturedArticle(final Article article) {
        if (imageViewFeatured != null) {

            if (URLUtil.isValidUrl(article.getImageLink())) {

                Picasso.with(imageViewFeatured.getContext()).load(article.getImageLink())
                        .into(imageViewFeatured, new Callback() {
                            @Override public void onSuccess() {
                                if (ArticleListFragment.this.getActivity() != null) {
                                    Bitmap bitmap = ((BitmapDrawable) imageViewFeatured.getDrawable()).getBitmap();
                                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                        public void onGenerated(Palette palette) {
                                            updateTextView(textViewFeaturedTitle, palette);
                                            int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));

                                            imageViewFeatured.setBackgroundColor(lightVibrantColor);
                                        }
                                    });
                                }
                            }

                            @Override public void onError() {
                                Log.e(TAG, "Picasso:Error loading:" + article.getImageLink());
                            }
                        });
            }
        }

        textViewFeaturedTitle.setText(article.getTitle());
        textViewFeaturedTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showArticle(article);
            }
        });
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


}
