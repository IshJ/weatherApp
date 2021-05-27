/*
This is a foreground service for scanning, side channel information collection and notificaiton poping. 
*/
package org.woheller69.weather;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
//import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.woheller69.weather.activities.SplashActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

//import static org.woheller69.weather.CacheScan.answered;
//import static org.woheller69.weather.CacheScan.notification;
//import static org.woheller69.weather.JobInsertRunnable.insert_locker;
import static org.woheller69.weather.activities.SplashActivity.cs;

public class SideChannelJob extends Service {
    public static volatile boolean continueRun;
    private static final String TAG = "AddressScan";
    private static int label = 0;
    static int[] pattern_filter = null;
    int scValueCount = 1;
    int index = 1;
    long cumulativeTime;
    private String currentFront = "DevSec";
    private static final String CHANNEL_ID = "e.smu.devsec";
    private static final String description = "Collecting Data";
    static Lock locker = new ReentrantLock();
    long start_time = 0;
    Thread thread_collect = null;
    Thread thread_notify = null;

    /**
     * Start service by notification
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void setForegroundService() {
        int importance = NotificationManager.IMPORTANCE_LOW;
        //build channel
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
        Intent intent;
        channel.setDescription(description);
        intent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("DevSec")
                .setContentText("Scanning")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingIntent);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        startForeground(111, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Job started");
        start_time = System.currentTimeMillis();
        doBackgroundWork();
        Toast.makeText(this, "Job scheduled successfully", Toast.LENGTH_SHORT)
                .show();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Method to run data collection in the background
     */
    public void doBackgroundWork() {
        Log.d(TAG, "New Thread Created");

        // Send the job to a different thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                BigInteger odexMemMapBegin;
                BigInteger odexMemMapBegin2;
                try {
                    if (cs == null) {
                        cs = new CacheScan(getBaseContext());//Initialize the CacheScan class
                        Log.d(TAG, "Initialize CacheScan");

////                        exec memory
                                BigInteger odexMemExBegin = new BigInteger(getOdexBeginExAddress(), 16);
                                Log.d("OdexScan::", "exec memory: "+ odexMemExBegin.toString(16) );
////                        exec offset
                                BigInteger execOffset = new BigInteger("11a000", 16);
                                Log.d("OdexScan::", "exec offset: "+ execOffset.toString(16) );

//####
//######
                            // exec memory-exec offset+4
                                    odexMemExBegin = odexMemExBegin.subtract(execOffset).add(new BigInteger("4", 10));

                        odexMemMapBegin = new BigInteger(getOdexBeginAddress(), 16);

                        Log.d(TAG, "Odex starting Address: " + odexMemMapBegin.toString(16));
                        Log.d(TAG, "Odex x starting Address: " + odexMemExBegin.toString(16));

                        String[] offsets = getMethodOffsets();
//don't change
                        String odexOffsets = "7b1730,7b2830,7b3c90,7b8ed0,7b9220,7bc890,7bd050,7be940,7c1160,7c14c0";
                        Log.d(TAG, "odex offsets:"+ odexOffsets);

                        Log.d(TAG, "scanned offsets:"+ String.join(",", offsets));
                        long[] intOffsets = new long[offsets.length];

                        for (int i = 0; i < offsets.length; i++) {
//                          (exec memory-exec offset)+code offset
                            offsets[i] = odexMemExBegin.add(new BigInteger(offsets[i], 16)).toString(10);
                            intOffsets[i] = Long.parseLong(offsets[i]);

                        }

                        while (true) {
                            scan4(intOffsets, intOffsets.length);
//                            scanOdex(intOffsets, intOffsets.length);
//                            Thread.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Start Scaning
                if (pattern_filter == null)
                    pattern_filter = Utils.getArray(getBaseContext(), "ptfilter");//Retrieve an array of pattern filter; since we only need to monitor partial functions, others will get blocked
            }
        }).start();


    }

