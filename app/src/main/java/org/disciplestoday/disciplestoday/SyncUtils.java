/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.disciplestoday.disciplestoday;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.disciplestoday.disciplestoday.accounts.GenericAccountService;
import org.disciplestoday.disciplestoday.provider.FeedContract;


/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    //private static final long SYNC_FREQUENCY = 60 * 60 * 7;  // 7 hours (in seconds)
    private static final long SYNC_FREQUENCY = 3;  // 7 hours (in seconds)
    //maybe use 3 hours since that still shouldn't affect battery life 'too much' if it's 4 times in 12 hours.
    //TOD: Make it so that it doesn't show notification the first install.

    private static final String CONTENT_AUTHORITY = FeedContract.CONTENT_AUTHORITY;
    static final String PREF_SETUP_COMPLETE = "setup_complete";
    public static final String PREF_LAST_PUB_DATE = "last_pub_date";
    //TODO: Refactor to prefs Manager

    //  private static final String TAG = SyncUtils.class.getSimpleName();
    private static final String TAG = "NJW";


    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public static void CreateSyncAccount(Context context) {
        Log.i("NJW9", "in Create Account");
        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = GenericAccountService.GetAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            Log.e("NJW9", "Add account explicitly");
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);


        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || ! setupComplete) {
            Log.d("NJW9", "trigger initial refresh");
            TriggerRefresh(SyncAdapter.MAIN_LIST_ID);
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
            //discovered minimum value of 1 hour is enforced, not sure how new that is
            // see https://developer.android.com/reference/android/content/ContentResolver.html#addPeriodSync
            // (pollFrequency)

        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     * @param moduleId - whateger nav drawer item is open, 353 if regular install (highlighted) or perhaps other if via share
     */
    public static void TriggerRefresh(String moduleId) {

        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putString(SyncAdapter.ARGS_MODULE_ID, moduleId);
        Log.e(TAG, "***Triggering Refresh for moduleId:" + moduleId);
        Account account = GenericAccountService.GetAccount();
        ContentResolver.setIsSyncable(account,FeedContract.CONTENT_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, FeedContract.CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(
                account,
                FeedContract.CONTENT_AUTHORITY,
                Bundle.EMPTY,
                SyncUtils.SYNC_FREQUENCY);
        ContentResolver.requestSync(
                account,      // Sync account
                FeedContract.CONTENT_AUTHORITY, // Content authority
                b);                                      // Extras
    }
}