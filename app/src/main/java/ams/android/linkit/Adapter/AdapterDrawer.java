package ams.android.linkit.Adapter;

/**
 * Created by Aidin on 11/19/2014.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ams.android.linkit.Model.DrawerMenuItem;
import ams.android.linkit.R;


public class AdapterDrawer extends ArrayAdapter<DrawerMenuItem> {
    private Context mContext;
    private LayoutInflater inflater;

    public AdapterDrawer(Context context) {
        super(context, 0);
        this.mContext = context;
    }

    public void addItem(DrawerMenuItem itemModel) {
        add(itemModel);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) convertView = inflater.inflate(R.layout.drawer_item, null);


        DrawerMenuItem item = getItem(position);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txtMenuTitle);
        ImageView imgThumb = (ImageView) convertView.findViewById(R.id.imgMenuThumb);

        txtTitle.setText(item.title);
        imgThumb.setImageResource(item.imageRes);

        return convertView;
    }

}

