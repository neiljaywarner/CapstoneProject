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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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
    public static final String ARG_ITEM_PUB_DATE = "item_pub_date";


    public static final String ARG_ITEM_FULLTEXT = "item_fulltext";
    public static final String ARG_ITEM_IMAGE_URL = "item_image_url";

    public static final String TAG = ArticleDetailFragment.class.getSimpleName();
    private static final int REQUEST_INVITE = 100 ;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String mArticleId;
    private String mTitle;
    private String mLink;
    private String mFullText;
    private String mImageUrl;
    private String mPubDate;

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
        FirebaseCrash.logcat(Log.DEBUG, TAG, "Opening detail fragment");

        /*suppress unused*/
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this.getContext())
                .addApi(AppInvite.API)
                .enableAutoManage(this.getActivity(), this)
                .build();


        if (getArguments().containsKey(ARG_ITEM_TITLE)) {
            mArticleId = getArguments().getString(ARG_ITEM_ID);
            mTitle = getArguments().getString(ARG_ITEM_TITLE);
            mFullText = getArguments().getString(ARG_ITEM_FULLTEXT);
            mLink = getArguments().getString(ARG_ITEM_LINK);
            mPubDate = getArguments().getString(ARG_ITEM_PUB_DATE);
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Setting mPubDate to:" + mPubDate);
            trackContentView(mArticleId, mTitle);


            mImageUrl = getArguments().getString(ARG_ITEM_IMAGE_URL);
            Log.i(TAG, "mImageurl=" + mImageUrl);

            if (mFullText.contains(Html.escapeHtml(mImageUrl))) {
                mImageUrl = "invalid_url_don't_show_duplicate";
            }

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(Html.fromHtml(mTitle));
                appBarLayout.setExpandedTitleColor(ContextCompat.getColor(activity, android.R.color.transparent));

                final ImageView imageView = (ImageView) activity.findViewById(R.id.article_detail_image);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openInBrowser(mLink);
                    }
                });
                final FloatingActionButton fabShare = (FloatingActionButton) activity.findViewById(R.id.fabShareArticle);

                fabShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share();
                    }
                });

                fabShare.setVisibility(View.GONE);
                //NJW fixme, test.


                Picasso.with(imageView.getContext()).load(mImageUrl)
                        .placeholder(android.R.drawable.progress_horizontal)
                        .error(R.drawable.black_tax_feature_graphic_njw_1)
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

                fabShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        share();
                    }
                });
            }
        } else {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "No article title argument");
        }
    }

    private void openInBrowser(String mLink) {
        Intent browserIntent = new Intent();
        browserIntent.setAction(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(mLink));
        startActivity(browserIntent);
    }

    private void share() {
        shareArticle(mArticleId, mTitle, mLink, mFullText);
        trackContentShare(mArticleId, mTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.article_detail, container, false);

        if (mPubDate != null) {
            TextView textView = (TextView) rootView.findViewById(R.id.textViewPubDate);

            Long dateLong = Long.parseLong(mPubDate);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

            GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
            calendar.setTimeInMillis(dateLong);


            textView.setText("Posted on: " + sdf.format(calendar.getTime()));

        }
        WebView webview = (WebView) rootView.findViewById(R.id.article_detail);

        WebSettings webSettings = webview.getSettings();

        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);

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


    private void trackContentView(String id, String title) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, title);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, TRACK_TYPE_ARTICLE);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        bundle = new Bundle();
        bundle.putString("article_id", id);
        bundle.putString("article_title", title);
        mFirebaseAnalytics.logEvent("view_article", bundle);

    }

    //TODO: Should we consider sharing just introText?
    private void shareArticle(String articleid, String articleTitle, String articleLink, String fullText ) {

        Uri deepLink = getDeepLinkUri(articleid);


        //------
        String htmlEmailContent = getString(R.string.share_article_html_email_content);
        htmlEmailContent = htmlEmailContent.replace("%%ARTICLE_TITLE%%", articleTitle);
        htmlEmailContent = htmlEmailContent.replace("%%ARTICLE_TEXT%%", fullText);
        htmlEmailContent = htmlEmailContent.replace("%%ARTICLE_LINK%%", articleLink);

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
