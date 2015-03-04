package ams.android.linkit.Adapter;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

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
    private static String TAG = "linkitShopper";
    private Context context;
    private final FragmentManager fragmentManager;
    private ArrayList<LinkitObject> items = new ArrayList<>();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private final DisplayImageOptions options;
    private final ImageLoadingListener imageListener;


    public AdapterListview(Context context, FragmentManager fragmentManager, ArrayList<LinkitObject> items) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.items = items;
        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.fail)
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.unlink)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        imageListener = new ImageDisplayListener();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }
        imageLoader = ImageLoader.getInstance();
    }

    private static class ViewHolder {
        public ImageView imgLink;
        public ImageView imgInsta;
        public ImageView imgProfile;
        public TextView txtDesc;
        public TextView txtOwner;
    }

    @Override
    public View getView(final int position, View rootView, ViewGroup parent) {
        final ViewHolder holder;
        if (rootView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.item_list, null);
            holder = new ViewHolder();
            holder.imgLink = (ImageView) rootView.findViewById(R.id.img_link);
            holder.imgInsta = (ImageView) rootView.findViewById(R.id.img_insta);
            holder.imgProfile = (ImageView) rootView.findViewById(R.id.imgProfile);
            holder.txtDesc = (TextView) rootView.findViewById(R.id.txtDesc);
            holder.txtOwner = (TextView) rootView.findViewById(R.id.txtOwner);
            rootView.setTag(holder);
        } else {
            holder = (ViewHolder) rootView.getTag();
        }
        imageLoader.displayImage(items.get(position).linkSrceenShot, holder.imgLink, options, imageListener);
        imageLoader.displayImage(items.get(position).imageUrl, holder.imgInsta, options, imageListener);
        imageLoader.displayImage(items.get(position).ownerProfilePic, holder.imgProfile, options, imageListener);

        if (!items.get(position).caption.equals("null")) {
            holder.txtDesc.setText(items.get(position).caption);
        } else {
            holder.txtDesc.setText("");
        }
        if (!items.get(position).owner.equals("null")) {
            holder.txtOwner.setText(items.get(position).owner);
        } else {
            holder.txtOwner.setText("");
        }
        holder.imgLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentWebView f1 = new FragmentWebView();
                Bundle bundle = new Bundle();
                bundle.putParcelable("item", items.get(position));
                f1.setArguments(bundle);
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.add(R.id.container, f1, "WebView");
                ft.addToBackStack("WebView");
                ft.commit();
                new postOpenedAsync().execute(items.get(position).mediaID);
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
        final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

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
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            JSONObject json = new JSONObject();
            try {
                String urlJSON = context.getResources().getString(R.string.BASE_URL).toString() + "users/" + ((GlobalApplication) context.getApplicationContext()).getUserId() + "/opened/" + data[0];
                HttpPost post = new HttpPost(urlJSON);
                post.addHeader("token", ((GlobalApplication) context.getApplicationContext()).getRegistrationId());
                post.addHeader("device", "android");
                post.addHeader("userType", "buyer");
                StringEntity se = new StringEntity(json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                client.execute(post);
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

