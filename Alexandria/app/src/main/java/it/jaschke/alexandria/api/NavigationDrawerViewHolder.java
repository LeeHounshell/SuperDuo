package it.jaschke.alexandria.api;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.R;

public class NavigationDrawerViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener
{
    private final static String TAG = "LEE: <" + NavigationDrawerViewHolder.class.getSimpleName() + ">";

    private final NavigationDrawerAdapter navigationDrawerAdapter;

    private ImageView mIconView;
    private TextView mDrawerItemView;

    public NavigationDrawerViewHolder(View itemView, int viewType, NavigationDrawerAdapter navigationDrawerAdapter) {
        super(itemView);
        this.navigationDrawerAdapter = navigationDrawerAdapter;
        if (viewType == NavigationDrawerAdapter.VIEW_TYPE_ITEM) {
            Log.v(TAG, "NavigationDrawerViewHolder - VIEW_TYPE_ITEM");
            mIconView = (ImageView) itemView.findViewById(R.id.list_item_icon);
            mDrawerItemView = (TextView) itemView.findViewById(R.id.list_item_text);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }
        else {
            Log.v(TAG, "NavigationDrawerViewHolder - VIEW_TYPE_HEADER");
        }
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        Log.v(TAG, "NavigationDrawer: CLICK! - position="+position);
        navigationDrawerAdapter.getNavigationDrawerFragment().selectItem(position-1);
    }

    public ImageView getIconView() {
        return mIconView;
    }

    public TextView getDrawerItemView() {
        return mDrawerItemView;
    }

}
