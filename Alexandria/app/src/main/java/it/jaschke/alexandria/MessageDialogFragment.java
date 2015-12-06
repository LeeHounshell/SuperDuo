package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;


//
// from: https://github.com/dm77/barcodescanner/blob/5efbf424d8012ca53c0d1cdcdff14e25ae40a010/zbar/sample/src/main/java/me/dm7/barcodescanner/zbar/sample/MessageDialogFragment.java
//
public class MessageDialogFragment extends DialogFragment {

    @SuppressWarnings("UnusedParameters")
    public interface MessageDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    private String mTitle;
    private String mMessage;
    private MessageDialogListener mListener;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static MessageDialogFragment newInstance(String message, MessageDialogListener listener) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.mTitle = AlexandriaApplication.getAppContext().getResources().getString(R.string.misc_scan_results);
        fragment.mMessage = message;
        fragment.mListener = listener;
        return fragment;
    }

    // FIXED: Not annotated method overrides method annotated with @NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setTitle(mTitle);

        String ok = AlexandriaApplication.getAppContext().getResources().getString(R.string.misc_ok);
        builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mListener != null) {
                    mListener.onDialogPositiveClick(MessageDialogFragment.this);
                }
            }
        });

        return builder.create();
    }

}
