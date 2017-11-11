package com.natashapetrenko.jobaggregator.data;

import android.provider.BaseColumns;

public class JobsContracts {

    public static final class JobsEntry implements BaseColumns {

        public static final String TABLE_JOBS = "jobs";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_COMPANY = "company";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_STATUS = "status";

    }
}
