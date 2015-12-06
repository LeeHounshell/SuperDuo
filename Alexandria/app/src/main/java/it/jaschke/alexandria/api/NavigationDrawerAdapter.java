package it.jaschke.alexandria.api;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.jaschke.alexandria.DrawerItemData;
import it.jaschke.alexandria.NavigationDrawerFragment;
import it.jaschke.alexandria.R;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerViewHolder> {
    private final static String TAG = "LEE: <" + NavigationDrawerAdapter.class.getSimpleName() + ">";

    public static final int VIEW_TYPE_ITEM = 0;

    private static final int VIEW_TYPE_HEADER = 1;

    private final DrawerItemData[] drawerItemsData;

    private NavigationDrawerFragment navigationDrawerFragment;

    public NavigationDrawerAdapter(DrawerItemData[] drawerItemsData) {
        super();
        Log.v(TAG, "NavigationDrawerAdapter");
        this.drawerItemsData = drawerItemsData;
    }

    @Override
    public NavigationDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(TAG, "-----> onCreateViewHolder <-----");
        if (parent instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_HEADER: {
                    Log.v(TAG, "create: VIEW_TYPE_HEADER");
                    layoutId = R.layout.header;
                    break;
                }
                case VIEW_TYPE_ITEM: {
                    Log.v(TAG, "create: VIEW_TYPE_ITEM");
                    layoutId = R.layout.navigation_list_item;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new NavigationDrawerViewHolder(view, viewType, this);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(NavigationDrawerViewHolder holder, int position) {
        Log.v(TAG, "onBindViewHolder - position="+position);
        if (isPositionHeader(position)) {
            Log.v(TAG, "bind: VIEW_TYPE_HEADER");
        }
        else {
            Log.v(TAG, "bind: VIEW_TYPE_ITEM");
            // - get data from drawerItemsData at this position
            // - and replace the contents of the view with that data
            String menuItem = drawerItemsData[position-1].getMenuItem();
            int menuIcon = drawerItemsData[position-1].getImageUrl();
            Log.v(TAG, "menuItem=" + menuItem + ", menuIcon=" + menuIcon);
            holder.getDrawerItemView().setText(menuItem);
            holder.getIconView().setImageResource(menuIcon);
        }
    }

    @Override
    public int getItemCount() {
        //Log.v(TAG, "getItemCount");
        return drawerItemsData.length+1;
    }

    // check the type of view passed
    @Override
    public int getItemViewType(int position) {
        //Log.v(TAG, "getItemViewType");
        if (isPositionHeader(position)) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        //Log.v(TAG, "isPositionHeader");
        return position == 0;
    }

    public void setNavigationDrawerFragment(NavigationDrawerFragment navigationDrawerFragment) {
        //Log.v(TAG, "setNavigationDrawerFragment");
        this.navigationDrawerFragment = navigationDrawerFragment;
    }

    public NavigationDrawerFragment getNavigationDrawerFragment() {
        //Log.v(TAG, "getNavigationDrawerFragment");
        return navigationDrawerFragment;
    }

}
