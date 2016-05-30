package org.disciplestoday.disciplestoday;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.disciplestoday.disciplestoday.data.FeedLoaderAsyncTask;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FeedLoaderAsyncTask.OnTaskCompleted {


    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private List<Article> mArticles;
    private FeedLoaderAsyncTask asyncTask;
    private RecyclerView recyclerView;
    private ImageView imageViewFeatured;
    private TextView textViewFeaturedTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        asyncTask = new FeedLoaderAsyncTask(MainActivity.this);
        asyncTask.execute();

        recyclerView = (RecyclerView) findViewById(R.id.article_list);
        assert recyclerView != null;

        imageViewFeatured = (ImageView) findViewById(R.id.featured_image);
        textViewFeaturedTitle = (TextView) findViewById(R.id.featured_article_title);



        if (findViewById(R.id.article_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public void onTaskCompleted() {
        mArticles = asyncTask.getItems();
        Article featuredArticle = mArticles.get(2);
        mArticles.remove(0);
        setupRecyclerView(recyclerView);

        setupFeaturedArticle(featuredArticle);
    }

    private void setupFeaturedArticle(Article article) {
        if (imageViewFeatured != null) {

            /*
            Picasso.with(imageViewFeatured.getContext()).load(article.getImageLink())
                    .fit()
                    .into(imageViewFeatured);
                    */

            if (URLUtil.isValidUrl(article.getImageLink())) {

                Picasso.with(imageViewFeatured.getContext()).load(article.getImageLink())
                        .fit()
                        .into(imageViewFeatured, new Callback() {
                            @Override public void onSuccess() {
                                Bitmap bitmap = ((BitmapDrawable) imageViewFeatured.getDrawable()).getBitmap();
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    public void onGenerated(Palette palette) {
                                        updateTextView(textViewFeaturedTitle, palette);

                                    }
                                });
                            }

                            @Override public void onError() {

                            }
                        });
            }


        }

        textViewFeaturedTitle.setText(article.getTitle());


    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(mArticles));
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
            String name = item.getTitle();
            String imageUrl = item.getImageLink();
            if (!imageUrl.isEmpty())
            {

                Picasso.with(holder.mImageView.getContext()).load(imageUrl)
                        .fit()
                        .into(holder.mImageView);
            }


            holder.mContentView.setText(name);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ArticleDetailFragment.ARG_ITEM_TITLE, item.getTitle());
                        arguments.putString(ArticleDetailFragment.ARG_ITEM_FULLTEXT, item.getFullText());
                        arguments.putString(ArticleDetailFragment.ARG_ITEM_IMAGE_URL, item.getImageLink());


                        ArticleDetailFragment fragment = new ArticleDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.article_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ArticleDetailActivity.class);
                        intent.putExtra(ArticleDetailFragment.ARG_ITEM_TITLE, item.getTitle());
                        intent.putExtra(ArticleDetailFragment.ARG_ITEM_FULLTEXT, item.getFullText());
                        intent.putExtra(ArticleDetailFragment.ARG_ITEM_IMAGE_URL, item.getImageLink());
                        context.startActivity(intent);
                    }

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

    private void openInBrowser(String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }
    // http://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //credit to https://github.com/antoniolg/MaterializeYourApp/blob/master/app/src/main/java/com/antonioleiva/materializeyourapp/DetailActivity.java
    private void updateTextView(TextView view, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int darkVibrantColor = palette.getDarkVibrantColor(getResources().getColor(android.R.color.black));

        view.setBackgroundColor(lightVibrantColor);
        view.setTextColor(darkVibrantColor);
       // palette.getDarkVibrantSwatch().getBodyTextColor();
        //view.setBackgroundColor(p);
      //  view.setBackgroundColor(lightVibrantColor);
       // view.setVisibility(View.VISIBLE);
    }
}
