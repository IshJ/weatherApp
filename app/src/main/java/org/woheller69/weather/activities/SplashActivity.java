package org.woheller69.weather.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
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
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
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
    static int loopCount = 5;
    static SortedSet<Integer> keys;
    static List<String> views;
    static List<ReentrantLock> viewLocks = Collections.synchronizedList(new ArrayList<>());
    public static ReentrantLock reentrantLock = new ReentrantLock();
    static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    static private Integer obj = 0;
    static boolean isOnceRun = false;
    static String shared_file_des = "";
    private static SharedPreferences sharedPreferences;
    private static Long timingCount;
    static Lock ground_truth_insert_locker = new ReentrantLock();
    static int waitVal = 100;

//        List<ActivityRunner> runners = new ArrayList<>();
//        views.stream().forEach(view -> runners.add(new ActivityRunner(view, pkgName)));

    static List<SequentialActivityRunner> sequentialRunners = new ArrayList<>();

    public static ArrayList<SideChannelValue> sideChannelValues = new ArrayList<>();
    public static ArrayList<GroundTruthValue> groundTruthValues = new ArrayList<>();

    static {
        System.loadLibrary("native-lib");


        viewMap = new HashMap<>();

//        viewMap.put(0, ".activities.RadiusSearchActivity"
//        );
//        viewMap.put(7, ".activities.ManageLocationsActivity"
//        );
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

//        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_MULTI_PROCESS);
//        sharedPreferences.edit().putString("fd", "33").commit();


//        int shared_map_ptr = setSharedMap();
//        Log.d("shared_data_shm splash", "shared_map_ptr " + shared_map_ptr);
//        try {
//
//            int pid = android.os.Process.myPid();
//            Log.d(TAG, "shared_data_shm spLASH! " + pid);
//            shared_file_des = "/proc/" + pid + "/fd/" + shared_map_ptr;
////            Uri fileUri = Uri.fromFile(new File(shared_file_des));
////            ParcelFileDescriptor inputPFD = getContentResolver().openFileDescriptor(fileUri, "r");
////            FileDescriptor fd = inputPFD.getFileDescriptor();
////            Log.d(TAG, "cpu shared_data_shm : splash 2 " + fd.toString());
//
//
////            setSharedMapChildTest(2, shared_file_des.toCharArray());
////            Runtime.getRuntime().exec("taskset -p 30 " + pid);
////            String cpuBind = getCommandResult("taskset -p " + pid);
////            Log.d(TAG, "cpu core: " + cpuBind);
//        } catch (Exception e) {
//            Log.d(TAG, "shared_data_shm " + e.toString());
//        }

//        Intent intent = getIntent();
//        String viewVal = intent.getStringExtra("viewVal");
//        Log.d("## ", "viewVal "+viewVal);
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));

//        getOdexBeginAddress();
        IntStream.range(0, views.size())
                .forEach(i -> sequentialRunners.add(new SequentialActivityRunner(views.get(i), pkgName, i)));
//        views.stream().forEach(view -> sequentialRunners.add(new SequentialActivityRunner(view, pkgName,sequentialRunners.size())));

        initializeDB();
        Intent begin = new Intent(this, SideChannelJob.class);
        startForegroundService(begin);
//        begin.putExtra("shared_map_ptr", shared_file_des);
//        try {
////            ParcelFileDescriptor fd = ParcelFileDescriptor.fromFd(shared_map_ptr);
////            Uri fileUri = Uri.fromFile(new File(shared_file_des));
////            ParcelFileDescriptor inputPFD = getContentResolver().openFileDescriptor(fileUri, "r");
////            FileDescriptor fd = inputPFD.getFileDescriptor();
//
////            Bundle bundle = new Bundle();
//////            bundle.putBinder("fd", new ObjectWrapperForBinder(fd));
////            bundle.putParcelable("fd", inputPFD);
////            begin.putExtras(bundle);
//
////            Log.d(TAG, "shared_data_shm pfd " + fd.toString());
//
////            begin.putParcelableArrayListExtra("fd", new ArrayList<>(Arrays.asList(inputPFD)));
//            startForegroundService(begin);
//        } catch (IOException e) {
//            Log.d(TAG, "shared_data_shm " + e.toString());
//        }


