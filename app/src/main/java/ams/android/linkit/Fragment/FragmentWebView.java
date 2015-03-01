package ams.android.linkit.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;


/**
 * Created by Aidin on 2/3/2015.
 */
public abstract class FragmentWebView extends Fragment {
    private static String defaultURL = "http://www.google.com/?gws_rd=ssl";
    protected BackHandlerInterface backHandlerInterface;
    RelativeLayout mainView;
    WebView vistaWeb;
    ImageButton btnBack, btnForward, btnLinkout;
    LinkitObject currentItem;
    Bitmap bm;
    String urlPhoto, urlJSON;
    Boolean isInWebViewState = true;
    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    ImageLoadingListener imageListener;

    public static final FragmentWebView newInstance(LinkitObject item) {
        FragmentWebView f = new FragmentWebView() {
            @Override
            public boolean onBackPressed() {
                return false;
            }
        };
        f.currentItem = item;
        return f;
    }

    public abstract boolean onBackPressed();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        mainView = (RelativeLayout)rootView.findViewById(R.id.lay_MainView);
        vistaWeb = (WebView) rootView.findViewById(R.id.webView_Content);
        final ProgressBar progressBarLoad = (ProgressBar) rootView.findViewById(R.id.progressBar_load);
        RelativeLayout layBottomBar = (RelativeLayout) rootView.findViewById(R.id.lay_bottomBar);
        btnBack = (ImageButton) rootView.findViewById(R.id.btn_back);
        btnForward = (ImageButton) rootView.findViewById(R.id.btn_forward);
        btnLinkout = (ImageButton) rootView.findViewById(R.id.btn_linkout);
        Button btnDone = (Button) rootView.findViewById(R.id.btn_done);
        final ImageView imgInsta = (ImageView) rootView.findViewById(R.id.img_insta_preview);

        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.fail)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.unlink).cacheInMemory(true)
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        return Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                    }
                })
                .cacheOnDisk(true).build();

        imageListener = new ImageDisplayListener();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity().getApplicationContext()));
        }
        imageLoader = ImageLoader.getInstance();

        ViewTreeObserver vto = layBottomBar.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageLoader.displayImage(currentItem.imageUrl, imgInsta, options, imageListener);
                imgInsta.getViewTreeObserver().removeOnPreDrawListener(this);
                imgInsta.getLayoutParams().width = imgInsta.getMeasuredHeight();
                return true;
            }
        });

        urlPhoto = getResources().getString(R.string.BASE_URL).toString() + "media/matchScreenShot/" + currentItem.mediaID;
        urlJSON = getResources().getString(R.string.BASE_URL).toString() + "media/match/" + currentItem.mediaID;

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vistaWeb.goBack();
                // checkNavigationButton();
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vistaWeb.goForward();
                //checkNavigationButton();
            }
        });

        btnLinkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.productLink));
                startActivity(browserIntent);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = getFragmentManager().findFragmentByTag("WebView");
                getActivity().getFragmentManager().beginTransaction().remove(currentFragment).commit();
            }
        });

        vistaWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBarLoad.setVisibility(View.VISIBLE);
                progressBarLoad.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBarLoad.setVisibility(View.INVISIBLE);
                //checkNavigationButton();
            }
        });

        //vistaWeb.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.2; GT-I9500 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.109 Mobile Safari/537.36");
        vistaWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        vistaWeb.getSettings().setAppCacheEnabled(true);
        vistaWeb.getSettings().setJavaScriptEnabled(true);
        vistaWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        if (currentItem.productLink.isEmpty()) {
            vistaWeb.loadUrl(defaultURL);
        } else {
            vistaWeb.loadUrl(currentItem.productLink);
        }

        // Get tracker.
        Tracker t = ((GlobalApplication) getActivity().getApplication()).getTracker(GlobalApplication.TrackerName.APP_TRACKER);
        t.setScreenName("LinkitShopper - WebView");
        t.send(new HitBuilders.AppViewBuilder().build());


        return rootView;
    }

//    private void checkNavigationButton() {
//        if (vistaWeb.canGoBack()) {
//            btnBack.setVisibility(View.VISIBLE);
//        } else {
//            btnBack.setVisibility(View.INVISIBLE);
//        }
//
//        if (vistaWeb.canGoForward()) {
//            btnForward.setVisibility(View.VISIBLE);
//        } else {
//            btnForward.setVisibility(View.INVISIBLE);
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(getActivity() instanceof BackHandlerInterface)) {
            throw new ClassCastException("Hosting activity must implement BackHandlerInterface");
        } else {
            backHandlerInterface = (BackHandlerInterface) getActivity();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        backHandlerInterface.setSelectedFragment(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainView.removeView(vistaWeb);
        vistaWeb.setFocusable(true);
        vistaWeb.removeAllViews();
        //vistaWeb.clearHistory();
        vistaWeb.destroy();
    }


    public void goBack() {
        vistaWeb.goBack();
    }

    public void backButtonWasPressed() {
        vistaWeb.setDrawingCacheEnabled(false);
        vistaWeb.setVisibility(View.VISIBLE);
        //checkNavigationButton();
        isInWebViewState = true;

    }

    public Boolean canGoBackHistory() {
        return vistaWeb.canGoBack();
    }

    public Boolean isReadyForExit() {
        if (isInWebViewState) {
            return true;
        } else {
            return false;
        }

    }

    public interface BackHandlerInterface {
        public void setSelectedFragment(FragmentWebView backHandledFragment);
    }

    private class ImageDisplayListener extends SimpleImageLoadingListener {
        final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            super.onLoadingFailed(imageUri, view, failReason);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 0);
                    displayedImages.add(imageUri);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            super.onLoadingCancelled(imageUri, view);
        }
    }
}
