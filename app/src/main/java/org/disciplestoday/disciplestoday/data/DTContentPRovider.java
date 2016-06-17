package org.disciplestoday.disciplestoday.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import org.disciplestoday.disciplestoday.Article;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class DTContentProvider extends ContentProvider {
    //using http://www.vogella.com/tutorials/AndroidSQLite/article.html in addition to file-> new contentprovider, etc

    private CupboardSQLiteOpenHelper mDatabaseHelper;
    private static final Object LOCK = new Object();


    // used for the UriMatcher
    private static final int ARTICLES = 10; //List screen/fragment/recyclerview
    private static final int ARTICLE = 20; //detail screen

    public DTContentProvider() {
    }
    public static final String AUTHORITY = "disciplestoday.org";

    private static final String BASE_PATH = "articles";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/articles";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/article";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ARTICLES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ARTICLE);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new CupboardSQLiteOpenHelper(getContext());
        return false;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        synchronized (LOCK) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            Class clz;
            long id = Long.getLong(uri.getLastPathSegment(), 0);
            Log.i("NJW", "in contprovvider, tryihng to insert with:" + uri.toString());
            switch (sURIMatcher.match(uri)) {
                case ARTICLE:
                case ARTICLES:
                    clz = Item.class;
                    if (id == 0) {
                        id = cupboard().withDatabase(db).put(clz, values);
                    } else {
                        id = cupboard().withDatabase(db).update(clz, values);
                    }
                    return Uri.parse(BASE_PATH  + "/" + id);
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {


        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Cursor cursor;

        switch (sURIMatcher.match(uri)) {
            case ARTICLES:  //FIXME: Make this with moduleId
            case ARTICLE:
                cursor = cupboard().withDatabase(db).query(Article.class).
                        withProjection(projection).
                        withSelection(selection, selectionArgs).
                        orderBy(sortOrder).
                        getCursor();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // the a-ha moment is realizing all you hae to do is return a cusor.

        //https://github.com/aegis123/Bettyskitchen-app/blob/master/BettysKitchen-app/src/main/java/com/bettys/kitchen/recipes/app/providers/RecipeProvider.java


        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
