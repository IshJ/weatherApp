package org.woheller69.weather.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;


import org.woheller69.weather.CacheScan;
import org.woheller69.weather.GroundTruthValue;
import org.woheller69.weather.JobInsertRunnable;
import org.woheller69.weather.JobMainAppInsertRunnable;
import org.woheller69.weather.MethodStat;
import org.woheller69.weather.ObjectWrapperForBinder;
import org.woheller69.weather.R;
import org.woheller69.weather.ShmClientLib;
import org.woheller69.weather.SideChannelContract;
import org.woheller69.weather.SideChannelJob;
import org.woheller69.weather.SideChannelValue;
import org.woheller69.weather.firststart.TutorialActivity;
import org.woheller69.weather.preferences.AppPreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import static org.woheller69.weather.JobInsertRunnable.insert_locker;

/**
 * Created by yonjuni on 24.10.16.
 */

public class SplashActivity extends AppCompatActivity {
    public static final String TAG = "SplashActivity";
    private static int currentViewId = 0;
    private static int currentLoopId = 0;
    public static CacheScan cs = null;
    static Map<Integer, String> viewMap = new HashMap<>();
    static Map<String, Integer> inverseViewMap = new HashMap<>();
    static final String pkgName = "org.woheller69.weather";
    static int loopCount = 15;
    static SortedSet<Integer> keys;
    static List<String> views;
    static List<ReentrantLock> viewLocks = Collections.synchronizedList(new ArrayList<>());
    static private Integer obj = 0;
    private static Long timingCount;
    static Lock ground_truth_insert_locker = new ReentrantLock();
    static int waitVal = 1000;
    Map<String, String> configMap = new HashMap<>();
    static final String CONFIG_FILE_PATH = "/data/local/tmp/config.out";
    public static Map<String, Integer> methodIdMap = new HashMap<>();

    public static int fd = -2;
    private Messenger mService;

    private Messenger replyMessenger = new Messenger(new MessengerHandler());

    static List<SequentialActivityRunner>
            sequentialRunners = new ArrayList<>();

    public static ArrayList<SideChannelValue> sideChannelValues = new ArrayList<>();
    public static ArrayList<GroundTruthValue> groundTruthValues = new ArrayList<>();
    public static final List<MethodStat> methodStats = new ArrayList<>();

    static {
        System.loadLibrary("native-lib");


        viewMap = new HashMap<>();

//        viewMap.put(0, ".activities.RadiusSearchActivity"
//        );
        viewMap.put(7, ".activities.ManageLocationsActivity"
        );
        viewMap.put(4, ".activities.RainViewerActivity"
        );
//        viewMap.put(3, ".activities.RadiusSearchResultActivity"
//        );
//        viewMap.put(5, ".activities.AboutActivity"
//        );
//        viewMap.put(6, ".activities.ForecastCityActivity"
//        );

        viewMap.keySet().forEach(k -> inverseViewMap.put(viewMap.get(k), k));
        keys = new TreeSet<>(viewMap.keySet());
        views = keys.stream().map(viewMap::get).collect(Collectors.toList());
        views.stream().forEach(i -> viewLocks.add(new ReentrantLock()));
    }

    private AppPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Inside oncreate");

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE
                            , Manifest.permission.CAMERA},
                    10);
        } else {
            setUpandRun();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    setUpandRun();
                } else {
                    finish();
                }
            }
        }
    }

    protected void setUpandRun() {

        fd = createAshMem();
        if (fd < 0) {
            Log.d("ashmem ", "not set onCreate " + fd);
        }
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));

        IntStream.range(0, views.size())
                .forEach(i -> sequentialRunners.add(new SequentialActivityRunner(views.get(i), pkgName, i)));

        copyOdex();

        configMap = readConfigFile();
//        configMap.entrySet().forEach(e -> Log.d("configMap: ", e.getKey() + " " + e.getValue()));

        loopCount = Integer.parseInt(Objects.requireNonNull(configMap.get("interLoopCount")));
        waitVal = Integer.parseInt(Objects.requireNonNull(configMap.get("delayWithinViews")));

        initializeDB();
        initializeDBAop();
        Intent begin = new Intent(this, SideChannelJob.class);
        bindService(begin, conn, Context.BIND_AUTO_CREATE);
        startForegroundService(begin);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        runView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Inside onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Inside onResume");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        Log.d(TAG, "Inside onActivityResult requestCode " + requestCode + " resultCode: " + resultCode);

        long startTime = System.currentTimeMillis();
        int waitRnd = (int) (Math.random() * waitVal);
        while (System.currentTimeMillis() - startTime < waitRnd
        ) {
        }
