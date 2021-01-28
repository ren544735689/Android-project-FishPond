package com.example.android_forest_app.db;

import android.provider.BaseColumns;

public final class TodoContract {
    //新建表
    public static final String SQL_CREATE_NOTES =
            "CREATE TABLE " + TodoNote.TABLE_NAME
                    + "(" + TodoNote.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TodoNote.COLUMN_TIME + " TEXT, "
                    + TodoNote.COLUMN_DEADLINE + " TEXT, "
                    + TodoNote.COLUMN_STATE + " TEXT, "
                    + TodoNote.COLUMN_SCHEDULED + " TEXT, "
                    + TodoNote.COLUMN_CAPTION + " TEXT)";

    private TodoContract() {
    }
    //T odoNote的列名对应
    public static class TodoNote implements BaseColumns {
        public static final String TABLE_NAME = "fish";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DEADLINE = "deadline";
        public static final String COLUMN_SCHEDULED = "scheduled";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_CAPTION = "caption";
    }

}