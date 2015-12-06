package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class About extends Fragment {
    private final static String TAG = "LEE: <" + About.class.getSimpleName() + ">";

    public About() {
        Log.v(TAG, "About");
    }

    public void clearRightContainer(Activity activity) {
        Log.v(TAG, "clearRightContainer");
        if (activity.findViewById(R.id.right_container) != null) {
            int id = R.id.right_container;
            Log.v(TAG, "RIGHT CONTAINER: set right_container GONE");
            activity.findViewById(id).setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        // FIXED: use the entire screen
        clearRightContainer(getActivity());
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    // FIXED: onAttach(Activity activity) is deprecated
    @Override
    public void onAttach(Context context) {
        Log.v(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.setTitle(R.string.about);
            // FIXED: use the entire screen
            clearRightContainer(activity);
        }
        else {
            Log.w(TAG, "expected context instanceof Activity");
        }
    }

}
