package org.disciplestoday.disciplestoday;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LOCATOR_URL = "http://www.dtodayinfo.net/Dtoday";
    private static final int HIGHLIGHTED_SUBITEM_INDEX = 0;
    private static final int REQUEST_INVITE = 1 ;
    public static final int SUBMENU_LINKS_INDEX = 3;
    public static final int NEWS_MENU_INDEX = 2;
    private boolean mTwoPane;

    public FirebaseAnalytics mFirebaseAnalytics;
    private GoogleApiClient mGoogleApiClient;

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
            navigateTo(mNavigationView.getMenu().getItem(NEWS_MENU_INDEX).getSubMenu().getItem(HIGHLIGHTED_SUBITEM_INDEX));
        }

    }

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
                if (isExternalLink(item)) {
                    openInBrowser(getExternalLink(item));
                } else {
                    Log.i(TAG, "Navdrawer->Show appropriate news feed.");
                    // Track via titleCondensed b/c title will be localized.
                    setPageTitle(item);
                    trackFeedSelection(item.getTitleCondensed().toString());
                    mLayoutNews.setVisibility(View.INVISIBLE);
                    showFragment(item);
                }

                //TODO: (Someday) Let this list be loaded from local storage so the user doesn't see the ones s/he's not interested in.

                break;
        }
    }

    /**
     * Determine if item is an external link
     * TODO: Make this less brittle/more robust, to not depend on magic string in condensed title
     * @param item
     * @return
     */
    private boolean isExternalLink(MenuItem item) {
        return item.getTitleCondensed().toString().startsWith("Link");
    }
    /**
     * From: http://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application
     * NOTE: Don't put these in a webview for now, plugins, videos, etc...
     * @param link
     */
    private void openInBrowser(String link)  {
        Log.e("NJW", "Opening link=" + link);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private String getExternalLink(MenuItem menuItem) {
        // Walk Menu tree to get order in links
        Menu linksSubMenu = mNavigationView.getMenu().getItem(SUBMENU_LINKS_INDEX).getSubMenu();
        String[] links = getResources().getStringArray(R.array.links);
        MenuItem linkMenuItem;
        for (int i = 0; i < linksSubMenu.size(); i++) {
            linkMenuItem = linksSubMenu.getItem(i);
            if (linkMenuItem.getItemId() == menuItem.getItemId()) {
                return links[i];
            }
        }
        return "";
        //TODO: Make this less brittle via JSON asset files or something.
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
    private void onInviteClicked() {

        Uri deepLink = getDeepLinkUri(mNavMenuItem);


        //------
        String htmlEmailContent = getString(R.string.invitation_html_email_content);
        htmlEmailContent = htmlEmailContent.replace("%%FEED%%", mNavMenuItem.getTitle());
       // htmlEmailContent = Html.escapeHtml(htmlEmailContent);
        String invitationScreenTitle = "Share " + mNavMenuItem.getTitle() + " articles.";
        //TODO: Internationalize with string parameter

        Intent intent = new AppInviteInvitation.IntentBuilder(invitationScreenTitle)
                .setMessage(getString(R.string.invitation_message))
                .setEmailSubject(getString(R.string.app_name) + "-" + mNavMenuItem.getTitle().toString())
                .setEmailHtmlContent(htmlEmailContent)
                .setDeepLink(deepLink)
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
        //TODO-v1.1: Use custom share sheet so "Email & SMS" comes up on top and to follow best practice.
    }

    /**
     * Turns news menu item into deep link.
     * @param menuItem - Currently selected news feed menu item you want to share
     * @return
     */
    private Uri getDeepLinkUri(MenuItem menuItem) {
        Uri deepLink;
        // Walk Menu tree to get order in links
        Menu subMenu = mNavigationView.getMenu().getItem(NEWS_MENU_INDEX).getSubMenu();
        MenuItem subMenuItem;
        int subMenuIndex = 0;
        String subMenuItemCondensedTitle = "highlighted";
        for (int i = 0; i < subMenu.size(); i++) {
            subMenuItem = subMenu.getItem(i);
            if (subMenuItem.getItemId() == menuItem.getItemId()) {
                subMenuIndex = i;
                subMenuItemCondensedTitle = subMenu.getItem(i).getTitleCondensed().toString();
            }
        }
        deepLink = new Uri.Builder().scheme("dt")
                .path("/" + subMenuItemCondensedTitle + "/" + NEWS_MENU_INDEX +"/" + subMenuIndex)
                .build();
        return deepLink;
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
                Toast.makeText(this, "Sending invite(s) canceled or failed, sorry about that.", Toast.LENGTH_LONG).show();
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
                                    Log.i("NJW", "Deep Link=" + deepLinkString);
                                    int deepLinkSubMenuIndex = Integer.parseInt(deepLinkString.split("/")[3]);
                                    Log.e("NJW", "subMenuIndex=" + deepLinkSubMenuIndex);
                                    MenuItem newsFeedMenuItem = mNavigationView.getMenu()
                                            .getItem(NEWS_MENU_INDEX).getSubMenu()
                                            .getItem(deepLinkSubMenuIndex);

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
