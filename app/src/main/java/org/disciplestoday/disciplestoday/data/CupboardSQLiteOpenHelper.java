package org.disciplestoday.disciplestoday.data;

/**
 * Created by neil on 6/7/16.
 * from cupboard docs.https://bitbucket.org/littlerobots/cupboard/wiki/withDatabase
 */

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.disciplestoday.disciplestoday.Article;
import org.disciplestoday.disciplestoday.provider.FeedContract;

import nl.qbusict.cupboard.convert.EntityConverter;

public class CupboardSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "disciplestoday.db";
    private static final int DATABASE_VERSION = 1;

    static {
        // register our model
        cupboard().register(Article.class);
    }

    public CupboardSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TYPE_TEXT = " TEXT";
    private static final String COMMA_SEP = ",";
    /** SQL statement to create "entry" table. */

    private static final String SQL_CREATE_articles =
            "CREATE TABLE " + FeedContract.Entry.TABLE_NAME + " (" +
                    FeedContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    FeedContract.Entry.COLUMN_NAME_ARTICLE_ID    + TYPE_TEXT  + " NOT NULL"+ COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_MODULE_ID    + TYPE_TEXT + COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_IMAGE_LINK    + TYPE_TEXT + COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_FULL_TEXT    + TYPE_TEXT + COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_AUTHOR    + TYPE_TEXT + COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_SUMMARY    + TYPE_TEXT + COMMA_SEP +
                    FeedContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT +
                    ", UNIQUE ("  + FeedContract.Entry.COLUMN_NAME_ARTICLE_ID + "," + FeedContract.Entry.COLUMN_NAME_MODULE_ID +"))";

    //TODO: Category_id field for a menu option for 'hope'
    // see http://stackoverflow.com/questions/2701877/sqlite-table-constraint-unique-on-multiple-columns
    // so that you can have duplicates within each feed, eg article with multiple moduleIds.

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        //using my own create statement to get the unique constraint on article_id
        db.execSQL(SQL_CREATE_articles);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }
}