//        temp switch off to enable just one UI
        runView();
    }

    private void runView() {
        Log.d(TAG + "#", "currentViewId:" + currentViewId);

        currentViewId++;
        if (currentViewId == sequentialRunners.size()) {
            if (currentLoopId >= loopCount) {
                try {
//                    Debug.stopMethodTracing();
                    copyMethodMap();
                    Log.d(TAG + "#", getDatabasePath("SideScan").toString());

                    Process p = Runtime.getRuntime().exec("cp " + getDatabasePath("SideScan") + ".db /sdcard/Documents");
//                    p.waitFor();
//                    p = Runtime.getRuntime().exec("cp " + getDatabasePath("MainApp") + ".db /sdcard/Documents");
                    p = Runtime.getRuntime().exec("cp " + getDatabasePath("MainApp") + ".db /sdcard/Documents");
//                    p.waitFor();

                } catch (Exception e) {
                    Log.d(TAG + "#", e.toString());
                }
                Log.d("weather:AddressScan2", "Automation_completed!");
                return;
            }
            currentLoopId++;
            currentViewId = 0;
        }
        sequentialRunners.get(currentViewId).run();

    }

    class SequentialActivityRunner {
        private final String view;
        private final String pkgName;
        private final int id;

        public SequentialActivityRunner(String view, String pkgName, int id) {
            this.view = view;
            this.pkgName = pkgName;
            this.id = id;
        }

        public void run() {

            Log.d("weather:AddressScan2", "#" + inverseViewMap.get(view) + "_1#");
            Intent intent = new Intent();
            configMap.entrySet().forEach(e -> intent.putExtra(e.getKey(), e.getValue()));
            intent.setComponent(new ComponentName(pkgName,
                    pkgName + view));

            startActivityIfNeeded(intent, id + 1);


        }
    }


    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d("ashmem", "Received information from the server: " + msg.getData().getString("reply"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Message msg = Message.obtain(null, 0);
            Bundle bundle = new Bundle();
            if (fd < 0) {
                Log.d("ashmem ", "not set onServiceConnected " + fd);
            }
            setAshMemVal(fd, 4l);
            try {
                ParcelFileDescriptor desc = ParcelFileDescriptor.fromFd(fd);
                bundle.putParcelable("msg", desc);
                msg.setData(bundle);
                msg.replyTo = replyMessenger;      // 2
                mService.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    private Map<String, String> readConfigFile() {
        Map<String, String> configMap = new HashMap<>();
        try {
            List<String> configs = Files.lines(Paths.get(CONFIG_FILE_PATH)).collect(Collectors.toList());
            configs.stream().filter(c -> !c.contains("//") && c.contains(":")).forEach(c -> configMap.put(c.split(":")[0].trim(), c.split(":")[1].trim()));

        } catch (IOException e) {
            Log.d(TAG + "#", e.toString());
        }
        return configMap;
    }

    private void copyOdex() {
        try {

            String oatHome = "/sdcard/Documents/oatFolder/oat/arm64/";
            Optional<String> baseOdexLine = Files.lines(Paths.get("/proc/self/maps")).collect(Collectors.toList())
                    .stream().sequential().filter(s -> s.contains("woheller69") && s.contains("base.odex"))
                    .findAny();
            if (baseOdexLine.isPresent()) {
                String odexpath = "/data/app/" + baseOdexLine.get().split("/data/app/")[1];
                String vdexpath = "/data/app/" + baseOdexLine.get().split("/data/app/")[1].replace("odex", "vdex");
//                String odexRootPath = "/data/app/"+baseOdexLine.get().split("/data/app/")[1].replace("/oat/arm64/base.odex","*");
                Log.d(TAG + "#", odexpath);
                Log.d(TAG + "#", "cp " + odexpath + " " + oatHome);
                Process p = Runtime.getRuntime().exec("cp " + odexpath + " " + oatHome);
                p.waitFor();
                p = Runtime.getRuntime().exec("cp " + vdexpath + " " + oatHome);
                Log.d(TAG + "#", "cp " + vdexpath + " " + oatHome);

                p.waitFor();
                Log.d(TAG + "#", "odex copied");

            } else {
                Log.d(TAG + "#", "base odex absent");
            }

        } catch (IOException | InterruptedException e) {
            Log.d(TAG + "#", e.toString());
        }
    }

    private void copyMethodMap() {
        String methodMapString = methodIdMap.entrySet().parallelStream().map(Object::toString).collect(Collectors.joining("|"));
        Log.d("MethodMap", methodMapString);
        Log.d("MethodMapCount", String.valueOf(methodIdMap.size()));

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

//    public static void parallelRun(List<ActivityRunner> runners) {
//        for (int j = 0; j < runners.size(); j++) {
//            Handler handler = new Handler();
//            handler.postDelayed(runners.get(j), 5000);
//        }
//    }

    public static void sequentialRun(List<SequentialActivityRunner> runners) {
        for (int j = 0; j < runners.size(); j++) {
            runners.get(j).run();
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < waitVal
            ) {
            }
        }
    }


//    class ActivityRunner implements Runnable {
//        private final String view;
//        private final String pkgName;
//
//        public ActivityRunner(String view, String pkgName) {
//            this.view = view;
//            this.pkgName = pkgName;
//        }
//
//        public void run() {
//            finish();
//            Log.d("###", view);
//            overridePendingTransition(0, 0);
//            Intent intent = new Intent();
//            intent.setComponent(new ComponentName(pkgName,
//                    pkgName + view));
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//        }
//    }


    protected void automate() {


        for (int i = 0; i < loopCount; i++) {

//            parallelRun(runners);
            sequentialRun(sequentialRunners);
        }
        Log.d("###", "Done!");
    }

    /**
     * Method to initialize database
     */
    void initializeDB() {
        // Creating the database file in the app sandbox
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MainApp.db",
                MODE_PRIVATE, null);
        Locale locale = new Locale("EN", "SG");
        db.setLocale(locale);
        // Creating the schema of the database
        String sSQL = "CREATE TABLE IF NOT EXISTS " + SideChannelContract.GROUND_TRUTH + " (" +
                SideChannelContract.Columns.SYSTEM_TIME + " INTEGER NOT NULL, " +
                SideChannelContract.Columns.LABEL + " TEXT, " +
                SideChannelContract.Columns.COUNT + " INTEGER);";
        db.execSQL(sSQL);
        sSQL = "DELETE FROM " + SideChannelContract.GROUND_TRUTH;
        db.execSQL(sSQL);
        db.close();
    }

    void initializeDBAop() {
        // Creating the database file in the app sandbox
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MainApp.db",
                MODE_PRIVATE, null);
        Locale locale = new Locale("EN", "SG");
        db.setLocale(locale);
        // Creating the schema of the database
        String sSQL = "CREATE TABLE IF NOT EXISTS " + SideChannelContract.GROUND_TRUTH_AOP + " (" +
                SideChannelContract.Columns.METHOD_ID + " INTEGER NOT NULL, " +
                SideChannelContract.Columns.START_COUNT + " INTEGER, " +
                SideChannelContract.Columns.END_COUNT + " INTEGER);";
        db.execSQL(sSQL);
        sSQL = "DELETE FROM " + SideChannelContract.GROUND_TRUTH_AOP;
        db.execSQL(sSQL);
        db.close();
    }


    protected void recordGroundTruth(String label, boolean isFinish) {

//        uncomment to get the timing count. Might lag the app if the scanning frequency is high
//        timingCount = getSharedPreferences("SideChannelInfo", Context.MODE_MULTI_PROCESS)
//                .getLong("timeCount", -1l);
        timingCount = -1l;

        GroundTruthValue groundTruthValue = new GroundTruthValue();
        groundTruthValue.setLabel(label);
        groundTruthValue.setSystemTime(System.currentTimeMillis());
        groundTruthValue.setCount(timingCount);
        insert_locker.lock();
        groundTruthValues.add(groundTruthValue);
        insert_locker.unlock();
        if (groundTruthValues.size() > 0 && isFinish) {
            new Thread(new JobMainAppInsertRunnable(getBaseContext())).start();
        }
    }


    public static native long GetTimingCount();

    public static native int setSharedMap();

    public native void setSharedMapChildTest(int shared_mem_ptr, char[] fileDes);

    public native int createAshMem();

    public static native long readAshMem(int fd);

    public static native void setAshMemVal(int fd, long val);

}
