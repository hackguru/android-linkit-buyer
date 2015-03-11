package ams.android.linkit.Fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ams.android.linkit.Activity.MainActivity;
import ams.android.linkit.Adapter.AdapterListview;
import ams.android.linkit.Adapter.AdapterListviewEmpty;
import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;
import ams.android.linkit.Tools.customListView;
import me.leolin.shortcutbadger.ShortcutBadger;


/**
 * Created by Aidin on 2/1/2015.
 */
public class FragmentLinks extends Fragment {
    private static String TAG = "linkitShopper";
    public static TextView txtMainTitle;
    ArrayList<LinkitObject> itemsLikes = new ArrayList<>();
    ArrayList<LinkitObject> itemsFeatured = new ArrayList<>();
    AdapterListview adapterListview;
    AdapterListviewEmpty adapterListviewEmpty;
    SwipeRefreshLayout swipeLayout;
    RelativeLayout layWaiting;
    customListView listView;
    TextView txtEmptyInfo;
    LinkitObject currentItem;
    Boolean callState = false;
    String globalEndDate = null;
    String globalStartDate = null;
    String listViewType;
    String userID, regID, merchantID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).currentFragmentName = "Link";
        try {
            currentItem = getArguments().getParcelable("item");
        } catch (Exception ex) {
        }
        View rootView = inflater.inflate(R.layout.fragment_links, container, false);
        userID = ((GlobalApplication) getActivity().getApplication()).getUserId();
        regID = ((GlobalApplication) getActivity().getApplication()).getRegistrationId();
        listView = (customListView) rootView.findViewById(R.id.listView);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        txtMainTitle = (TextView) rootView.findViewById(R.id.txtMainTitle);
        ImageButton btnMenuDrawer = (ImageButton) rootView.findViewById(R.id.btnMenuDrawer);
        ImageButton btnInsta = (ImageButton) rootView.findViewById(R.id.btn_instagram);
        layWaiting = (RelativeLayout) rootView.findViewById(R.id.lay_waiting);
        txtEmptyInfo = (TextView) rootView.findViewById(R.id.txtEmptyInfo);

        btnMenuDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.openDrawerMenu();
            }
        });

        btnInsta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent insta_intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    startActivity(insta_intent);
                } catch (Exception e) {
                    Log.e("linkitBuyer", "can't open Instagram");
                }
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (listViewType == "L") {
                    refreshLikesData();
                } else if (listViewType == "F") {
                    refreshFeaturedData();
                } else if (listViewType == "P") {
                    refreshMerchantPostedData();
                }
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        adapterListview = new AdapterListview(getActivity(), getFragmentManager(), itemsLikes);
        listView.setAdapter(adapterListview);
        listView.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
        listView.setOnDetectScrollListener(new customListView.OnDetectScrollListener() {
            @Override
            public void onUpScrolling() {

            }

            @Override
            public void onDownScrolling() {
                if (listView.getLastVisiblePosition() == itemsLikes.size() - 1) {
                    if (!callState) {
                        //Log.i("linkit", "end list");
                        layWaiting.setVisibility(View.VISIBLE);
                        callState = true;
                        if (listViewType == "L") {
                            addDataToEndLikes();
                        } else if (listViewType == "F") {
                            //refreshFeaturedData();
                        } else if (listViewType == "P") {
                            addDataToEndMerchantPosted();
                        }

                    }
                }
            }
        });
        swipeLayout.setRefreshing(true);
        refreshLikesData();

        // Get tracker.
        Tracker t = ((GlobalApplication) getActivity().getApplication()).getTracker(GlobalApplication.TrackerName.APP_TRACKER);
        t.setScreenName("LinkitShopper - List");
        t.send(new HitBuilders.AppViewBuilder().build());

        return rootView;
    }

    public void serverLogout() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("token", regID);
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        String URL = getResources().getString(R.string.BASE_URL) + "users/updateregid";
        client.post(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                ((GlobalApplication) getActivity().getApplication()).clearAllSettings();
                try {
                    ((GlobalApplication) getActivity().getApplicationContext()).setBadgetCount(0);
                    ShortcutBadger.setBadge(getActivity().getApplicationContext(), 0);
                } catch (Exception e) {
                }
                //showToast("Logout");
                itemsLikes.clear();
                itemsFeatured.clear();
                FragmentLogin f1 = new FragmentLogin();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, f1,"Login");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (statusCode == 401) {
                    Log.e("linkit", "ERR 401");
                    ((GlobalApplication) getActivity().getApplication()).clearAllSettings();
//                    showToast("Logout");
//                    FragmentLogin f1 = new FragmentLogin();
//                    FragmentTransaction ft = getFragmentManager().beginTransaction();
//                    ft.replace(R.id.container, f1); // f1_container is your FrameLayout container
//                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                    ft.addToBackStack("Login");
//                    ft.commit();
                } else {
                    Log.e("linkit", "ERR");
                }
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    public void addDataToEndLikes() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", regID);
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        if (globalStartDate != null) {
            requestParams.add("endDate", globalStartDate);
        }
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/likedmedias";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    parseJsonLikes(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                globalStartDate = itemsLikes.get(itemsLikes.size() - 1).createdDate;
                adapterListview.notifyDataSetChanged();
                layWaiting.setVisibility(View.INVISIBLE);
                callState = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                callState = false;
                swipeLayout.setRefreshing(false);
                layWaiting.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void addDataToEndMerchantPosted() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", regID);
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        if (globalStartDate != null) {
            requestParams.add("endDate", globalStartDate);
        }
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + merchantID + "/matchedmedia";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    parseJsonLikes(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                globalStartDate = itemsLikes.get(itemsLikes.size() - 1).createdDate;
                adapterListview.notifyDataSetChanged();
                layWaiting.setVisibility(View.INVISIBLE);
                callState = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                callState = false;
                swipeLayout.setRefreshing(false);
                layWaiting.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void refreshLikesData() {
        listViewType = "L";
        itemsLikes.clear();
        adapterListview.notifyDataSetChanged();
        listView.setVisibility(View.GONE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", regID);
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        if (globalStartDate != null) {
            requestParams.add("startDate", globalStartDate);
        }

        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/likedmedias";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                itemsLikes.clear();
                try {
                    parseJsonLikes(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itemsLikes.isEmpty()) {
                    txtEmptyInfo.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    txtEmptyInfo.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    globalStartDate = itemsLikes.get(itemsLikes.size() - 1).createdDate;
                    listView.setAdapter(adapterListview);
                    listView.setOnItemClickListener(null);
                    adapterListview.notifyDataSetChanged();
                    swipeLayout.setRefreshing(false);
                    if (currentItem != null) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        FragmentWebView f1 = new FragmentWebView();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("item", itemsLikes.get(0));
                        f1.setArguments(bundle);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.add(R.id.container, f1, "WebView");
                        ft.addToBackStack("WebView");
                        ft.commit();
                        currentItem = null;
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                swipeLayout.setRefreshing(false);
            }
        });
    }

    public void refreshMerchantPostedData() {
        listViewType = "P";
        itemsLikes.clear();
        adapterListview.notifyDataSetChanged();
        listView.setVisibility(View.GONE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", regID);
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        if (globalStartDate != null) {
            requestParams.add("startDate", globalStartDate);
        }

        String URL = getResources().getString(R.string.BASE_URL) + "users/" + merchantID + "/matchedmedia";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                itemsLikes.clear();
                try {
                    parseJsonLikes(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itemsLikes.isEmpty()) {
                    txtEmptyInfo.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    txtEmptyInfo.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    globalStartDate = itemsLikes.get(itemsLikes.size() - 1).createdDate;
                    listView.setAdapter(adapterListview);
                    listView.setOnItemClickListener(null);
                    adapterListview.notifyDataSetChanged();
                    swipeLayout.setRefreshing(false);
                    if (currentItem != null) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        FragmentWebView f1 = new FragmentWebView();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("item", itemsLikes.get(0));
                        f1.setArguments(bundle);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.add(R.id.container, f1, "WebView");
                        ft.addToBackStack("WebView");
                        ft.commit();
                        currentItem = null;
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                swipeLayout.setRefreshing(false);
            }
        });
    }

    public void refreshFeaturedData() {
        listViewType = "F";
        txtEmptyInfo.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        itemsFeatured.clear();
        adapterListview.notifyDataSetChanged();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", regID);
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/recommendedMerchants";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                itemsFeatured.clear();
                try {
                    parseJsonFeatured(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                swipeLayout.setRefreshing(false);
                adapterListviewEmpty = new AdapterListviewEmpty(getActivity(), getFragmentManager(), itemsFeatured);
                listView.setAdapter(adapterListviewEmpty);
                listView.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        merchantID = itemsFeatured.get(position).mediaID;
                        refreshMerchantPostedData();
//                        Uri uri = Uri.parse("http://instagram.com/_u/" + itemsFeatured.get(position).owner);
//                        Intent insta = new Intent(Intent.ACTION_VIEW, uri);
//                        insta.setPackage("com.instagram.android");
//                        if (isIntentAvailable(getActivity().getApplicationContext(), insta)) {
//                            startActivity(insta);
//                        } else {
//                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/" + itemsFeatured.get(position).owner)));
//                        }
                    }
                });
                adapterListviewEmpty.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void parseJsonLikes(String jsonStr) {
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray feeds = jsonObj.getJSONArray("results");
                int counterStart, counterEnd, counterSeed;
                counterStart = 0;
                counterEnd = feeds.length();
                counterSeed = 1;
                for (int i = counterStart; i < counterEnd; i = i + counterSeed) {
                    JSONObject item = feeds.getJSONObject(i);
                    JSONObject xitem;
                    LinkitObject myobject = new LinkitObject();

                    if (item.has("_id")) {
                        myobject.mediaID = item.getString("_id");
                    } else {
                        myobject.mediaID = "";
                    }

                    if (item.has("media")) {
                        xitem = item.getJSONObject("media");
                    } else {
                        xitem = item;
                    }
                    if (xitem.getJSONObject("owner").has("username")) {
                        myobject.owner = xitem.getJSONObject("owner").getString("username");
                    } else {
                        myobject.owner = "";
                    }
                    if (xitem.getJSONObject("owner").has("profilePicture")) {
                        myobject.ownerProfilePic = xitem.getJSONObject("owner").getString("profilePicture");
                    } else {
                        myobject.ownerProfilePic = "";
                    }
                    if (xitem.has("created")) {
                        myobject.createdDate = xitem.getString("created");
                    } else {
                        myobject.createdDate = "";
                    }
                    if (xitem.has("caption")) {
                        myobject.caption = xitem.getString("caption");
                    } else {
                        myobject.caption = "";
                    }
                    if (xitem.has("productDescription")) {
                        myobject.productDescription = xitem.getString("productDescription");
                    } else {
                        myobject.productDescription = "";
                    }
                    if (xitem.has("linkToProduct")) {
                        myobject.productLink = xitem.getString("linkToProduct");
                    } else {
                        myobject.productLink = "";
                    }
                    if (xitem.has("productLinkScreenshot")) {
                        myobject.linkSrceenShot = xitem.getString("productLinkScreenshot");
                    } else {
                        myobject.linkSrceenShot = "";
                    }
                    if (xitem.getJSONObject("images").getJSONObject("standard_resolution").has("url")) {
                        myobject.imageUrl = xitem.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                    } else {
                        myobject.imageUrl = "";
                    }

                    itemsLikes.add(myobject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //Log.e(TAG, "Couldn't get any data from the url");
        }
    }

    private void parseJsonFeatured(String jsonStr) {
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray feeds = jsonObj.getJSONArray("results");
                for (int i = 0; i < feeds.length(); i++) {
                    JSONObject item = feeds.getJSONObject(i);
                    LinkitObject myobject = new LinkitObject();

                    if (item.has("_id")) {
                        myobject.mediaID = item.getString("_id");
                    } else {
                        myobject.mediaID = "";
                    }
                    if (item.has("username")) {
                        myobject.owner = item.getString("username");
                    } else {
                        myobject.owner = "";
                    }
                    if (item.has("profilePicture")) {
                        myobject.ownerProfilePic = item.getString("profilePicture");
                    } else {
                        myobject.ownerProfilePic = "";
                    }
                    if (item.has("bio")) {
                        myobject.productDescription = item.getString("bio");
                    } else {
                        myobject.productDescription = "";
                    }
                    if (item.has("website")) {
                        myobject.ownerWebsite = item.getString("website");
                    } else {
                        myobject.ownerWebsite = "";
                    }
                    //

                    itemsFeatured.add(myobject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //Log.e(TAG, "Couldn't get any data from the url");
        }
    }

    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
