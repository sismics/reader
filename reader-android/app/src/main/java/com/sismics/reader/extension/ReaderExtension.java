package com.sismics.reader.extension;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.reader.R;
import com.sismics.reader.resource.SubscriptionResource;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Dashclock widget extension.
 *
 * @author bgamard.
 */
public class ReaderExtension extends DashClockExtension {

    private class Subscription {
        String title;
        int unreadCount;
    }

    @Override
    protected void onUpdateData(int reason) {
        setUpdateWhenScreenOn(false);

        // Fetch connection status
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // Check if a connection is available
        if (networkInfo.isConnected()) {
            // Fetch the number of unread articles
            SubscriptionResource.list(this, true, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject json) {
                    int unread = json.optInt("unread_count");
                    if (unread == 0) {
                        // Hide the widget is there is no unread articles
                        publishUpdate(null);
                        return;
                    }

                    // Extracting subscriptions with the most unread articles
                    List<Subscription> subscriptionList = new ArrayList<Subscription>();
                    JSONObject rootCategory = json.optJSONArray("categories").optJSONObject(0);
                    JSONArray categories = rootCategory.optJSONArray("categories");
                    if (categories != null) {
                        for (int i = 0; i < categories.length(); i++) {
                            JSONObject category = categories.optJSONObject(i);

                            JSONArray subscriptions = category.optJSONArray("subscriptions");
                            if (subscriptions != null) {
                                for (int j = 0; j < subscriptions.length(); j++) {
                                    JSONObject subscription = subscriptions.optJSONObject(j);

                                    Subscription item = new Subscription();
                                    item.title = subscription.optString("title");
                                    item.unreadCount = subscription.optInt("unread_count");
                                    subscriptionList.add(item);
                                }
                            }
                        }
                    }

                    // Extracting root subscriptions
                    JSONArray subscriptions = rootCategory.optJSONArray("subscriptions");
                    if (subscriptions != null) {
                        for (int j = 0; j < subscriptions.length(); j++) {
                            JSONObject subscription = subscriptions.optJSONObject(j);

                            Subscription item = new Subscription();
                            item.title = subscription.optString("title");
                            item.unreadCount = subscription.optInt("unread_count");
                            subscriptionList.add(item);
                        }
                    }

                    // Sort subscriptions by unread count
                    Collections.sort(subscriptionList, new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription lhs, Subscription rhs) {
                            return lhs.unreadCount < rhs.unreadCount ? 1 : (lhs.unreadCount == rhs.unreadCount ? 0 : -1);
                        }
                    });

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < subscriptionList.size(); i++) {
                        Subscription subscription = subscriptionList.get(i);
                        sb.append(subscription.title + " (" + subscription.unreadCount+ ")");
                        if (i != subscriptionList.size() - 1) {
                            sb.append(", ");
                        }
                    }

                    // Publish the extension data update
                    publishUpdate(new ExtensionData()
                            .visible(true)
                            .icon(R.drawable.ic_dashclock)
                            .status(unread + "")
                            .expandedTitle(String.format(getString(R.string.extension_expanded_title), unread))
                            .expandedBody(sb.toString())
                            .clickIntent(new Intent().setAction("com.sismics.reader.OPEN")));
                }

                @Override
                public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBytes, final Throwable throwable) {
                    // Network error, retry as soon as possible
                    setUpdateWhenScreenOn(true);
                }
            });
        } else {
            // No connection available, retry as soon as possible
            setUpdateWhenScreenOn(true);
        }
    }
}
