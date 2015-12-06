package it.jaschke.alexandria;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + ListOfBooksFragment.class.getSimpleName() + ">";

    private ListOfBooksCallbacks mCallbacks;
    private BookListAdapter mBookListAdapter;
    private RecyclerView mBookListRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private AppCompatEditText mSearchText;

    public ListOfBooksFragment() {
        Log.v(TAG, "ListOfBooksFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");

        // NOTE: the cursor close happens in mBookListAdapter.closeDown()
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        mBookListAdapter = new BookListAdapter(cursor);

        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        TextInputLayout textInputLayout = (TextInputLayout) rootView.findViewById(R.id.searchTextInputLayout);
        textInputLayout.setHint(getString(R.string.search_hint));

        mSearchText = (AppCompatEditText) rootView.findViewById(R.id.search_text);
        // FIXED: allow the enter key to kick off a search..
        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.v(TAG, "[ENTER] pressed!");
                    // FIXED: pop down the keyboard
                    hide_keyboard();
                    ListOfBooksFragment.this.restartLoader();
                    return true;
                }
                return false;
            }
        });

        rootView.findViewById(R.id.search_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "search_button: CLICK!");
                        // FIXED: pop down the keyboard
                        hide_keyboard();
                        ListOfBooksFragment.this.restartLoader();
                    }
                }
        );

        mBookListAdapter.setListOfBooksFragment(this);
        mBookListRecyclerView = (RecyclerView) rootView.findViewById(R.id.listOfBooks);
        mBookListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBookListRecyclerView.setAdapter(mBookListAdapter);
        mBookListRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return rootView;
    }

    private void selectItem(final String ean) {
        Log.v(TAG, "selectItem: ean="+ean);
        mCallbacks.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "SELECT ITEM: "+mPosition);
                mCallbacks.onItemSelected(ean);
            }
        });
    }

    private void hide_keyboard() {
        Log.v(TAG, "hide_keyboard");
        mCallbacks.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "HIDE THE KEYBOARD");
                mCallbacks.hide_keyboard();
            }
        });
    }

    public void closeDown() {
        Log.v(TAG, "closeDown");
        if (mBookListAdapter != null) {
            mBookListAdapter.closeDown();
        }
    }

    private void restartLoader() {
        Log.v(TAG, "restartLoader");
        final int LOADER_ID = 10;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "onCreateLoader");

        final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = mSearchText.getText().toString();

        if (searchString.length() > 0) {
            searchString = "%" + searchString + "%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString, searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(TAG, "SEARCH: onLoadFinished");
        mBookListAdapter.swapCursor(data);
        if (data.getCount() >= 1) {
            mPosition = 0; // show the first item in the list
            mBookListRecyclerView.smoothScrollToPosition(mPosition);
            // FIXED: display the first found book
            String ean = mBookListAdapter.getEan(mPosition);
            Log.v(TAG, "SEARCH: display book for ean="+ean);
            selectItem(mPosition);
        }
        else {
            Log.w(TAG, "SEARCH: RecyclerView.NO_POSITION");
            // FIXED: clear out the failed search text
            mSearchText.setText("");
            // FIXED: notify the user the search failed
            String not_found = getResources().getString(R.string.not_found);
            Toast.makeText(getActivity(), not_found, Toast.LENGTH_LONG).show();

            // also vibrate the device to signal nothing found
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(300); // Vibrate for 300 milliseconds
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset");
        mBookListAdapter.swapCursor(null);
        ((MainActivity) getActivity()).onLoaderReset();
    }

    public void selectItem(int position) {
        Log.v(TAG, "selectItem: position="+position);
        mPosition = position;
        if (mCallbacks != null) {
            String ean = mBookListAdapter.getEan(position);
            Log.v(TAG, "selectItem: ean="+ean);
            selectItem(ean);
        }
    }

    // FIXED: onAttach(Activity activity) is deprecated
    @Override
    public void onAttach(Context context) {
        Log.v(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.setTitle(R.string.books);
            mCallbacks = (ListOfBooksCallbacks) activity;
        }
        else {
            Log.w(TAG, "expected context instanceof Activity");
        }
    }

    @Override
    public void onDetach() {
        Log.v(TAG, "onDetach");
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface ListOfBooksCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        Handler getHandler();
        void onItemSelected(String ean);
        void hide_keyboard();
    }

}