//        #####
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        #####
//        temp switch off
//        long count = GetTimingCount();
//        Log.d("rainviewer", "GetTimingCount "+count);
//        while(GetTimingCount()<1){
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d("rainviewer", "GetTimingCount "+count);


//        #####
//        runView();
//        ######


//        if (true) {  //First time got to TutorialActivity
//            Intent mainIntent = new Intent(SplashActivity.this, TutorialActivity.class);
////            Bundle bundle = new Bundle();
////            bundle.putString("viewVal" , viewVal==null?"View4":viewVal);
////            mainIntent.putExtras(bundle);
//            SplashActivity.this.startActivity(mainIntent);
//        } else { //otherwise directly start ForecastCityActivity
//
//            Intent mainIntent = new Intent(SplashActivity.this, ForecastCityActivity.class);
//            SplashActivity.this.startActivity(mainIntent);
//        }

//        SplashActivity.this.finish();
//        int waitVal = 5000;
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - startTime < waitVal) {
//        }
//        finish();
//        overridePendingTransition( 0, 0);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Inside onStart");
//        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_MULTI_PROCESS);
//        String savedValueInWriterProcess = sharedPreferences.getString("fd", "");
//        Log.d("shared_data_shm splash sharedpref", " sp.toString() "+savedValueInWriterProcess);

        long startTime = System.currentTimeMillis();

//        try {
//            Thread.sleep(20000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        int i = 15;
//        while (i > 0) {
//            startTime = System.currentTimeMillis();
//            while (System.currentTimeMillis() - startTime < waitVal) {
//            }
////            try {
////                Thread.sleep(100);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//            Log.d("weather:AddressScan2", "#4_0_1#");
//            recordGroundTruth("4_0_1", false);
//            int a = method0();
//            i--;
//        }


//        while (cs==null) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        brings 210

//temp switch off to run one single activity
        runView();

//temp switch on to run one single activity
//        if (!isOnceRun) {
//            Intent intent = new Intent();
//            intent.setComponent(new ComponentName(pkgName,
//                    pkgName + ".activities.RainViewerActivity"));
//            startActivity(intent);
//            isOnceRun = true;
//        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Inside onResume");
//        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_MULTI_PROCESS);
//        String savedValueInWriterProcess = sharedPreferences.getString("fd", "");
//        Log.d("shared_data_shm splash sharedpref", " sp.toString() "+savedValueInWriterProcess);
//        long startTime = System.currentTimeMillis();
//        for(int i=0;i<100;i++) {
//            sharedPreferences = getSharedPreferences("Settings", Context.MODE_MULTI_PROCESS);
//            savedValueInWriterProcess = sharedPreferences.getString("fd", "");
//            Log.d("shared_data_shm splash sharedpref", " sp.toString() "+savedValueInWriterProcess);
//        }
//        uncommenting below will cause wrong timings
//        runView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Inside onActivityResult requestCode " + requestCode + " resultCode: " + resultCode);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < waitVal
        ) {
        }
