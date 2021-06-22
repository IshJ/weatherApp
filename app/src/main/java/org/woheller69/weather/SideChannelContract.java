package org.woheller69.weather;


import android.provider.BaseColumns;

public class SideChannelContract {
    public static final String TABLE_NAME = "Side_Channel_Info";
    public static final String GROUND_TRUTH = "Ground_Truth";

    /**
     * Static class to return column names for the database
     */
    public static class Columns {
        //public static final String _ID = BaseColumns._ID;
        public static final String TIMESTAMP = "Timestamp";
        public static final String SYSTEM_TIME = "System_Time";
        public static final String SCAN_TIME = "Scan_Time";
        public static final String ADDRESS = "Address";
        public static final String COUNT = "Count";
        public static final String LABEL = "Label";
         private Columns() {}
        // private constructor to prevent instantiation
    }

    public static String[] CLASSES = new String[]{
            "QueryInformation",
            "Camera",
            //"Calendar",
            //"ReadSMS",
            "RequestLocation",
            "AudioRecording",
            //"ReadContacts"
    };
}
