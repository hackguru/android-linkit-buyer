package ams.android.linkit.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
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

import ams.android.linkit.Adapter.AdapterListview;
import ams.android.linkit.Adapter.AdapterListviewEmpty;
import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;
import ams.android.linkit.Tools.myListView;
import me.leolin.shortcutbadger.ShortcutBadger;


/**
 * Created by Aidin on 2/1/2015.
 */
public class FragmentLinks extends Fragment {

    ArrayList<LinkitObject> items = new ArrayList<LinkitObject>();
    ArrayList<LinkitObject> itemsEmpty = new ArrayList<LinkitObject>();
    AdapterListview adapterListview;
    AdapterListviewEmpty adapterListviewEmpty;
    myListView listView;
    String userID, regID;
    LayoutInflater ginflater;
    View grootView;
    SwipeRefreshLayout swipeLayout;
    LinkitObject currentItem;
    RelativeLayout layWaiting;
    Boolean callState = false;
    TextView txtEmptyInfo;
    String globalEndDate = null;
    String globalStartDate = null;

    public static final FragmentLinks newInstance(LinkitObject item) {
        FragmentLinks f = new FragmentLinks();
        f.currentItem = item;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!(getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        View rootView = inflater.inflate(R.layout.fragment_links, container, false);
        userID = ((GlobalApplication) getActivity().getApplication()).getUserId();
        regID = ((GlobalApplication) getActivity().getApplication()).getRegistrationId();
        ginflater = inflater;
        grootView = rootView;
        listView = (myListView) rootView.findViewById(R.id.listView);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        ImageButton btnLogout = (ImageButton) rootView.findViewById(R.id.btn_logout);
        layWaiting = (RelativeLayout) rootView.findViewById(R.id.lay_waiting);
        txtEmptyInfo = (TextView) rootView.findViewById(R.id.txtEmptyInfo);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setIcon(R.drawable.ic_launcher)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                serverLogout();
                            }
                        });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //addDataToStart();
                refreshData();
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        adapterListview = new AdapterListview(getActivity(), getFragmentManager(), items);

        listView.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
        listView.setOnDetectScrollListener(new myListView.OnDetectScrollListener() {
            @Override
            public void onUpScrolling() {

            }

            @Override
            public void onDownScrolling() {
                if (listView.getLastVisiblePosition() == items.size() - 1) {
                    if (!callState) {
                        Log.i("linkit", "end list");
                        layWaiting.setVisibility(View.VISIBLE);
                        callState = true;
                        addDataToEnd();
                    }
                }
            }
        });

        swipeLayout.setRefreshing(true);
        refreshData();

        // Get tracker.
        Tracker t = ((GlobalApplication) getActivity().getApplication()).getTracker(GlobalApplication.TrackerName.APP_TRACKER);
        t.setScreenName("LinkitShopper - List");
        t.send(new HitBuilders.AppViewBuilder().build());

        return rootView;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        swipeLayout.setRefreshing(true);
//        refreshData();
    }

    public void serverLogout() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        String URL = getResources().getString(R.string.BASE_URL) + "users/updateregid";
        client.post(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                ((GlobalApplication) getActivity().getApplication()).clearAllSettings();
                try {
                    ((GlobalApplication) getActivity().getApplicationContext()).setBadgetCount(0);
                    ShortcutBadger.setBadge(getActivity().getApplicationContext(), 0);
                } catch (Exception e) {
                }
                //showToast("Logout");
                items.clear();
                itemsEmpty.clear();
                FragmentLogin f1 = new FragmentLogin();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, f1); // f1_container is your FrameLayout container
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                //ft.addToBackStack("Login");
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

    public void addDataToEnd() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        if (globalStartDate != null) {
            requestParams.add("endDate", globalStartDate);
        }
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/likedmedias";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    parseJSON(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                globalStartDate = items.get(items.size() - 1).createdDate;
                adapterListview.notifyDataSetChanged();
                layWaiting.setVisibility(View.INVISIBLE);
                callState = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                callState = false;
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    public void refreshData() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        if (globalStartDate != null) {
            requestParams.add("startDate", globalStartDate);
        }
