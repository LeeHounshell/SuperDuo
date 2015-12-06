package it.jaschke.alexandria.api;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.R;

public class BookViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener
{
    private final static String TAG = "LEE: <" + BookViewHolder.class.getSimpleName() + ">";

    private final BookListAdapter bookListAdapter;

    public final ImageView mBookCover;
    public final TextView mBookTitle;
    public final TextView mBookSubTitle;

    @SuppressWarnings("unused")
    public BookViewHolder(View itemView, int viewType, BookListAdapter bookListAdapter) {
        super(itemView);
        Log.v(TAG, "BookViewHolder");
        this.bookListAdapter = bookListAdapter;
        mBookCover = (ImageView) itemView.findViewById(R.id.fullBookCover);
        mBookTitle = (TextView) itemView.findViewById(R.id.listBookTitle);
        mBookSubTitle = (TextView) itemView.findViewById(R.id.listBookSubTitle);
        itemView.setClickable(true);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        Log.v(TAG, "ListOfBooks: CLICK! - position="+position);
        bookListAdapter.getListOfBooksFragment().selectItem(position);
    }

}
