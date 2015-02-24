package ams.android.linkit.Adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ams.android.linkit.Fragment.FragmentWebView;
import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;
import ams.android.linkit.Tools.GlobalApplication;

/**
 * Created by Aidin on 2/3/2015.
 */
public class AdapterListview extends BaseAdapter {

    Activity activity;
    Context context;
    FragmentManager fragmentManager;
    ArrayList<LinkitObject> items = new ArrayList<LinkitObject>();
    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    ImageLoadingListener imageListener;

    public AdapterListview(Activity activity, FragmentManager fragmentManager, ArrayList<LinkitObject> items) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.fragmentManager = fragmentManager;
        this.items = items;
        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.fail)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.unlink).cacheInMemory(true)
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        return Bitmap.createScaledBitmap(bitmap, 400, 400, true);
                    }
                })
                .cacheOnDisk(true).build();

        imageListener = new ImageDisplayListener();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(final int position, View rootView, ViewGroup parent) {
        LayoutInflater inflater = null;
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (rootView == null) rootView = inflater.inflate(R.layout.item_list, null);

        ImageView imgLink = (ImageView) rootView.findViewById(R.id.img_link);
        ImageView imgInsta = (ImageView) rootView.findViewById(R.id.img_insta);
        ImageView imgProfile = (ImageView) rootView.findViewById(R.id.imgProfile);
        TextView txtCaption = (TextView) rootView.findViewById(R.id.txtDesc);
        TextView txtOwner = (TextView) rootView.findViewById(R.id.txtOwner);

//        final LayoutInflater finalInflater = inflater;
//        txtCaption.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CustomDialog cdd = new CustomDialog(activity);
//                cdd.show();
//            }
//        });

        if (!items.get(position).productDescription.equals("null")) {
            txtCaption.setText(items.get(position).productDescription);
        } else {
            txtCaption.setText("");
        }

        if (!items.get(position).owner.equals("null")) {
            txtOwner.setText(items.get(position).owner);
        } else {
            txtOwner.setText("");
        }

        imageLoader.displayImage(items.get(position).linkSrceenShot, imgLink, options, imageListener);
        imageLoader.displayImage(items.get(position).imageUrl, imgInsta, options, imageListener);
        imageLoader.displayImage(items.get(position).ownerProfilePic, imgProfile, options, imageListener);

        //if (!items.get(position).linkToProduct.equals("")) {
        imgLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new postOpenedAsync().execute(items.get(position).mediaID);
                FragmentWebView f1 = FragmentWebView.newInstance(items.get(position));
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                ft.add(R.id.container, f1, "WebView");
                ft.addToBackStack("WebView");
                ft.commit();
            }
        });
        return rootView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
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

    private class postOpenedAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
            HttpResponse response;
            JSONObject json = new JSONObject();

            try {
                String urlJSON = activity.getResources().getString(R.string.BASE_URL).toString() + "users/" + ((GlobalApplication) activity.getApplication()).getUserId() + "/opened/" + data[0];
                HttpPost post = new HttpPost(urlJSON);
                post.addHeader("token", ((GlobalApplication) activity.getApplication()).getRegistrationId());
                post.addHeader("device", "android");
                post.addHeader("userType", "buyer");
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);
                    /*Checking response */
                InputStream in = null;
                if (response != null) {
                    in = response.getEntity().getContent(); //Get the data in the entity
                }
                return "OK";

            } catch (Exception e) {
                Log.i("linkit Response: ", "error" + e.getMessage());
                e.printStackTrace();
                return "ERROR";
            }
        }

        @Override
        protected void onPostExecute(String result) {


        }
    }
}

