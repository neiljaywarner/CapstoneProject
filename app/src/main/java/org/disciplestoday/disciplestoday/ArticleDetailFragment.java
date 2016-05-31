package org.disciplestoday.disciplestoday;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * A fragment representing a single Article detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link ArticleDetailActivity}
 * on handsets.
 */
public class ArticleDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_TITLE = "item_title";
    public static final String ARG_ITEM_FULLTEXT = "item_fulltext";
    public static final String ARG_ITEM_IMAGE_URL = "item_image_url";



    //TOOD: Use parcelable and/or remove these items
    private String mTitle;
    private String mLink;
    private String mImageUrl;
    // TODO: Use package for network with the classes to parse gson, and package model for data model to be passed as parcelable
    // and/or used with content provider.

    /**


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_TITLE)) {
            mTitle = getArguments().getString(ARG_ITEM_TITLE);
            mLink = getArguments().getString(ARG_ITEM_FULLTEXT);
            mImageUrl = getArguments().getString(ARG_ITEM_IMAGE_URL);

            Log.e("NJW", "mimageurl=" + mImageUrl);
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
           // mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            //TODO: GO back to example from template when doing content provider... or pass as parcelable...or both
            // See http://stackoverflow.com/questions/30323424/android-pass-a-parcelable-array-or-fetch-data-again-from-content-provider
            // content provider for the course though..
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mTitle);
                appBarLayout.setExpandedTitleColor(ContextCompat.getColor(activity, android.R.color.transparent));


                final ImageView imageView = (ImageView) activity.findViewById(R.id.article_detail_image);
                final FloatingActionButton fabShare = (FloatingActionButton) activity.findViewById(R.id.fabShareArticle);
                fabShare.setVisibility(View.GONE);

                if (URLUtil.isValidUrl(mImageUrl)) {

                    Picasso.with(imageView.getContext()).load(mImageUrl)
                            .fit()
                            .into(imageView, new Callback() {
                                @Override public void onSuccess() {
                                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                        public void onGenerated(Palette palette) {
                                            updateBackground(fabShare, palette);
                                        }
                                    });
                                }

                                @Override public void onError() {

                                }
                            });
                }


                //TODO: Try adding introtext as textview/metabar so it does't look like a wall of text, confirm from client
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.article_detail, container, false);

        // TODO: Use id from content provider to get item
        /*
        if (mItem != null) {
           //Load via parcelable for performance?
           //load list via contentprovider and cursorloaders.
        }
        */

        WebView webview = (WebView) rootView.findViewById(R.id.article_detail);
        mLink = mLink.replace("images/", "http://disciplestoday.org/images/");
        mLink = Html.fromHtml(mLink).toString();
        webview.loadData(mLink, "text/html; charset=utf-8", "utf-8");


        return rootView;
    }

    //credit to https://github.com/antoniolg/MaterializeYourApp/blob/master/app/src/main/java/com/antonioleiva/materializeyourapp/DetailActivity.java
    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.colorAccent));

        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
        fab.setVisibility(View.VISIBLE);
    }
}
//             Credit to http://antonioleiva.com/collapsing-toolbar-layout/ and other materialize your app goodness he blogs about.