//        if (globalEndDate != null) {
//            requestParams.add("endDate", globalEndDate);
//        }
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/likedmedias";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                items.clear();
                try {
                    parseJSON(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (items.isEmpty()) {
                    txtEmptyInfo.setVisibility(View.VISIBLE);
                    refreshDataEmpty();

                } else {
                    txtEmptyInfo.setVisibility(View.GONE);
                    //globalEndDate = items.get(0).createdDate;
                    globalStartDate = items.get(items.size() - 1).createdDate;
                    listView.setAdapter(adapterListview);
                    adapterListview.notifyDataSetChanged();
                    swipeLayout.setRefreshing(false);
                    if (currentItem != null) {
                        FragmentWebView f1 = FragmentWebView.newInstance(items.get(0));
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                        ft.add(R.id.container, f1, "WebView");
                        ft.addToBackStack("WebView");
                        ft.commit();
                        currentItem = null;
                    }
                }
                //showToast("Data Updated");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                swipeLayout.setRefreshing(false);
            }
        });
    }

    public void refreshDataEmpty() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/recommendedMerchants";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                itemsEmpty.clear();
                try {
                    parseJSONEmpty(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                swipeLayout.setRefreshing(false);
                adapterListviewEmpty = new AdapterListviewEmpty(getActivity(), getFragmentManager(), itemsEmpty);
                listView.setAdapter(adapterListviewEmpty);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Uri uri = Uri.parse("http://instagram.com/_u/" + itemsEmpty.get(position).owner);
                        Intent insta = new Intent(Intent.ACTION_VIEW, uri);
                        insta.setPackage("com.instagram.android");
                        if (isIntentAvailable(getActivity().getApplicationContext(), insta)) {
                            startActivity(insta);
                        } else {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/" + itemsEmpty.get(position).owner)));
                        }
                    }
                });
                adapterListviewEmpty.notifyDataSetChanged();
            }

            private boolean isIntentAvailable(Context ctx, Intent intent) {
                final PackageManager packageManager = ctx.getPackageManager();
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                return list.size() > 0;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                //Log.e("linkit", "ERR : " + errorResponse.toString());
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    private void parseJSON(String jsonStr) {
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
                    LinkitObject myobject = new LinkitObject();

                    if (item.has("_id")) {
                        myobject.mediaID = item.getString("_id");
                    } else {
                        myobject.mediaID = "";
                    }
                    if (item.getJSONObject("media").getJSONObject("owner").has("username")) {
                        myobject.owner = item.getJSONObject("media").getJSONObject("owner").getString("username");
                    } else {
                        myobject.owner = "";
                    }
                    if (item.getJSONObject("media").getJSONObject("owner").has("profilePicture")) {
                        myobject.ownerProfilePic = item.getJSONObject("media").getJSONObject("owner").getString("profilePicture");
                    } else {
                        myobject.ownerProfilePic = "";
                    }
                    if (item.getJSONObject("media").has("created")) {
                        myobject.createdDate = item.getJSONObject("media").getString("created");
                    } else {
                        myobject.createdDate = "";
                    }
                    if (item.getJSONObject("media").has("productDescription")) {
                        myobject.productDescription = item.getJSONObject("media").getString("productDescription");
                    } else {
                        myobject.productDescription = "";
                    }
                    if (item.getJSONObject("media").has("linkToProduct")) {
                        myobject.productLink = item.getJSONObject("media").getString("linkToProduct");
                    } else {
                        myobject.productLink = "";
                    }
                    if (item.getJSONObject("media").has("productLinkScreenshot")) {
                        myobject.linkSrceenShot = item.getJSONObject("media").getString("productLinkScreenshot");
                    } else {
                        myobject.linkSrceenShot = "";
                    }
                    try {
                        myobject.imageUrl = item.getJSONObject("media").getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                    } catch (Exception ex) {
                        myobject.imageUrl = "";
                    }
                    if (!myobject.imageUrl.isEmpty()) {
                        items.add(myobject);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //Log.e(TAG, "Couldn't get any data from the url");
        }
    }

    private void parseJSONEmpty(String jsonStr) {
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

                    itemsEmpty.add(myobject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //Log.e(TAG, "Couldn't get any data from the url");
        }
    }

//    private void showToast(String text) {
//        View layout = ginflater.inflate(R.layout.toast, (ViewGroup) grootView.findViewById(R.id.toast_layout_root));
//        final CardView card = (CardView) layout.findViewById(R.id.card_view_toast);
//        card.setCardBackgroundColor(Color.parseColor("#2191c1"));
//        TextView textView = (TextView) layout.findViewById(R.id.text);
//        textView.setText(text);
//        Toast toast = new Toast(getActivity().getApplicationContext());
//        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
//        toast.setDuration(Toast.LENGTH_SHORT);
//        toast.setView(layout);
//        toast.show();
//    }
}
