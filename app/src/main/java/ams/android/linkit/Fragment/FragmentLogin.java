package ams.android.linkit.Fragment;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ams.android.linkit.Activity.MainActivity;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;


/**
 * Created by Aidin on 2/1/2015.
 */
public class FragmentLogin extends Fragment {

    private static String TAG = "linkitShopper";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleCloudMessaging gcm;
    WebView webView;
    ImageView imageReload;
    ProgressBar progressBarLoad;

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
//        if (!(getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }

        ((MainActivity) getActivity()).currentFragmentName = "Login";

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        webView = (WebView) rootView.findViewById(R.id.webViewLogin);
        imageReload = (ImageView) rootView.findViewById(R.id.imgRefresh);
        progressBarLoad = (ProgressBar) rootView.findViewById(R.id.progressBar_load);

        ImageView imagePlay = (ImageView) rootView.findViewById(R.id.imgPlay);
        imagePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentIntro f1 = new FragmentIntro();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                ft.replace(R.id.container, f1, "Intro");
                ft.addToBackStack("Intro");
                ft.commit();
            }
        });

        imageReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUrl();
                //new hasActiveInternetConnectionTask().execute();
            }
        });
        imageReload.bringToFront();
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT > 20) {
            cookieManager.flush();
        } else {
            cookieManager.removeAllCookie();
        }

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(GlobalApplication.getAppContext());
            if (((GlobalApplication) getActivity().getApplication()).getRegistrationId().isEmpty()) {
                registerInBackground();
            } else {
                setUrl();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        WebViewClient wvc = new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadData("<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" ?>", "text/html", "UTF-8");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBarLoad.setVisibility(View.INVISIBLE);
                try {
                    if (url.startsWith(getResources().getString(R.string.BASE_URL) + "users/insta-buyer-cb")) {
                        login(getResources().getString(R.string.BASE_URL) + "users/userId");
                    }
                } catch (Exception ex) {
                }
                super.onPageFinished(view, url);
            }
        };
        webView.setWebViewClient(wvc);

        // Get tracker.
        Tracker t = ((GlobalApplication) getActivity().getApplication()).getTracker(GlobalApplication.TrackerName.APP_TRACKER);
        t.setScreenName("LinkitShopper - Login");
        t.send(new HitBuilders.AppViewBuilder().build());

        return rootView;
    }

    private void setUrl() {
        String url = getResources().getString(R.string.BASE_URL) + "users/auth/buyer/android/" + ((GlobalApplication) getActivity().getApplication()).getRegistrationId();
        webView.loadUrl(url);
        progressBarLoad.setVisibility(View.VISIBLE);
    }

    private void registerInBackground() {
        new RetrieveFeedTask().execute();
    }

    private class RetrieveFeedTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String msg;
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(GlobalApplication.getAppContext());
                }
                String regId = gcm.register(getResources().getString(R.string.SENDER_ID));
                msg = "Device registered, registration ID=" + regId;
                ((GlobalApplication) getActivity().getApplication()).setRegistrationId(regId);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setUrl();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(GlobalApplication.getAppContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }

    private void login(String URL) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("token", ((GlobalApplication) getActivity().getApplication()).getRegistrationId());
        client.addHeader("device", "android");
        client.addHeader("userType", "buyer");
        client.get(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    parseJSON(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
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
                ((GlobalApplication) getActivity().getApplication()).setUserId(jsonObj.getString("userId"));
                FragmentLinks f1 = new FragmentLinks();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, f1, "Links");
                ft.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
    }
}
