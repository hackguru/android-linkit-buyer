package ams.android.linkit.Adapter;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ams.android.linkit.Model.LinkitObject;
import ams.android.linkit.R;

/**
 * Created by Aidin on 2/23/2015.
 */
public class AdapterListviewEmpty extends BaseAdapter {
    Context context;
    FragmentManager fragmentManager;
    ArrayList<LinkitObject> items = new ArrayList<LinkitObject>();
    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    ImageLoadingListener imageListener;

    public AdapterListviewEmpty(Context context, FragmentManager fragmentManager, ArrayList<LinkitObject> items) {
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
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        return Bitmap.createScaledBitmap(bitmap, 400, 400, true);
                    }
                })
                .build();

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
        if (rootView == null) rootView = inflater.inflate(R.layout.item_list_empty, null);

        ImageView imgProfile = (ImageView) rootView.findViewById(R.id.imgProfile);
        TextView txtOwner = (TextView) rootView.findViewById(R.id.txtOwner);
        TextView txtDesc = (TextView) rootView.findViewById(R.id.txtDesc);

        if (!items.get(position).productDescription.equals("null")) {
            txtDesc.setText(items.get(position).productDescription + " \n" + items.get(position).ownerWebsite);
        } else {
            txtDesc.setText("");
        }

        if (!items.get(position).owner.equals("null")) {
            txtOwner.setText(items.get(position).owner);
        } else {
            txtOwner.setText("");
        }

        imageLoader.displayImage(items.get(position).ownerProfilePic, imgProfile, options, imageListener);
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

}
