package ams.android.linkit.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import ams.android.linkit.Activity.MainActivity;
import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;


/**
 * Created by Aidin on 2/3/2015.
 */
public class FragmentWebView extends Fragment {
    private static String TAG = "linkitShopper";
    private static String defaultURL = "http://www.google.com/?gws_rd=ssl";
    RelativeLayout mainView;
    static ImageLoader imageLoader = ImageLoader.getInstance();
    static DisplayImageOptions options;
    static DisplayImageOptions optionsFull;
    static ImageLoadingListener imageListener;
    WebView vistaWeb;
    ImageButton btnBack;
    ImageButton btnForward;
    ImageButton btnLinkout;
    LinkitObject currentItem;
    String urlPhoto, urlJSON;
    Boolean isInWebViewState = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (!(getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        ((MainActivity) getActivity()).currentFragmentName = "WebView";
        try {
            currentItem = getArguments().getParcelable("item");
        } catch (Exception ex) {
        }
        final View rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        final ProgressBar progressBarLoad = (ProgressBar) rootView.findViewById(R.id.progressBar_load);
        final ImageView imgInsta = (ImageView) rootView.findViewById(R.id.img_insta_preview);
        final ImageView imgInstaFull = (ImageView) rootView.findViewById(R.id.imgInstaPreviewFull);
        RelativeLayout layBottomBar = (RelativeLayout) rootView.findViewById(R.id.lay_bottomBar);
        Button btnDone = (Button) rootView.findViewById(R.id.btnDone);
        mainView = (RelativeLayout) rootView.findViewById(R.id.lay_MainView);
        vistaWeb = (WebView) rootView.findViewById(R.id.webView_Content);
        btnBack = (ImageButton) rootView.findViewById(R.id.btn_back);
        btnForward = (ImageButton) rootView.findViewById(R.id.btn_forward);
        btnLinkout = (ImageButton) rootView.findViewById(R.id.btn_linkout);

        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.fail)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.unlink)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        return Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                    }
                })
                .build();
        optionsFull = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.fail)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.unlink)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

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
            }
        });
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vistaWeb.goForward();
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
                getFragmentManager().popBackStack();
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

        vistaWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        vistaWeb.getSettings().setAppCacheEnabled(true);
        vistaWeb.getSettings().setJavaScriptEnabled(true);
        vistaWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        if (currentItem.productLink.isEmpty()) {
            vistaWeb.loadUrl(defaultURL);
        } else {
            vistaWeb.loadUrl(currentItem.productLink);
        }

        imgInsta.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        imgInstaFull.setVisibility(View.VISIBLE);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        imgInstaFull.setVisibility(View.INVISIBLE);
                        return true;
                }
                return false;
            }
        });
        imageLoader.displayImage(currentItem.imageUrl, imgInstaFull, optionsFull, imageListener);

        // Get tracker.
        Tracker t = ((GlobalApplication) getActivity().getApplication()).getTracker(GlobalApplication.TrackerName.APP_TRACKER);
        t.setScreenName("LinkitShopper - WebView");
        t.send(new HitBuilders.AppViewBuilder().build());

        return rootView;
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

    public void goBack() {
        vistaWeb.goBack();
    }

    public void backButtonWasPressed() {
        vistaWeb.setDrawingCacheEnabled(false);
        vistaWeb.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnForward.setVisibility(View.VISIBLE);
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
}