    public String getOdexBeginAddress() {

        // get Process ID of the running app
        int pid = android.os.Process.myPid();

        Log.d(TAG, "%%%% spLASH! getOdexBeginAddress" + pid);

        try {

            Log.d(TAG, "%%%% spLASH! grep woheller69 /proc/self/maps | grep odex");
            Optional<String> odc = Files.lines(Paths.get("/proc/self/maps")).collect(Collectors.toList())
                    .stream().sequential().filter(s -> s.contains("woheller69") && s.contains("base.odex"))
                    .findFirst().map(s -> new StringTokenizer(s, "-")).filter(StringTokenizer::hasMoreElements)
                    .map(StringTokenizer::nextToken);
            Log.d(TAG, "%%%% spLASH! odc " + odc);
            if (odc.isPresent()) {
                return odc.get();
            }
        } catch (Exception e) {
            Log.d(TAG, "ERROR!!!!" + e.toString());
        }
        return "77a6425000";
    }

    public String
    getOdexBeginExAddress() {

        // get Process ID of the running app
        int pid = android.os.Process.myPid();
        Log.d(TAG, "%%%% spLASH! pid " + pid);

        try {
            Log.d(TAG, "%%%% spLASH! grep woheller69 /proc/self/maps | grep odex");

            List<String> lines = Files.lines(Paths.get("/proc/self/maps")).collect(Collectors.toList());
            for (String line : lines) {
                if (line.contains("odex")) {
                    Log.d("OdexScan\n ", line);
                }
            }

            Optional<String> odc = Files.lines(Paths.get("/proc/self/maps")).collect(Collectors.toList())
                    .stream().sequential().filter(s -> s.contains("woheller69") && s.contains("base.odex") && s.contains("r-xp"))
                    .findFirst().map(s -> new StringTokenizer(s, "-")).filter(StringTokenizer::hasMoreElements)
                    .map(StringTokenizer::nextToken);
            Log.d(TAG, "%%%% spLASH! odc " + odc);
            if (odc.isPresent()) {
                return odc.get();
            }
        } catch (Exception e) {
            Log.d(TAG, "ERROR!!!!" + e.toString());
        }
        return "77a6425000";
    }

    public String[] getMethodOffsets() {

        //SideChannelOffsets
        return new String[]{"000000","7b8ed0","7b9220","7bc890","7bd050","7be940","7c1160"};
//        return new String[]{"000000", "000000"};
    }

    public String[] getClassOffsets() {
//       return new ArrayList<String>(Collections.nCopies(11, "000000")).toArray(new String[10]);

        return new String[]{"000000", "00010d78", "00010d68", "00010d70", "00010c6c", "00010d7c", "00010c04", "00010c44", "00010c18", "00010c60", "00010d64"};
    }

    public native void scan(int[] pattern, int length);

    public native void scan1(int[] pattern, int length);

    public native void scan3(String[] stringArray, int length);

    public native void scan4(long[] arr, int length);

    public native void scanOdex(long[] arr, int length);

    public native void pause();

    /**
     * Method to compute battery level based on API call to BatteryManager
     *
     * @param context: Android activity context for detecting change in battery charge
     * @return
     */
    private float computeBatteryLevel(Context context) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float) scale;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            int pid = android.os.Process.myPid();
            Log.d(TAG, "%%%% spLASH! " + pid);
            Runtime.getRuntime().exec("taskset -p f "+pid);
            String cpuBind = getCommandResult("taskset -p " + pid);
            Log.d(TAG, "cpu core: " + cpuBind);

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        Log.d("foreground", "onCreate");
        //如果API在26以上即版本为O则调用startForefround()方法启动服务
        setForegroundService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        final long minutes = (System.currentTimeMillis()-start_time)/(1000*60);
//        pause();//pause the scanning
//        //locker.lock();
//        if(stage==0) {//not in trial mode
//            if(minutes>0) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String r = TimerManager.getInstance().uploadTimeCheck(getBaseContext(), minutes);//
//                        if (r != null && r.equals("1"))
//                            Log.d(TAG, "Running time are uploaded successfully");
//                        else
//                            Log.d(TAG, "Unable to upload the running time");
//                    }
//                }).start();
//            }
//        }
//        //locker.unlock();
//        SharedPreferences edit = getBaseContext().getSharedPreferences("user",0);
//        SharedPreferences.Editor editor = edit.edit();
//        editor.putLong("Answered",answered);//record the numbr of notification
//        editor.putLong("Notification",notification);
//        editor.putLong("lastday",lastday);
//        editor.putLong("day",day);
//        editor.commit();
//        continueRun = false;
//        //make sure threads are stopped
//        if(thread_collect!=null) {
//            try {
//                thread_collect.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            thread_collect = null;
//        }
//        if(thread_notify!=null) {
//            try {
//                thread_notify.interrupt();
//                thread_notify.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            thread_notify = null;
//        }
//        Toast.makeText(this, "Job cancelled", Toast.LENGTH_SHORT)
//                .show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static String getCommandResult(String command) {
        StringBuilder log = new StringBuilder();

        try {
            // Run the command
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            // Grab the results
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log.toString();
    }
}


