package org.disciplestoday.disciplestoday;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static org.disciplestoday.disciplestoday.Article.TRACK_TYPE_ARTICLE;


/**
 * A fragment representing a single Article detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link ArticleDetailActivity}
 * on handsets.
 */
public class ArticleDetailFragment extends Fragment implements  GoogleApiClient.OnConnectionFailedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    public static final String ARG_ITEM_TITLE = "item_title";
    public static final String ARG_ITEM_LINK = "item_link";

    public static final String ARG_ITEM_FULLTEXT = "item_fulltext";
    public static final String ARG_ITEM_IMAGE_URL = "item_image_url";

    public static final String TAG = ArticleDetailFragment.class.getSimpleName();
    private static final int REQUEST_INVITE = 100 ;

    public FirebaseAnalytics mFirebaseAnalytics;
    private GoogleApiClient mGoogleApiClient;



    //TOOD: Use parcelable and/or remove these items (remember to handle saveInstanceState)
    private String mArticleId;
    private String mTitle;
    private String mLink;
    private String mFullText;
    private String mImageUrl;
    // TODO: Use package for network with the classes to parse gson, and package model for data model to be passed as parcelable
    // and/or used with content provider.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.getActivity());

        mGoogleApiClient = new GoogleApiClient.Builder(this.getContext())
                .addApi(AppInvite.API)
                .enableAutoManage(this.getActivity(), this)
                .build();

        if (getArguments().containsKey(ARG_ITEM_TITLE)) {
            mArticleId = getArguments().getString(ARG_ITEM_ID);
            mTitle = getArguments().getString(ARG_ITEM_TITLE);
            mFullText = getArguments().getString(ARG_ITEM_FULLTEXT);
            mLink = getArguments().getString(ARG_ITEM_LINK);

            mImageUrl = getArguments().getString(ARG_ITEM_IMAGE_URL);
            Log.i(TAG, "mImageurl=" + mImageUrl);


            if (mFullText.contains(Html.escapeHtml(mImageUrl))) {
                mImageUrl = "invalid_url_don't_show_duplicate";
            }
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

                fabShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share();
                    }
                });


                fabShare.setVisibility(View.GONE);
                if (!URLUtil.isValidUrl(mImageUrl)) {
                    mImageUrl = Article.DEFAULT_IMAGE_URL;
                }

                    Picasso.with(imageView.getContext()).load(mImageUrl)
                            .placeholder(android.R.drawable.progress_horizontal)
                            .into(imageView, new Callback() {
                                @Override public void onSuccess() {
                                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                        public void onGenerated(Palette palette) {
                                            updateBackground(fabShare, palette);
                                            int lightVibrantColorList = palette.getLightVibrantColor(getResources().getColor(android.R.color.white));

                                            imageView.setBackgroundColor(lightVibrantColorList);
                                        }
                                    });
                                }

                                @Override public void onError() {

                                }
                            });



                //TODO: Try adding introtext as textview/metabar so it does't look like a wall of text, confirm from client

                fabShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        share();
                    }
                });
            }
        }
    }

    private void share() {
        shareArticle(mArticleId, mTitle, mLink, mFullText);
        trackContentShare(mArticleId, mTitle);
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
        mFullText = Html.fromHtml(mFullText).toString();
        webview.loadData(mFullText, "text/html; charset=utf-8", "utf-8");


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
    //TODO: Use advertising ID so we get interests and more geographic data.
    //TODO: We could consider tracking how long it takes to load things as      params.putDouble(Param.VALUE, 10ms);

    //TODO: Use this one after i have content provider and use share.
    private void trackContentShare(Article article) {
        trackContentShare(article.getId(), article.getTitle());
    }

    private void trackContentShare(String id, String title) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, title);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, TRACK_TYPE_ARTICLE);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);

        bundle = new Bundle();
        bundle.putString("article_id", id);
        bundle.putString("article_title", title);
        mFirebaseAnalytics.logEvent("share_article", bundle);
        // Using custom share_event b/c default share event doesn't seem to let you put title
        // but built-in share event may be useful for other tools/reports.
    }


    //TODO: Should we consider sharing just introText?
    private void shareArticle(String articleid, String articleTitle, String articleLink, String fullText ) {

        Uri deepLink = getDeepLinkUri(articleid);


        //------
        String htmlEmailContent = getString(R.string.share_article_html_email_content);
        htmlEmailContent = htmlEmailContent.replace("%%ARTICLE_TITLE%%", articleTitle);
        htmlEmailContent = htmlEmailContent.replace("%%ARTICLE_TEXT%%", fullText);
        htmlEmailContent = htmlEmailContent.replace("%%ARTICLE_LINK%%", articleLink);
            //TODO: Could ask client if this should be introText

        //TODO: DELETE ALL THE TODOS and put in github issues :)


            //Note: fromHtml handles stuff like &quot to quotation marks, which was in the data.
        String invitationScreenTitle = "Share '" + Html.fromHtml(articleTitle) + "'";
        //TODO: use strings.xml
        String message = "Check out:'" + Html.fromHtml(articleTitle) + "'";
        //NOTE: Message can be only 100 characters... TODO: maybe goo.gl links somehow?
        // but if it's iPhone + Android that will be enough...
        Intent intent = new AppInviteInvitation.IntentBuilder(invitationScreenTitle)
                .setMessage(message)
                .setEmailSubject(getString(R.string.app_name))
                .setEmailHtmlContent(htmlEmailContent)
                .setDeepLink(deepLink)
                .build();
        startActivityForResult(intent, REQUEST_INVITE);

        //TODO-v1.1: Use custom share sheet so "Email & SMS" comes up on top and to follow best practice.
    }

    private Uri getDeepLinkUri(String articleId) {
        Uri deepLink = new Uri.Builder().scheme("dt")
                .path("/article/" + articleId)
                .build();
        return deepLink;
    }

    //TODO: track share_success with onActivityResult... might be a little tricky because in fragment?

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
