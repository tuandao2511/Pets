package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by Administrator on 7/8/2017.
 */

public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PETS = 100;
    private static final int PET_ID = 101;

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new PetDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        Log.v(LOG_TAG,"URI la " + uri);
        int match = sUriMatcher.match(uri);
        Log.v(LOG_TAG, "match la gi " + match);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                // TODO: Perform database query on pets table
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        int match = sUriMatcher.match(uri);
        Log.v(LOG_TAG, "Match la gi " + match);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues contentValues) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();


        boolean checkDate = checkDataValidation(contentValues);
        long newId = -1;
        if (checkDate) {
            newId = database.insert(PetEntry.TABLE_NAME, null, contentValues);

            if (newId == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return ContentUris.withAppendedId(uri, newId);
    }

    private boolean checkDataValidation(ContentValues contentValues) {
        String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
        String breed = contentValues.getAsString(PetEntry.COLUMN_PET_BREED);
        String weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT).toString();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Pet requires name");
        }
        if (weight.isEmpty()) {
            throw new IllegalArgumentException("Pet requires weight");
        }

        return true;
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(contentValues, selection, selectionArgs,uri);
            case PET_ID:
                selection = PetEntry._ID +"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))} ;
                return updatePet(contentValues,selection,selectionArgs,uri);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }
    }

    private int updatePet(ContentValues contentValues, String selection, String[] selectionArgs,Uri uri) {

        if (contentValues.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = contentValues.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Pet requires name");
            }
        }
        if (contentValues.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            String weight = contentValues.getAsInteger(PetEntry.COLUMN_PET_WEIGHT).toString();
            if (weight.isEmpty()) {
                throw new IllegalArgumentException("Pet requires name");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }


        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int updateRow = db.update(PetEntry.TABLE_NAME,contentValues,selection,selectionArgs);

        if (updateRow != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateRow;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return deletePet(selection,selectionArgs,uri);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deletePet(selection,selectionArgs,uri);
            default:
                throw new IllegalArgumentException("delete error" +uri);
        }
    }

    private int deletePet(String selection, String[] selectionArgs,Uri uri) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deleteRow = db.delete(PetEntry.TABLE_NAME,selection,selectionArgs);

        if(deleteRow != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteRow;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }
}
