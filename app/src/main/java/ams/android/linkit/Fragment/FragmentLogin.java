package ams.android.linkit.Fragment;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.URL;

import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;


/**
 * Created by Aidin on 2/1/2015.
 */
public class FragmentLogin extends Fragment {

    private static String TAG = "linkit";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleCloudMessaging gcm;
    Context context;
    WebView webView;
    ImageView imageReload;


    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (!(getActivity().getRequestedOrientation() ==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        webView = (WebView) rootView.findViewById(R.id.webViewLogin);
        imageReload = (ImageView) rootView.findViewById(R.id.imgRefresh);
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
                new hasActiveInternetConnectionTask().execute();
            }
        });

        CookieSyncManager.createInstance(getActivity());
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
        WebViewClient wvc = new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // then it is not handled by default action
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                imageReload.setVisibility(View.VISIBLE);
                imageReload.bringToFront();
                webView.loadData("<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" ?>", "text/html", "UTF-8");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
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
        return rootView;
    }

    class hasActiveInternetConnectionTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (isNetworkAvailable(context)) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                    urlc.setRequestProperty("User-Agent", "Test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();

                    return (urlc.getResponseCode() == 200);
                } catch (IOException e) {
                    Log.e(TAG, "Error checking internet connection", e);
                    return false;
                }
            } else {
                Log.d(TAG, "No network available!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setUrl();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Internet connection required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    private void setUrl() {
        String url = getResources().getString(R.string.BASE_URL) + "users/auth/buyer/android/" + ((GlobalApplication) getActivity().getApplication()).getRegistrationId();
        webView.loadUrl(url);
        imageReload.setVisibility(View.INVISIBLE);
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
                //ft.addToBackStack("Links");
                ft.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
    }
}
