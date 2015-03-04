package ams.android.linkit.Activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import ams.android.linkit.Fragment.FragmentLinks;
import ams.android.linkit.Fragment.FragmentLogin;
import ams.android.linkit.Fragment.FragmentWebView;
import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends Activity {

    private static String TAG = "linkit";

    public static String currentFragmentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // clear Badget Counter
        try {
            ((GlobalApplication) getApplication()).setBadgetCount(0);
            ShortcutBadger.setBadge(getApplicationContext(), 0);
        } catch (Exception e) {
        }

        // check if Notification Received
        if (!getIntent().hasExtra("RunByNoti")) {
           if (savedInstanceState == null) {
                checkLogin();
            }
        } else {
            LinkitObject myObject = new LinkitObject();
            myObject.productLink = getIntent().getExtras().getString("productLink");
            myObject.imageUrl = getIntent().getExtras().getString("imageUrl");
            myObject.productDescription = getIntent().getExtras().getString("text");
            myObject.linkSrceenShot = getIntent().getExtras().getString("linkSrceenShot");

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            FragmentLinks f1 = new FragmentLinks();
            Bundle bundle = new Bundle();
            bundle.putParcelable("item", myObject);
            f1.setArguments(bundle);
            ft.replace(R.id.container, f1, "Links");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack("Links");
            ft.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
         Log.i(TAG, "popbackstack count: " + count);

        if (currentFragmentName.equals("WebView")) {
            FragmentWebView webFragment = (FragmentWebView) getFragmentManager().findFragmentByTag("WebView");
            if (!webFragment.isReadyForExit()) {
                webFragment.backButtonWasPressed();
            } else if (webFragment.canGoBackHistory()) {
                webFragment.goBack();
            } else {
                currentFragmentName = "Link";
                getFragmentManager().popBackStack();
            }
        } else if (currentFragmentName.equals("Intro")) {
            currentFragmentName = "Login";
            getFragmentManager().popBackStack();
        }
        else if (currentFragmentName.equals("Login")) {
            finish();
        } else if (currentFragmentName.equals("Link")) {
            finish();
        }
    }

    private void checkLogin() {
        if (((GlobalApplication) getApplication()).getUserId().isEmpty()) {
            FragmentLogin f1 = new FragmentLogin();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, f1, "Login");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack("Login");
            ft.commit();
        } else {
            FragmentLinks f1 = new FragmentLinks();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, f1, "Links");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack("Links");
            ft.commit();
        }
    }

//    @Override
//    protected void onUserLeaveHint() {
//        super.onUserLeaveHint();
//        finish();
//    }
}