//        temp switch off to enable just one UI
        runView();
    }

    private void runView() {
//        long startTime = System.currentTimeMillis();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
////            while (System.currentTimeMillis() - startTime < 2000
////            ) {
////            }
//        reentrantLock.lock();
        currentViewId++;
        if (currentViewId == sequentialRunners.size()) {
            if (currentLoopId >= loopCount) {
                Log.d("weather:AddressScan2", "Automation_completed!");
//                finish();
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
//            long startTime = System.currentTimeMillis();
//            while (System.currentTimeMillis()-startTime<1000
//            ){}
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(pkgName,
                    pkgName + view));


//            startActivity(intent);
            startActivityIfNeeded(intent, id + 1);

//            CountDownTimer mcd = new CountDownTimer(1000, 1000) {
//                public void onTick(long millisUntilFinished) {}
//                public void onFinish() {
//                    try
//                    {
//                        finishActivity(id);
//                        Log.d(TAG, "inside mcd");
//                    }
//                    catch (Exception ex)
//                    {}
//                }
//            }.start();


//
//
////            finish();
////            Log.d("###", view);
//            Log.d("weather:AddressScan2",  "#"+inverseViewMap.get(view)+"#0#1");
//            overridePendingTransition(0, 0);
//            Intent intent = new Intent();
//            intent.setComponent(new ComponentName(pkgName,
//                    pkgName + view));
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            startActivity(intent);
        }
    }

    public String getOdexBeginAddress() {

        // get Process ID of the running app
        int pid = android.os.Process.myPid();
        Log.d(TAG, "%%%% spLASH! " + pid);

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
        return "";
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
        db.close();
    }


    protected void recordGroundTruth(String label, boolean isFinish) {
//        int pp= setSharedMap();
//        Log.d("shared_data_shm", "pp "+pp);
//        int ans = getSharedMapVal();
//        Log.d("shared_data_shm ", " rainview get "+ans);
        Log.d("sharedPref", " splash request " + timingCount);

//        timingCount = getSharedPreferences("SideChannelInfo", Context.MODE_MULTI_PROCESS)
//                .getLong("timeCount", -1l);
        timingCount = -1l;
//        Log.d("shared_data_shm splash sharedpref", " sp.toString() "+savedValueInWriterProcess);
        Log.d("sharedPref", " splash " + timingCount);
        Log.d("splash", "groundTruthValues count " + timingCount);

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

    protected int method0() {
        recordGroundTruth("4_0_0", true);
        Log.d("weather:AddressScan2", "#4_0_0#");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < waitVal
        ) {
        }
//timeCount
        int number = ThreadLocalRandom.current().nextInt(100, 1000 + 1);
        int log = 0;
        int bits = number;
        if ((bits & 0xffff0000) != 0) {
            bits >>>= 16;
            log = 16;
        }
        if (bits >= 256) {
            bits >>>= 8;
            log += 8;
        }
        if (bits >= 16) {
            bits >>>= 4;
            log += 4;
        }
        if (bits >= 4) {
            bits >>>= 2;
            log += 2;
        }
        if (1 << log < number)
            log++;
        int a = log + (bits >>> 1);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 100000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(100, 10000 + 1)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        int n = array.length;
        int result = binarySearch(array, 5, 0, n - 1);

        Log.d("weather:AddressScan2", "#4_1_1#");

        recordGroundTruth("4_1_1", false);
        method1();
        return result;
    }

    static int pow(int base, int power) {
        int result = 1;
        for (int i = 0; i < power; i++)
            result *= base;
        return result;
    }

    protected int method1() {
        recordGroundTruth("4_1_0", true);
        Log.d("weather:AddressScan2", "#4_1_0#");
//        long count = GetTimingCount();

//        Log.d("rainviewer", "groundTruthValues count " + count);
        Log.d("rainviewer", "groundTruthValues " + groundTruthValues.size());
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < waitVal
        ) {
        }
        // create an object of Main class


        // create a sorted array

        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 1000000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(100, 1000 + 1)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        int n = array.length;

        // get input from user for element to be searched

        // call the binary search method
        // pass arguments: array, element, index of first and last element
        int result = binarySearch(array, 5, 0, n - 1);
        return result;
    }

    int binarySearch(int array[], int element, int low, int high) {

        // Repeat until the pointers low and high meet each other
        while (low <= high) {

            // get index of mid element
            int mid = low + (high - low) / 2;

            // if element to be searched is the mid element
            if (array[mid] == element)
                return mid;

            // if element is less than mid element
            // search only the left side of mid
            if (array[mid] < element)
                low = mid + 1;

                // if element is greater than mid element
                // search only the right side of mid
            else
                high = mid - 1;
        }

        return -1;
    }

    public static native long GetTimingCount();

    public static native int setSharedMap();

    public native void setSharedMapChildTest(int shared_mem_ptr, char[] fileDes);


}
