package ams.android.linkit.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ams.android.linkit.Adapter.AdapterDrawer;
import ams.android.linkit.Fragment.FragmentLinks;
import ams.android.linkit.Fragment.FragmentLogin;
import ams.android.linkit.Fragment.FragmentWebView;
import ams.android.linkit.Model.DrawerMenuItem;
import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends Activity {

    private static String TAG = "linkit";
    private static DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    public static String currentFragmentName = "";
    private ArrayList<DrawerMenuItem> menus = new ArrayList<>();
    private Context mContext;
    public static void openDrawerMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mContext = this;
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
        fillMenu();
        _initMenu();
    }

    private void _initMenu() {
        AdapterDrawer mAdapter = new AdapterDrawer(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_launcher, R.string.accept, R.string.decline) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        for (DrawerMenuItem myMenuItem : menus) mAdapter.addItem(myMenuItem);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        if (mDrawerList != null) mDrawerList.setAdapter(mAdapter);
        mDrawerLayout.setScrimColor(Color.parseColor("#aaffffff"));
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.drawer_header, mDrawerList, false);
        mDrawerList.addHeaderView(header, null, false);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                               @Override
                                               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                   mDrawerList.setItemChecked(position - 1, true);
                                                   mDrawerLayout.closeDrawer(mDrawerList);
                                                   if (position == 1) {
                                                       FragmentLinks.txtMainTitle.setText(menus.get(position - 1).title);
                                                       ((FragmentLinks) getFragmentManager().findFragmentByTag("Links")).refreshLikesData();
                                                   } else if (position == 3) {
                                                       FragmentLinks.txtMainTitle.setText(menus.get(position - 1).title);
                                                       ((FragmentLinks) getFragmentManager().findFragmentByTag("Links")).refreshFeaturedData();
                                                   } else if (position == 9) {
                                                       AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                       builder
                                                               .setTitle("Logout")
                                                               .setMessage("Do you want to logout?")
                                                               .setIcon(R.drawable.ic_launcher)
                                                               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                   public void onClick(DialogInterface dialog, int which) {
                                                                       ((FragmentLinks) getFragmentManager().findFragmentByTag("Links")).serverLogout();
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
                                               }
                                           }

        );
    }

    private void fillMenu() {
        DrawerMenuItem myMenu = new DrawerMenuItem("1", "My Likes", "1", R.drawable.likes);
        menus.add(myMenu);
        myMenu = new DrawerMenuItem("2", "My Merchants", "2", R.drawable.mymerchant);
        menus.add(myMenu);
        myMenu = new DrawerMenuItem("3", "Featured Merchants", "3", R.drawable.featured);
        menus.add(myMenu);
        myMenu = new DrawerMenuItem("9", "Sign Out ", "3", R.drawable.logout);
        menus.add(myMenu);
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
        } else if (currentFragmentName.equals("Login")) {
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
