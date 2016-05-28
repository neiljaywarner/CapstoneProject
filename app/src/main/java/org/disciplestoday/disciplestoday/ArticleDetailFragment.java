package org.disciplestoday.disciplestoday;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


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
            Log.e("NJW", "mLink=" + mLink);
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
            ((TextView) rootView.findViewById(R.id.article_detail)).setText(mItem.details);
        }
        */

        WebView webview = (WebView) rootView.findViewById(R.id.article_detail);
        mLink = mLink.replace("images/", "http://disciplestoday.org/images/");

        webview.loadData(mLink, "text/html", null);

        return rootView;
    }
}
