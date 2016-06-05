package org.disciplestoday.disciplestoday;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LOCATOR_URL = "http://www.dtodayinfo.net/Dtoday";
    private static final int HIGHLIGHTED_INDEX = 3; //the default and 3rd item in the nav drawer.
    private static final int REQUEST_INVITE = 1 ;
    private boolean mTwoPane;

    public FirebaseAnalytics mFirebaseAnalytics;
    private GoogleApiClient mGoogleApiClient;

    private static final String TRACK_MENU_SELECTION="feed";

    NavigationView mNavigationView;
    private WebView webviewLocator;
    private View mLayoutNews;
    private MenuItem mNavMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

            //Handle invitations if any. (Firebase invites)
        receiveInvitations();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        webviewLocator = (WebView) findViewById(R.id.webview_locator);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mLayoutNews = findViewById(R.id.content_main);
        mNavigationView.setNavigationItemSelectedListener(this);

        setupLocator();

        if (savedInstanceState == null) {
            navigateTo(mNavigationView.getMenu().getItem(2).getSubMenu().getItem(0));
        }

    }

    //TODO: Move to onResume to prevent backgroundingcarsh
    private void showFragment(MenuItem menuItem) {
        mLayoutNews.setVisibility(View.VISIBLE);
        webviewLocator.setVisibility(View.GONE);
        ArticleListFragment listFragment = ArticleListFragment.newInstance(menuItem);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.article_list_container, listFragment)
                .commit();
    }


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
    public boolean onNavigationItemSelected(MenuItem item) {
        navigateTo(item);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateTo(MenuItem item) {

        // Handle navigation view item clicks here.
        Log.e("NJW", "CLICKED:" + item.getTitleCondensed());
        if (item.getItemId() != R.id.nav_share) {
            mNavMenuItem = item;
            Log.e("NJW", "setting navmenuitem to :" + item.getTitleCondensed());

        }
        int id = item.getItemId();

        //TODO: Other links...
        switch (id) {

            case R.id.nav_locator:
                Log.i(TAG, "Navdrawer->Locator");
                gotoLocator();
                break;
            case R.id.nav_share:
                Log.i(TAG, "Clicked invite");
                onInviteClicked();
                //TODO: use as a parameter the currently displayed newsfeed if there is one.
                break;
            default:
                if (id == R.id.nav_hot_news) {
                    String link = "http://www.icochotnews.com";
                    openInBrowser(link);
                }
                Log.i(TAG, "Navdrawer->Show appropriate news feed.");
               // Track via titleCondensed b/c title will be localized.
                setPageTitle(item);
                trackFeedSelection(item.getTitleCondensed().toString());
                mLayoutNews.setVisibility(View.INVISIBLE);
                showFragment(item);
                //TODO: (Someday) Let this be loaded from local storage so the user doesn't see the ones s/he's not interested in.

                break;
        }
    }

    /**
     * From: http://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application
     * NOTE: Don't put these in a webview for now, plugins, videos, etc...
     * @param link
     */
    private void openInBrowser(String link)  {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private void setPageTitle(MenuItem item) {
        String title = item.getTitle().toString();
        if (item.getItemId() == R.id.nav_highlighted) {
            title = getString(R.string.app_name);
        }
        setTitle(title);

    }


    /**
     * This is loaded in a webview invisibly so it seems instant.
     */

    private void setupLocator() {
        webviewLocator.loadUrl(LOCATOR_URL);
        WebSettings webSettings = webviewLocator.getSettings();
        webviewLocator.setWebViewClient(new WebViewClient());

        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
      //  webSettings.setMinimumFontSize(33);
        webSettings.setSupportZoom(true);
       // webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);

        // TODO: See how many of these we should use on the other webviews!
        // e.g. plugin state...

        // see https://developer.android.com/reference/android/webkit/WebSettings.LayoutAlgorithm.html
        //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        //TODO: Enable for api 19 and above. 75% and above (kitkat and above) and climbing i'm sure.
        webviewLocator.setVisibility(View.GONE);
    }



    /**
     * Temporary locator via webview
     */

    private void gotoLocator() {

        webviewLocator.setVisibility(View.VISIBLE);
        mLayoutNews.setVisibility(View.GONE);

        if (webviewLocator.getUrl().equals(LOCATOR_URL)) {
            webviewLocator.loadUrl(LOCATOR_URL);
        }
    }

    public static long mFeedLoadStart = 0;
    private void trackFeedSelection(String feedName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, feedName);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle);
        mFeedLoadStart = System.nanoTime();
    }


    //TODO: Refactor into trackerhelper so we can use google analytics and/or flurry if we want and more easily do duration
    // at least get gogole analytics (For city)


    //TODO: Walk the nav tree in subroutine to build the path and unwalk the tree to use the path so it 'just works'
    private void onInviteClicked() { //12:04am

        Uri deepLink = new Uri.Builder().scheme("dt").path("/2/3").build();
        // TODO use getDeepLink()
        if (mNavMenuItem!= null && mNavMenuItem.getItemId() == R.id.nav_campus) {
            deepLink = new Uri.Builder().scheme("dt").path("campus/2/3").build();
        }

        if (mNavMenuItem!= null && mNavMenuItem.getItemId() == R.id.nav_man_up) {
            deepLink = new Uri.Builder().scheme("dt").path("manup/2/3").build();
        }


        //------
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_call_to_action_button_text))
                .setDeepLink(deepLink)
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
        //TODO: Consider using customImage and deep link for a given feed here.
        //TODO-v1.1: Use custom share sheet so "Email & SMS" comes up on top and to follow best practice.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("NJW", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                    //TODO: Log that they invited X number of people...
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
                Log.e("NJW", "Sending failed or was canceled");
                Toast.makeText(this, "Sending invite(s) failed, sorry about that.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void receiveInvitations() {
        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        // taken from https://firebase.google.com/docs/invites/android#receive-invitations
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLinkString = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);
                                    Log.e("NJW", "String invitationId=" + invitationId);
                                    Log.e("NJW", "Deep Link=" + deepLinkString);

                                    MenuItem newsFeedMenuItem = mNavigationView.getMenu().getItem(2).getSubMenu().getItem(3);
                                    if (deepLinkString.contains("manup")) {
                                        newsFeedMenuItem = mNavigationView.getMenu().getItem(2).getSubMenu().getItem(6);
                                        mNavigationView.setCheckedItem(R.id.nav_man_up);
                                    }
                                    if (deepLinkString.contains("campus")) {
                                        newsFeedMenuItem = mNavigationView.getMenu().getItem(2).getSubMenu().getItem(2);
                                        mNavigationView.setCheckedItem(R.id.nav_campus);
                                    }
                                    showFragment(newsFeedMenuItem);
                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    // ...
                                }
                            }
                        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
