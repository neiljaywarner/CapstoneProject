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

import com.google.firebase.analytics.FirebaseAnalytics;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LOCATOR_URL = "http://www.dtodayinfo.net/Dtoday";
    private static final int HIGHLIGHTED_INDEX = 3; //the default and 3rd item in the nav drawer.
    private boolean mTwoPane;

    public FirebaseAnalytics mFirebaseAnalytics;
    private static final String TRACK_MENU_SELECTION="feed";

    NavigationView mNavigationView;
    private WebView webviewLocator;
    private View mLayoutNews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
        navigateTo(mNavigationView.getMenu().getItem(2).getSubMenu().getItem(0));

        setupLocator();

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
        Log.e("NJW", "LOADING:" + item.getTitleCondensed());
        int id = item.getItemId();

        //TODO: Other links... invites, etc.
        switch (id) {

            case R.id.nav_locator:
                Log.i(TAG, "Navdrawer->Locator");
                gotoLocator();
                break;
            default:
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
    // at least get gogole analytics
}
