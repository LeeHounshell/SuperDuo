package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + AddBook.class.getSimpleName() + ">";

    private final static boolean USE_ZXING_SCANNER = true;

    private final static String EAN_CONTENT = "eanContent";
    private final static int SCANNER_ACTIVITY_CODE = 1;

    private AppCompatEditText mEan;
    private View mRootView;

    // FIXED: use an empty constructor for Fragment Instantiation
    public AddBook() {
        Log.v(TAG, "AddBook");
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
    public void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (mEan != null) {
            outState.putString(EAN_CONTENT, mEan.getText().toString());
        }
    }

    // FIXED: always adjust for isbn10
    private String getEan(Editable s) {
        Log.v(TAG, "getEan");
        String ean = s.toString();
        //catch isbn10 numbers
        String isbn_prefix = getResources().getString(R.string.misc_isbn_prefix);
        if (ean.length() == 10 && !ean.startsWith(isbn_prefix)) {
            ean = isbn_prefix + ean;
        }
        return ean;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        // FIXED: use the entire screen
        clearRightContainer(getActivity());

        mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        TextInputLayout textInputLayout = (TextInputLayout) mRootView.findViewById(R.id.eanTextInputLayout);
        textInputLayout.setHint(getString(R.string.input_hint));

        mEan = (AppCompatEditText) mRootView.findViewById(R.id.ean);

        mEan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = getEan(s);
                // FIXED: only auto-search when we have 10 or 13 digits
                String isbn_prefix = getResources().getString(R.string.misc_isbn_prefix);
                if ((ean.length() == 10 && !ean.startsWith(isbn_prefix)) || ean.length() == 13) {
                    String search_ean = ean;
                    if (search_ean.length() == 10) {
                        search_ean = isbn_prefix + ean;
                    }

                    Log.v(TAG, "searching for search_ean="+search_ean);
                    String searching = getResources().getString(R.string.searching);
                    Toast.makeText(getActivity(), searching, Toast.LENGTH_LONG).show();
                    // also vibrate the device to signal search
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(300); // Vibrate for 300 milliseconds

                    // we have an ISBN, start a book intent to do the actual search
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, search_ean);
                    bookIntent.setAction(BookService.FETCH_BOOK);
                    getActivity().startService(bookIntent);
                    AddBook.this.restartLoader();
                }
                else {
                    Log.v(TAG, "invalid ean="+ean);
                    clearFields();
                }
            }
        });

        mRootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "scan_button: CLICK!");
                // FIXED: use the entire screen
                clearRightContainer(getActivity());

                // FIXED: remove any currently showing book info
                clearFields();

                Intent i;
                // I have implemented both ZXing and ZBar scanner functionality..
                // the one used depends on the value for USE_ZXING_SCANNER
                if (USE_ZXING_SCANNER) {
                    Log.v(TAG, "USING ZXING SCANNER");
                    i = new Intent(getActivity(), ZXingScannerActivity.class);
                }
                else {
                    Log.v(TAG, "USING ZBAR SCANNER");
                    i = new Intent(getActivity(), ZBarScannerActivity.class);
                }
                startActivityForResult(i, SCANNER_ACTIVITY_CODE);
            }
        });

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "save_button: CLICK!");
                mEan.setText("");
            }
        });

        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "ADDBOOK - delete_button: CLICK!");
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, getEan(mEan.getText()));
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEan.setText("");
                // FIXED: remove the the right_container
                clearRightContainer(getActivity());
            }
        });

        if (savedInstanceState != null) {
            mEan.setText(savedInstanceState.getString(EAN_CONTENT));
            mEan.setHint("");
        }

        return mRootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        if (requestCode == SCANNER_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String format = data.getStringExtra("format");
                String content = data.getStringExtra("content");
                Log.v(TAG, "SCANNER: format: " + format + ", content: " + content);
                mEan.setText(content);
                //
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Log.v(TAG, "PLAY: <SHUTTER_CLICK>");
                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                }
                // also vibrate the device to signal capture, so NO AUDIO-ONLY FEEDBACK
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500); // Vibrate for 500 milliseconds
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // if there's no result
                Log.v(TAG, "SCANNER: <RESULT_CANCELED>");
            }
        }
    }

    private void restartLoader() {
        Log.v(TAG, "restartLoader");
        if (getLoaderManager() != null) {
            int LOADER_ID = 1;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            Log.w(TAG, "unable to restartLoader!");
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "onCreateLoader");
        if (getEan(mEan.getText()).length() == 0) {
            return null;
        }
        String eanStr = getEan(mEan.getText());
        long book_id;
        // FIXED: handle invalid ISBN or ISBN with text
        try {
            book_id = Long.parseLong(eanStr);
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.buildFullBookUri(book_id),
                    null,
                    null,
                    null,
                    null
            );
        } catch (NumberFormatException e) {
            Log.v(TAG, "invalid isbn:" + eanStr);
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        Log.v(TAG, "onLoadFinished");
        if (!cursor.moveToFirst()) {
            return;
        }

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = cursor.getString(cursor.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        // FIXED: authors NullPointerException
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }
        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        Log.v(TAG, "imgUrl="+imgUrl);
        // FIXED: imgUrl NullPointerException
        if (imgUrl != null) {
            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                new DownloadImage((ImageView) mRootView.findViewById(R.id.bookCover)).execute(imgUrl);
                Log.v(TAG, "onLoadFinished - mRootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);");
                mRootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
            }
        }

        String categories = cursor.getString(cursor.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        // FIXED: categories NullPointerException
        if (categories != null) {
            ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);
        }

        Log.v(TAG, "onLoadFinished - mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);");
        mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        Log.v(TAG, "onLoadFinished - mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);");
        mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset");
    }

    private void clearFields() {
        Log.v(TAG, "clearFields");
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.authors)).setText("");
        ((TextView) mRootView.findViewById(R.id.categories)).setText("");
        Log.v(TAG, "clearFields - mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        Log.v(TAG, "clearFields - mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);");
        mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        Log.v(TAG, "clearFields - mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);");
        mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    // FIXED: onAttach(Activity activity) is deprecated
    @Override
    public void onAttach(Context context) {
        Log.v(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.setTitle(R.string.scan);
        }
        else {
            Log.w(TAG, "expected context instanceof Activity");
        }
    }

}
