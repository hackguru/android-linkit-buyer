package ams.android.linkit.Model;

/**
 * Created by Aidin on 11/19/2014.
 */
public class DrawerMenuItem {
    public String id;
    public String title;
    public String value;
    public int imageRes;

    public DrawerMenuItem()
    {

    }

    public DrawerMenuItem(String id, String title, String value, int imageRes) {
        this.id = id;
        this.title = title;
        this.value = value;
        this.imageRes = imageRes;
    }
}