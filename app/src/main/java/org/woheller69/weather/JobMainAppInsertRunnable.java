package org.woheller69.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.woheller69.weather.activities.SplashActivity.groundTruthValues;
import static org.woheller69.weather.activities.SplashActivity.sideChannelValues;

public class JobMainAppInsertRunnable implements Runnable {
    Context context;
    SQLiteDatabase db;
    ContentValues values;
    long startTime;
    public static Lock insert_locker = new ReentrantLock();

    private static final String TAG = "JobInsertRunnable";
    /**
     * Constructor for this class

     * @param context:           Android activity context for opening the database
     */
    public JobMainAppInsertRunnable(Context context) {
        this.context = context;
        /*
        this.groundTruthValues = groundTruthValues;
        this.userFeedbacks = userFeedbacks;
        this.compilerValues = compilerValues;
        this.frontAppValues = frontAppValues;

         */
    }

    /**
     * Method to perform the operation in a different thread (from the Runnable interface)
     */
    public void run() {
        // Start timing the entire process and open the database
        insert_locker.lock();//locked here, in case that other thread delete the ArrayList
        if(sideChannelValues.isEmpty() && groundTruthValues.isEmpty()){
            insert_locker.unlock();
            return;
        }
        startTime = System.currentTimeMillis();
        db = context.openOrCreateDatabase("MainApp.db", Context.MODE_PRIVATE, null);
        // DB transaction for faster batch insertion of data into database
        db.beginTransaction();
//        Log.d("sidescandb", "sideChannelValues "+sideChannelValues.size()+ " groundTruthValues "+groundTruthValues.size() );
//        if(!sideChannelValues.isEmpty()){
//            Log.d("sidescandb", "sideChannelValues[0] "+sideChannelValues.get(0).getCount()+ " | "+ sideChannelValues.get(0).getAddress());
//        }
//        if(!groundTruthValues.isEmpty()){
//            Log.d("sidescandb", "groundTruthValues[0] "+groundTruthValues.get(0).getCount());
//        }

//        Log.d("sidescandb", "groundTruthValues "+groundTruthValues.size());

        // Ground Truth insertion
        if(groundTruthValues!=null&&groundTruthValues.size()!=0) {
            values = new ContentValues();
            for (GroundTruthValue groundTruthValue : groundTruthValues) {
                values.put(SideChannelContract.Columns.SYSTEM_TIME,
                        groundTruthValue.getSystemTime());
                values.put(SideChannelContract.Columns.COUNT,
                        groundTruthValue.getCount());
                values.put(SideChannelContract.Columns.LABEL,
                        groundTruthValue.getLabel());
                long out =  db.insert(SideChannelContract.GROUND_TRUTH, null, values);

            }
//            Log.d(TAG+" mainAppDb #4_1# ", "" + groundTruthValues.size());

            groundTruthValues = new ArrayList<>();

        }


        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        long deltaTime = System.currentTimeMillis() - startTime;
        insert_locker.unlock();
        boolean ifcompress = Utils.checkfile(context);//get the size of db
        if(ifcompress) {//if the db is larger than limit size, compress it.
            Utils.compress(context);
        }
//        Log.d(TAG, "Time taken for DB storage (ms): " + deltaTime);
    }
}

