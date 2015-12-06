package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import java.util.ArrayList;

import me.dm7.barcodescanner.zbar.BarcodeFormat;


//
// from: https://github.com/dm77/barcodescanner/blob/5efbf424d8012ca53c0d1cdcdff14e25ae40a010/zbar/sample/src/main/java/me/dm7/barcodescanner/zbar/sample/FormatSelectorDialogFragment.java
//
public class FormatSelectorDialogFragment extends DialogFragment {
    private final static String TAG = "LEE: <" + FormatSelectorDialogFragment.class.getSimpleName() + ">";

    public interface FormatSelectorDialogListener {
        void onFormatsSaved(ArrayList<Integer> selectedIndices);
    }

    private ArrayList<Integer> mSelectedIndices;
    private FormatSelectorDialogListener mListener;

    public void onCreate(Bundle state) {
        Log.v(TAG, "onCreate");
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static FormatSelectorDialogFragment newInstance(FormatSelectorDialogListener listener, ArrayList<Integer> selectedIndices) {
        Log.v(TAG, "newInstance");
        FormatSelectorDialogFragment fragment = new FormatSelectorDialogFragment();
        if (selectedIndices == null) {
            selectedIndices = new ArrayList<>();
        }
        fragment.mSelectedIndices = new ArrayList<>(selectedIndices);
        fragment.mListener = listener;
        return fragment;
    }

    // FIXED: Not annotated method overrides method annotated with @NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(TAG, "onCreateDialog");
        String[] formats = new String[BarcodeFormat.ALL_FORMATS.size()];
        boolean[] checkedIndices = new boolean[BarcodeFormat.ALL_FORMATS.size()];
        int i = 0;
        for (BarcodeFormat format : BarcodeFormat.ALL_FORMATS) {
            formats[i] = format.getName();
            // FIXED: efficiency
            checkedIndices[i] = mSelectedIndices.contains(i);
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.choose_formats)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(formats, checkedIndices,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedIndices.add(which);
                                } else if (mSelectedIndices.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedIndices.remove(mSelectedIndices.indexOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.good_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedIndices results somewhere
                        // or return them to the component that opened the dialog
                        if (mListener != null) {
                            mListener.onFormatsSaved(mSelectedIndices);
                        }
                    }
                })
                .setNegativeButton(R.string.trash_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        return builder.create();
    }

}
