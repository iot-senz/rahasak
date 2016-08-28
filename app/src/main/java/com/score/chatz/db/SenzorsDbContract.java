package com.score.chatz.db;

import android.provider.BaseColumns;

/**
 * Keep database table attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbContract {

    public SenzorsDbContract() {}

    /* Inner class that defines sensor table contents */
    public static abstract class Location implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_USER = "user";
    }

    /* Inner class that defines secret table */
    public static abstract class Secret implements BaseColumns {
        public static final String TABLE_NAME = "secret";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLOMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_SENDER = "sender";
        public static final String COLUMN_NAME_RECEIVER = "receiver";
        public static final String COLUMN_NAME_DELETE = "deleted";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }

    /* Inner class that defines permission control for the user
     * Add more permissions here in the future */
    public static abstract class Permission implements BaseColumns {
        public static final String TABLE_NAME = "permission";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_CAMERA = "camera";
        public static final String COLOMN_NAME_USER = "user";
    }

    /* Inner class that defines permission control for the user
     * Add more permissions here in the future */
    public static abstract class PermissionConfiguration implements BaseColumns {
        public static final String TABLE_NAME = "permission_config";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_CAMERA = "camera";
        public static final String COLOMN_NAME_USER = "user";
    }

    /* Inner class that defines the user table contents */
    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLOMN_NAME_IMAGE = "image";
    }

    /* Inner class that defines the shared_user table contents */
    public static abstract class SharedUser implements BaseColumns {
        public static final String TABLE_NAME = "shared_user";
        public static final String COLUMN_NAME_USER = "user";
        public static final String COLUMN_NAME_SENSOR = "sensor";
    }

}
