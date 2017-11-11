package com.natashapetrenko.jobaggregator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JobsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jobs.db";
    private static final int DATABASE_VERSION = 1;

    public JobsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sSql = "CREATE TABLE IF NOT EXISTS " + JobsContracts.JobsEntry.TABLE_JOBS + " ("
                + JobsContracts.JobsEntry._ID + " INTEGER PRIMARY KEY,"
                + JobsContracts.JobsEntry.COLUMN_TITLE + " TEXT NOT NULL,"
                + JobsContracts.JobsEntry.COLUMN_COMPANY + " TEXT,"
                + JobsContracts.JobsEntry.COLUMN_DESCRIPTION + " TEXT,"
                + JobsContracts.JobsEntry.COLUMN_LINK + " TEXT NOT NULL,"
                + JobsContracts.JobsEntry.COLUMN_STATUS + " TEXT NOT NULL,"
                + JobsContracts.JobsEntry.COLUMN_DATE + " DATE"
                + ")";

        sqLiteDatabase.execSQL(sSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO
    }
}
