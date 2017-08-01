package org.disciplestoday.disciplestoday;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

//public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
public class MainActivity extends AppCompatActivity  {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_INVITE = 1 ;


    public FirebaseAnalytics mFirebaseAnalytics;
    //private GoogleApiClient mGoogleApiClient;


    //TOODO: BUILD BACKSTACK!!! so that 'back' from about doesn't exit the app..

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the FirebaseAnalytics instance.

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

            //Handle invitations if any. (Firebase invites)
        receiveInvitations();
        */


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            showNewsFeedFragment("2"); //1
        }

    }

    //TODO in onSave instancestate


    private void showNewsFeedFragment(String page) {
        ArticleListFragment listFragment = ArticleListFragment.newInstance(page);

        Log.i(TAG, "Showing list fragment:" );
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.article_list_container, listFragment)
                .commit();
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                    //TODO: Track to analytics that they invited X number of people?
                }
            } else {
                Log.e(TAG, "Sending failed or was canceled");
                Toast.makeText(this, getResources().getString(R.string.invite_error_or_cancel), Toast.LENGTH_LONG).show();
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
                                    Intent intent = result.getInvitationIntent();
                                    String deepLinkString = AppInviteReferral.getDeepLink(intent);
                                    Log.i(TAG, "Deep Link=" + deepLinkString);
                                    if (deepLinkString.contains("article")) {
                                        // TODO: Open in new ArticleDetailActivity
                                        return;
                                    }

                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    // ...
                                }
                            }
                        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    */

}
