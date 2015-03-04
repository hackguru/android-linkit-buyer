package ams.android.linkit.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Aidin on 2/3/2015.
 */
public class LinkitObject implements Parcelable {
    public String mediaID;
    public String createdDate;
    public String productDescription;
    public String owner;
    public String ownerProfilePic;
    public String ownerWebsite;
    public String caption;
    public String productLink;
    public String linkSrceenShot;
    public String imageUrl;
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaID);
        dest.writeString(createdDate);
        dest.writeString(productDescription);
        dest.writeString(owner);
        dest.writeString(ownerProfilePic);
        dest.writeString(ownerWebsite);
        dest.writeString(caption);
        dest.writeString(productLink);
        dest.writeString(linkSrceenShot);
        dest.writeString(imageUrl);
    }
}
