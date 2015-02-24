package ams.android.linkit.Fragment;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;


/**
 * Created by Aidin on 2/1/2015.
 */
public class FragmentLogin extends Fragment {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "linkit";
    GoogleCloudMessaging gcm;
    Context context;
    WebView webView;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        webView = (WebView) rootView.findViewById(R.id.webView);
        webView.clearCache(true);
        webView.clearFormData();
        //CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        context = getActivity().getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            if (((GlobalApplication) getActivity().getApplication()).getRegistrationId().isEmpty()) {
                registerInBackground();
            } else {
                setUrl();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        //webView.loadUrl("https://instagram.com/accounts/login/");
        WebViewClient wvc = new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // probably you need to open that url rather than redirect:
                //view.loadUrl(url);
                return false; // then it is not handled by default action
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.startsWith(getResources().getString(R.string.BASE_URL) + "users/insta-buyer-cb")) {
                    login(getResources().getString(R.string.BASE_URL) + "users/userId");
                }
            }
        };

        webView.setWebViewClient(wvc);
        return rootView;
    }

    private void setUrl() {
        String url = getResources().getString(R.string.BASE_URL) + "users/auth/buyer/android/" + ((GlobalApplication) getActivity().getApplication()).getRegistrationId();
        webView.loadUrl(url);
    }

    private void registerInBackground() {
        new RetrieveFeedTask().execute();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
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
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                try {
                    parseJSON(new String(response, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "ERR");
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
                ft.replace(R.id.container, f1); // f1_container is your FrameLayout container
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack("Links");
                ft.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
    }

    private class RetrieveFeedTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String msg;
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
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
}
