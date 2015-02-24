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
import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends Activity implements FragmentWebView.BackHandlerInterface {

    private static String TAG = "linkit";


    private FragmentWebView selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
//            FragmentIntro f1 = new FragmentIntro();
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
//            ft.replace(R.id.container, f1, "Intro");
//            //ft.addToBackStack("Intro");
//            ft.commit();
        } else {
            if (!getIntent().hasExtra("RunByNoti")) {
                checkLogin();
            } else {
                LinkitObject myObject = new LinkitObject();
                myObject.productLink = getIntent().getExtras().getString("productLink");
                myObject.imageUrl = getIntent().getExtras().getString("imageUrl");
                myObject.productDescription = getIntent().getExtras().getString("text");
                myObject.linkSrceenShot = getIntent().getExtras().getString("linkSrceenShot");
                new FragmentLinks();
                FragmentLinks f1 = FragmentLinks.newInstance(myObject);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, f1, "Links");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack("Links");
                ft.commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    @Override
    public void onBackPressed() {
        try {
            int count = getFragmentManager().getBackStackEntryCount();
            if (count > 1) {
                if ((getFragmentManager().getBackStackEntryAt(count - 1).getName().equals("WebView")) && !selectedFragment.isReadyForExit()) {
                    selectedFragment.backButtonWasPressed();
                    Log.i(TAG, "Web - BackButton");
                } else if (selectedFragment.isReadyForExit() && selectedFragment.canGoBackHistory()) {
                    selectedFragment.goBack();
                    Log.i(TAG, "Go Back");
                } else if ((getFragmentManager().getBackStackEntryAt(count - 1).getName().equals("Links"))) {
                    /// ((FragmentLinks)getFragmentManager().findFragmentByTag("Links")).refreshData();
                    Log.i(TAG, "backStack");
                } else {
                    getFragmentManager().popBackStack();
                    Log.i(TAG, "None - popbackstack");
                }
            } else {
                getFragmentManager().popBackStack();
                finish();
            }
        } catch (Exception e) {
            getFragmentManager().popBackStack();
            Log.i(TAG, "Error on back - " + e.getMessage());
        }
    }


    @Override
    public void setSelectedFragment(FragmentWebView selectedFragment) {
        this.selectedFragment = selectedFragment;
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
}
