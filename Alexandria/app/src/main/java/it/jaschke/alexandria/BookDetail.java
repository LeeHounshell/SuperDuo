package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "LEE: <" + BookDetail.class.getSimpleName() + ">";

    public final static String EAN_KEY = "EAN";

    private View mRootView;
    private String mEan;
    private ShareActionProvider mShareActionProvider;

    public BookDetail() {
        Log.v(TAG, "BookDetail");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");

        Bundle arguments = getArguments();
        if (arguments != null) {
            mEan = arguments.getString(BookDetail.EAN_KEY);
            if (mEan != null && mEan.length() > 0) {
                int LOADER_ID = 10;
                getLoaderManager().restartLoader(LOADER_ID, null, this);
            }
            else {
                Log.v(TAG, "empty BookDetail");
            }
        }

        mRootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "DETAIL - delete_button: CLICK!");
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEan);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEan = "";
                // FIXED: remove the the right_container
                clearRightContainer(getActivity());
                // FIXED: remove the book from display
                MainActivity.bookDeleted = true;
                getActivity().onBackPressed();
            }
        });

        // FIXED: properly handle back button in ONE place
        mRootView.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "back_button: CLICK!");
                getActivity().onBackPressed();
            }
        });
        return mRootView;
    }

    private void clearRightContainer(Activity activity) {
        Log.v(TAG, "clearRightContainer");
        if (activity.findViewById(R.id.right_container) != null) {
            int id = R.id.right_container;
            Log.v(TAG, "RIGHT CONTAINER: set right_container GONE");
            activity.findViewById(id).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.book_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "onCreateLoader");
        // FIXED: null ISBN
        if (mEan != null) {
            long book_id;
            // FIXED: handle invalid ISBN or ISBN with text
            try {
                book_id = Long.parseLong(mEan);
                return new CursorLoader(
                        getActivity(),
                        AlexandriaContract.BookEntry.buildFullBookUri(book_id),
                        null,
                        null,
                        null,
                        null
                );
            } catch (NumberFormatException e) {
                Log.e(TAG, "invalid isbn:" + mEan);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        Log.v(TAG, "onLoadFinished");
        if (!cursor.moveToFirst()) {
            return;
        }

        makeVisible();

        final String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        final String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        final String subTitle = (bookSubTitle != null && bookSubTitle.length() > 0) ? ("\n<br>\n" + bookSubTitle) : "";
        final String authors = cursor.getString(cursor.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        final String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        final String desc = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.DESC));

        Log.v(TAG, "onLoadFinished - bookTitle="+bookTitle);
        ((TextView) mRootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        if (mShareActionProvider != null) {
            if (imgUrl != null && imgUrl.length() > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "also share the cover image..");
                        final Intent shareIntent = getShareIntent(bookTitle, subTitle, authors, desc);
                        if (shareTheCoverImage(bookTitle, imgUrl, shareIntent)) {
                            shareIntent.setType("application/image");
                        }
                        ImageView image = (ImageView) mRootView.findViewById(R.id.fullBookCover);
                        image.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(TAG, "SETUP SHARE INTENT WITH COVER IMAGE");
                                mShareActionProvider.setShareIntent(shareIntent);
                            }
                        });
                    }
                }).start();
            }
            else {
                Intent shareIntent = getShareIntent(bookTitle, subTitle, authors, desc);
                mShareActionProvider.setShareIntent(shareIntent);
            }
        }
        else {
            Log.w(TAG, "*** mShareActionProvider is null! ***");
        }

        ((TextView) mRootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);
        ((TextView) mRootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String[] authorsArr = authors.split(",");
        ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        if (imgUrl != null && Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) mRootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
        }
        else {
            // FIXED: use a place holder image if the URL does not exist
            Log.w(TAG, "UNABLE TO LOCATE URL FOR BOOK DETAIL: USE PLACE HOLDER");
            Context context = AlexandriaApplication.getAppContext();
            Bitmap bookCover = BitmapFactory.decodeResource(context.getResources(), R.drawable.book_placeholder);
            ImageView image = (ImageView) mRootView.findViewById(R.id.fullBookCover);
            image.setImageBitmap(bookCover);
        }

        String categories = cursor.getString(cursor.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);
    }

    @NonNull
    private Intent getShareIntent(String bookTitle, String subTitle, String authors, String desc) {
        Log.v(TAG, "getShareIntent");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // FIXED: only set this for API 21+
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            // from: http://stackoverflow.com/questions/32941254/is-there-anything-similar-to-flag-activity-new-document-for-older-apis
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        // FIXED: share proper book info
        //shareIntent.setType("text/plain");
        shareIntent.setType("text/html");

        String share_body = "\n<body>\n<br>\n"
                + "\"" + bookTitle + "\""
                + subTitle
                + "\n<br>\n<br> by " + authors
                + "\n<br> ISBN# " + mEan
                + "\n<br>\n<br>\n" + desc
                + "\n</body>\n";

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_text));
        shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(share_body));
        return shareIntent;
    }

    private boolean shareTheCoverImage(String bookTitle, String coverUrl, Intent shareIntent) {
        Log.v(TAG, "shareTheCoverImage");
        boolean success = false;
        if (coverUrl != null && coverUrl.length() > 0 && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                // from: http://www.oliverpearmain.com/blog/android-how-to-launch-an-email-intent-attaching-a-resource-via-a-url/
                Log.v(TAG, "download the cover image..");
                URL coverImageUrl = new URL(coverUrl);
                URLConnection connection = coverImageUrl.openConnection();
                // from: http://stackoverflow.com/questions/1945201/android-image-caching
                connection.setUseCaches(true);

                Bitmap coverImageBitmap = BitmapFactory.decodeStream((InputStream)connection.getContent());
                if (coverImageBitmap != null) {
                    Log.v(TAG, "we have the cover image");

                    // Save the downloaded cover image to the pictures folder on the SD Card
                    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    if (!directory.exists()) {
                        if (!directory.mkdirs()) { // Make sure the Pictures directory exists.
                            Log.w(TAG, "unable to create directory: " + directory);
                        }
                    }
                    String attachmentFileName = bookTitle.replace(' ', '_') + ".jpg";
                    File destinationFile = new File(directory, attachmentFileName);
                    FileOutputStream out = new FileOutputStream(destinationFile);
                    coverImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                    Uri mediaStoreImageUri = Uri.fromFile(destinationFile);

                    // Add the attachment to the intent
                    shareIntent.putExtra(Intent.EXTRA_STREAM, mediaStoreImageUri);
                    success = true;
                    Log.v(TAG, "cover image loaded successfully");
                }
            }
            catch (Exception e) {
                // something went wrong
                Log.w(TAG, "unable to download cover image at url=" + coverUrl + " - e=" + e);
            }
        }
        Log.v(TAG, "shareTheCoverImage: success="+success);
        return success;
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset");
        // FIXED: see if we need to open the drawer
        ((MainActivity) getActivity()).onLoaderReset();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause");
        // FIXED: call correct super
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, "onDestroyView");
        // FIXED: implement onDestroyView()
        super.onDestroyView();
    }

    private void makeVisible() {
        Log.v(TAG, "makeVisible");
        // FIXED: issue with visibility - now defaults to INVISIBLE until needed
        if (mRootView.findViewById(R.id.delete_button) != null) {
            mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
        }
        if (mRootView.findViewById(R.id.save_button) != null) {
            mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        }
        if (mRootView.findViewById(R.id.back_button) != null) {
            mRootView.findViewById(R.id.back_button).setVisibility(View.VISIBLE);
        }
        if (mRootView.findViewById(R.id.fullBookCover) != null) {
            mRootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }
    }

}
