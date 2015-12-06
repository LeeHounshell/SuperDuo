package it.jaschke.alexandria;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import it.jaschke.alexandria.data.AlexandriaContract;

public class TestProvider extends AndroidTestCase {
    private static final String TAG = TestProvider.class.getSimpleName();

    public void setUp() {
        Log.v(TAG, "setUp");
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        Log.v(TAG, "deleteAllRecords");
        try {
            mContext.getContentResolver().delete(
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    null
            );
            mContext.getContentResolver().delete(
                    AlexandriaContract.CategoryEntry.CONTENT_URI,
                    null,
                    null
            );

            mContext.getContentResolver().delete(
                    AlexandriaContract.AuthorEntry.CONTENT_URI,
                    null,
                    null
            );

            Cursor cursor = mContext.getContentResolver().query(
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            //noinspection ConstantConditions
            assertEquals(0, cursor.getCount());
            cursor.close();

            cursor = mContext.getContentResolver().query(
                    AlexandriaContract.AuthorEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            //noinspection ConstantConditions
            assertEquals(0, cursor.getCount());
            cursor.close();

            cursor = mContext.getContentResolver().query(
                    AlexandriaContract.CategoryEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            //noinspection ConstantConditions
            assertEquals(0, cursor.getCount());
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void testGetType() {
        Log.v(TAG, "testGetType");

        String type = mContext.getContentResolver().getType(AlexandriaContract.BookEntry.CONTENT_URI);
        assertEquals(AlexandriaContract.BookEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.AuthorEntry.CONTENT_URI);
        assertEquals(AlexandriaContract.AuthorEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.CategoryEntry.CONTENT_URI);
        assertEquals(AlexandriaContract.CategoryEntry.CONTENT_TYPE, type);

        long id = 9780137903955L;
        type = mContext.getContentResolver().getType(AlexandriaContract.BookEntry.buildBookUri(id));
        assertEquals(AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.BookEntry.buildFullBookUri(id));
        assertEquals(AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.AuthorEntry.buildAuthorUri(id));
        assertEquals(AlexandriaContract.AuthorEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(AlexandriaContract.CategoryEntry.buildCategoryUri(id));
        assertEquals(AlexandriaContract.CategoryEntry.CONTENT_ITEM_TYPE, type);

    }

    public void testInsertRead() {
        Log.v(TAG, "testInsertRead");

        insertReadBook();
        insertReadAuthor();
        insertReadCategory();

        readFullBook();
        readFullList();
    }

    private void insertReadBook() {
        Log.v(TAG, "insertReadBook");
        ContentValues bookValues = TestDb.getBookValues();

        Uri bookUri = mContext.getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, bookValues);
        long bookRowId = ContentUris.parseId(bookUri);
        assertTrue(bookRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, bookValues);

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(bookRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, bookValues);

    }

    private void insertReadAuthor() {
        Log.v(TAG, "insertReadAuthor");
        ContentValues authorValues = TestDb.getAuthorValues();

        Uri authorUri = mContext.getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, authorValues);
        long authorRowId = ContentUris.parseId(authorUri);
        assertTrue(authorRowId != -1);
        assertEquals(authorRowId, TestDb.ean);

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.AuthorEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, authorValues);

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.AuthorEntry.buildAuthorUri(authorRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, authorValues);

    }

    private void insertReadCategory() {
        Log.v(TAG, "insertReadCategory");
        ContentValues categoryValues = TestDb.getCategoryValues();

        Uri categoryUri = mContext.getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, categoryValues);
        long categoryRowId = ContentUris.parseId(categoryUri);
        assertTrue(categoryRowId != -1);
        assertEquals(categoryRowId, TestDb.ean);

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.CategoryEntry.CONTENT_URI,
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, categoryValues);

        cursor = mContext.getContentResolver().query(
                AlexandriaContract.CategoryEntry.buildCategoryUri(categoryRowId),
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, categoryValues);

    }

    private void readFullBook() {
        Log.v(TAG, "readFullBook");

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.buildFullBookUri(TestDb.ean),
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, TestDb.getFullDetailValues());
    }

    private void readFullList() {
        Log.v(TAG, "readFullList");

        Cursor cursor = mContext.getContentResolver().query(
                AlexandriaContract.BookEntry.FULL_CONTENT_URI,
                null, // projection
                null, // selection
                null, // selection args
                null  // sort order
        );

        TestDb.validateCursor(cursor, TestDb.getFullListValues());
    }

}