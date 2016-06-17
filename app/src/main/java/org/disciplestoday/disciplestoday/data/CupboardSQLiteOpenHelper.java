package org.disciplestoday.disciplestoday.data;

/**
 * Created by neil on 6/7/16.
 * from cupboard docs.https://bitbucket.org/littlerobots/cupboard/wiki/withDatabase
 */

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.disciplestoday.disciplestoday.Article;

public class CupboardSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "disciplestoday.db";
    private static final int DATABASE_VERSION = 2;

    static {
        // register our model
        Log.e("NJW", "registering article model the first time CupboardSqlliteOpenHelper is instantinated at all ");
        cupboard().register(Article.class);
    }

    public CupboardSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();
        // add indexes and other database tweaks
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }
}