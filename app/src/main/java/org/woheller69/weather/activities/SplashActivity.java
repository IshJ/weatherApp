package org.woheller69.weather.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.woheller69.weather.CacheScan;
import org.woheller69.weather.R;
import org.woheller69.weather.SideChannelJob;
import org.woheller69.weather.firststart.TutorialActivity;
import org.woheller69.weather.preferences.AppPreferencesManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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


//        List<ActivityRunner> runners = new ArrayList<>();
//        views.stream().forEach(view -> runners.add(new ActivityRunner(view, pkgName)));

    static List<SequentialActivityRunner> sequentialRunners = new ArrayList<>();

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
//        try {
//            int pid = android.os.Process.myPid();
//            Log.d(TAG, "%%%% spLASH! " + pid);
//            Runtime.getRuntime().exec("taskset -p 30 " + pid);
//            String cpuBind = getCommandResult("taskset -p " + pid);
//            Log.d(TAG, "cpu core: " + cpuBind);
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }

//        Intent intent = getIntent();
//        String viewVal = intent.getStringExtra("viewVal");
//        Log.d("## ", "viewVal "+viewVal);
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));

//        getOdexBeginAddress();
        IntStream.range(0, views.size())
                .forEach(i -> sequentialRunners.add(new SequentialActivityRunner(views.get(i), pkgName, i)));
//        views.stream().forEach(view -> sequentialRunners.add(new SequentialActivityRunner(view, pkgName,sequentialRunners.size())));


        Intent begin = new Intent(this, SideChannelJob.class);
        startForegroundService(begin);


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
//        brings 210
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        runView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        long startTime = System.currentTimeMillis();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        uncommenting below will cause wrong timings
//        runView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - startTime < 500
//        ) {
//        }
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
                finish();
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
            startActivityForResult(intent, id);

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
            while (System.currentTimeMillis() - startTime < 1000
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

}
