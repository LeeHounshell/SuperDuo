package it.jaschke.alexandria.api;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.jaschke.alexandria.ListOfBooksFragment;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.DownloadImage;


public class BookListAdapter extends RecyclerView.Adapter<BookViewHolder> {
    private final static String TAG = "LEE: <" + BookListAdapter.class.getSimpleName() + ">";

    private Cursor mCursor;
    private ListOfBooksFragment listOfBooksFragment;

    public BookListAdapter(Cursor cursor) {
        super();
        mCursor = cursor;
        Log.v(TAG, "BookListAdapter");
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(TAG, "onCreateViewHolder");
        int layoutId = R.layout.book_list_item;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        view.setFocusable(true);
        return new BookViewHolder(view, viewType, this);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Log.v(TAG, "onBindViewHolder - position="+position);

        if (getCursor() != null) {
            if (getCursor().moveToPosition(position)) {
                // leave cursor open, as it is reused on next click
                int bookUrlId = getCursor().getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL);
                String imgUrl = getCursor().getString(bookUrlId);
                Log.v(TAG, "bookUrlId=" + bookUrlId + ", imgUrl=" + imgUrl);
                new DownloadImage(holder.mBookCover).execute(imgUrl);

                String bookTitle = getCursor().getString(getCursor().getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                Log.v(TAG, "bookTitle=" + bookTitle);
                holder.mBookTitle.setText(bookTitle);

                String bookSubTitle = getCursor().getString(getCursor().getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
                Log.v(TAG, "bookSubTitle=" + bookSubTitle);
                holder.mBookSubTitle.setText(bookSubTitle);
            }
            else {
                Log.e(TAG, "*** UNABLE TO POSITION CURSOR TO ROW: "+position+" ***");
            }
        } else {
            Log.e(TAG, "*** THE CURSOR IS NULL ***");
        }
    }

    @Override
    public int getItemCount() {
        //Log.v(TAG, "getItemCount");
        if (null == getCursor()) {
            return 0;
        }
        return getCursor().getCount();
    }

    public void swapCursor(Cursor newCursor) {
        Log.v(TAG, "---> swapCursor");
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    private Cursor getCursor() {
        //Log.v(TAG, "getCursor");
        return mCursor;
    }

    public ListOfBooksFragment getListOfBooksFragment() {
        Log.v(TAG, "getListOfBooksFragment");
        return listOfBooksFragment;
    }

    public void setListOfBooksFragment(ListOfBooksFragment listOfBooksFragment) {
        Log.v(TAG, "setListOfBooksFragment");
        this.listOfBooksFragment = listOfBooksFragment;
    }

    public String getEan(int position) {
        String ean = null;
        if (getCursor() != null) {
            if (getCursor().moveToPosition(position)) {
                int eanIndex = getCursor().getColumnIndex(AlexandriaContract.BookEntry._ID);
                ean = getCursor().getString(eanIndex);

                String bookTitle = getCursor().getString(getCursor().getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                Log.v(TAG, "===> bookTitle=" + bookTitle);
            }
        }
        Log.v(TAG, "---> getEan: position="+position+", ean=" + ean);
        return ean;
    }

    public void closeDown() {
        Log.v(TAG, "closeDown");
        if (getCursor() != null) {
            // FIXED: close the cursor
            getCursor().close();
        }
        swapCursor(null);
    }

}
