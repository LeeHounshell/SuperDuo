package it.jaschke.alexandria;

import android.util.Log;

public class DrawerItemData {
    private final static String TAG = "LEE: <" + DrawerItemData.class.getSimpleName() + ">";

    private final String menuItem;
    private final int imageUrl;

    public DrawerItemData(String menuItem, int imageUrl){
        Log.v(TAG, "DrawerItemData");
        this.menuItem = menuItem;
        this.imageUrl = imageUrl;
    }

    public String getMenuItem() {
        Log.v(TAG, "getMenuItem");
        return menuItem;
    }

    public int getImageUrl() {
        Log.v(TAG, "getImageUrl");
        return imageUrl;
    }

}