//
////                        ####
////                        base memory
//                        odexMemMapBegin2 = new BigInteger(getOdexBeginAddress(), 16);
//                                Log.d("OdexScan::", "base memory: "+ odexMemMapBegin2.toString(16) );
////                        exec memory
//                                BigInteger odexMemExBegin = new BigInteger(getOdexBeginExAddress(), 16);
//                                Log.d("OdexScan::", "exec memory: "+ odexMemExBegin.toString(16) );
//
////                        exec offset
//                                BigInteger execOffset = new BigInteger("11a000", 16);
//                                Log.d("OdexScan::", "exec offset: "+ execOffset.toString(16) );
//
////                        code offset
//                                BigInteger codeOffset = new BigInteger("11a010", 16);
//                                Log.d("OdexScan::", "code offset: "+ codeOffset.toString(16) );
//
////                        exec memory+exec offset
//                                BigInteger odexMemEx2Begin = odexMemExBegin.add(execOffset);
//                                Log.d("OdexScan::", "exec memory+exec offset: "+ odexMemEx2Begin.toString(16) );
//
////                        exec memory+code offset
//                                BigInteger odexMemEx3Begin = odexMemExBegin.add(codeOffset);
//                                Log.d("OdexScan::", "exec memory+code offset: "+ odexMemEx3Begin.toString(16) );
//
////                        exec memory+exec offset+code offset
//                                BigInteger odexMemEx4Begin = odexMemExBegin.add(execOffset).add(codeOffset);
//                                Log.d("OdexScan::", "exec memory+exec offset+code offset: "+ odexMemEx4Begin.toString(16) );
//
////                        base memory+exec offset+code offset
//                                BigInteger odexMemEx5Begin = odexMemMapBegin2.add(execOffset).add(codeOffset);
//                                Log.d("OdexScan::", "base memory+exec offset+code offsety: "+ odexMemEx5Begin.toString(16) );
//
////                        base memory+code offset
//                                BigInteger odexMemEx6Begin = odexMemMapBegin2.add(codeOffset);
//                                Log.d("OdexScan::", "base memory+code offset: "+ odexMemEx6Begin.toString(16) );
//
////                        exec memory+code offset - exec offset
//                                BigInteger odexMemEx7Begin = odexMemExBegin.add(codeOffset).subtract(execOffset);
//                                Log.d("OdexScan::", "exec memory+code offset - exec offset: "+ odexMemEx7Begin.toString(16) );
//
//
////                        BigInteger odexMemEx7Begin = odexMemExBegin.add(new BigInteger("11a000", 16));
//
//                                List<BigInteger> toBeScanned = Arrays.asList(
//        odexMemMapBegin2,
////                                odexMemExBegin,
////                                execOffset,
////                                codeOffset,
//        odexMemEx2Begin,
//        odexMemEx3Begin,
//        odexMemEx4Begin,
//        odexMemEx5Begin,
//        odexMemEx6Begin,
//        odexMemEx7Begin
//        );
//
//        List<String> toBeScannedLong = toBeScanned.stream().map(s -> s.toString(10)).collect(Collectors.toList());
//        long[] longOffsets = new long[toBeScanned.size()];
//        for (int i = 0; i < toBeScanned.size(); i++) {
//        longOffsets[i] = Long.valueOf(toBeScannedLong.get(i));
//        }
////                        while (true) {
////                            scanOdex(longOffsets, longOffsets.length);
////                            Thread.sleep(500);
////                        }

//    scanOdex(longOffsets, longOffsets.length);
//                        while (true) {
//
//                                Thread.sleep(500);
//                                }
