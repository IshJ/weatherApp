package org.woheller69.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.woheller69.weather.activities.SplashActivity.groundTruthValues;
import static org.woheller69.weather.activities.SplashActivity.methodStats;
import static org.woheller69.weather.activities.SplashActivity.sideChannelValues;

public class JobMainAppInsertRunnable implements Runnable {
    Context context;
    SQLiteDatabase db;
    ContentValues values;
    public static Lock insert_locker = new ReentrantLock();

    private static final String TAG = "JobInsertRunnable";
    /**
     * Constructor for this class

     * @param context:           Android activity context for opening the database
     */
    public JobMainAppInsertRunnable(Context context) {
        this.context = context;
    }

    /**
     * Method to perform the operation in a different thread (from the Runnable interface)
     */
    public void run() {
        // Start timing the entire process and open the database
        insert_locker.lock();//locked here, in case that other thread delete the ArrayList
        if(methodStats.isEmpty()){
            insert_locker.unlock();
            return;
        }
        db = context.openOrCreateDatabase("MainApp.db", Context.MODE_PRIVATE, null);
        // DB transaction for faster batch insertion of data into database
        db.beginTransaction();
        synchronized (methodStats) {
            // Ground Truth insertion
            if (!methodStats.isEmpty()) {
                values = new ContentValues();
                for (MethodStat methodStat : methodStats) {
                    values.put(SideChannelContract.Columns.METHOD_ID,
                            methodStat.getId());
                    values.put(SideChannelContract.Columns.START_COUNT,
                            methodStat.getStartCount());
                    values.put(SideChannelContract.Columns.END_COUNT,
                            methodStat.getEndCount());
                    long out = db.insert(SideChannelContract.GROUND_TRUTH_AOP, null, values);

                }
//            Log.d(TAG+" mainAppDb #4_1# ", "" + groundTruthValues.size());


            }
            methodStats.clear();
        }



        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        insert_locker.unlock();
        boolean ifcompress = Utils.checkfile(context);//get the size of db
        if(ifcompress) {//if the db is larger than limit size, compress it.
            Utils.compress(context);
        }
//        Log.d(TAG, "Time taken for DB storage (ms): " + deltaTime);
    }
}

