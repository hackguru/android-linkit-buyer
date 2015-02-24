package ams.android.linkitmerchant.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import ams.android.linkitmerchant.Adapter.AdapterListview;
import ams.android.linkitmerchant.Model.LinkitObject;
import ams.android.linkitmerchant.R;
import ams.android.linkitmerchant.Tools.GlobalApplication;
import ams.android.linkitmerchant.Tools.myListView;


/**
 * Created by Aidin on 2/1/2015.
 */
public class FragmentLinks extends Fragment {

    static ArrayList<LinkitObject> items = new ArrayList<LinkitObject>();
    static AdapterListview adapterListview;
    myListView listView;
    String userID, regID;
    LayoutInflater ginflater;
    View grootView;
    SwipeRefreshLayout swipeLayout;
    LinkitObject currentItem;
    RelativeLayout layWaiting;
    Boolean callState = false;

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
        ImageButton btnLogout = (ImageButton) rootView.findViewById(R.id.btn_logout);
        layWaiting = (RelativeLayout) rootView.findViewById(R.id.lay_waiting);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setIcon(R.drawable.linkit)
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


        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(null, null, getResources().getString(R.string.PAGING_COUNT));
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        adapterListview = new AdapterListview(getActivity(), getFragmentManager(), items);

        listView = (myListView) rootView.findViewById(R.id.listView);
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
                        addData(null, items.get(items.size() - 1).createdDate, getResources().getString(R.string.PAGING_COUNT));
                    }
                }
            }
        });
        listView.setAdapter(adapterListview);

        refreshData(null, null, getResources().getString(R.string.PAGING_COUNT));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData(null, null, getResources().getString(R.string.PAGING_COUNT));
    }

    private void showToast(String text) {
        View layout = ginflater.inflate(R.layout.toast, (ViewGroup) grootView.findViewById(R.id.toast_layout_root));
        final CardView card = (CardView) layout.findViewById(R.id.card_view_toast);
        card.setCardBackgroundColor(Color.parseColor("#2191c1"));
        TextView textView = (TextView) layout.findViewById(R.id.text);
        textView.setText(text);
        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public void serverLogout() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "merchant");
        String URL = getResources().getString(R.string.BASE_URL) + "users/updateregid";
        client.post(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                ((GlobalApplication) getActivity().getApplication()).clearAllSettings();
                showToast("Logout");
                FragmentLogin f1 = new FragmentLogin();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, f1); // f1_container is your FrameLayout container
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack("Login");
                ft.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (statusCode == 401) {
                    Log.e("linkit-merchant", "ERR 401");
                    ((GlobalApplication) getActivity().getApplication()).clearAllSettings();
                    showToast("Logout");
                    FragmentLogin f1 = new FragmentLogin();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container, f1); // f1_container is your FrameLayout container
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.addToBackStack("Login");
                    ft.commit();
                } else {
                    Log.e("linkit-merchant", "ERR");
                }

            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    public void addData(String startDate, String endDate, String count) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();

        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "merchant");

        if (startDate != null) {
            requestParams.add("startDate", startDate);
        }

        if (endDate != null) {
            requestParams.add("endDate", endDate);
        }

        if (count != null) {
            requestParams.add("count", count);
        }

        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/postedmedias";
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
                adapterListview.notifyDataSetChanged();
                layWaiting.setVisibility(View.INVISIBLE);
                callState = false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                callState = false;
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    public void refreshData(String startDate, String endDate, String count) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();

        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "merchant");
        if (startDate != null) {
            requestParams.add("startDate", startDate);
        }

        if (endDate != null) {
            requestParams.add("endDate", endDate);
        }

        if (count != null) {
            requestParams.add("count", count);
        }
        String URL = getResources().getString(R.string.BASE_URL) + "users/" + userID + "/postedmedias";
        client.get(URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                items.clear();
                try {
                    parseJSON(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
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


                //showToast("Data Updated");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                //Log.e("linkit-merchant", "ERR : " + errorResponse.toString());
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
                for (int i = 0; i < feeds.length(); i++) {
                    JSONObject item = feeds.getJSONObject(i);
                    LinkitObject myobject = new LinkitObject();

                    if (item.has("_id")) {
                        myobject.mediaID = item.getString("_id");
                    } else {
                        myobject.mediaID = "";
                    }
                    // date
                    if (item.has("created")) {
                        myobject.createdDate = item.getString("created");
                    } else {
                        myobject.createdDate = "";
                    }
                    if (item.has("productDescription")) {
                        myobject.productDescription = item.getString("productDescription");
                    } else {
                        myobject.productDescription = "";
                    }
                    if (item.has("linkToProduct")) {
                        myobject.productLink = item.getString("linkToProduct");
                    } else {
                        myobject.productLink = "";
                    }
                    if (item.has("productLinkScreenshot")) {
                        myobject.linkSrceenShot = item.getString("productLinkScreenshot");
                    } else {
                        myobject.linkSrceenShot = "";
                    }
                    if (item.getJSONObject("images").getJSONObject("standard_resolution").has("url")) {
                        myobject.imageUrl = item.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                    } else {
                        myobject.imageUrl = "";
                    }
                    items.add(myobject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //Log.e(TAG, "Couldn't get any data from the url");
        }
    }
}
