package org.woheller69.weather.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import org.woheller69.weather.APP;
import org.woheller69.weather.CacheScan;
import org.woheller69.weather.GroundTruthValue;
//import org.woheller69.weather.ISharedMem;
import org.woheller69.weather.JobInsertRunnable;
import org.woheller69.weather.JobMainAppInsertRunnable;
import org.woheller69.weather.ShmClientLib;
import org.woheller69.weather.preferences.AppPreferencesManager;

import org.woheller69.weather.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static android.system.OsConstants.O_RDONLY;
import static android.system.OsConstants.O_RDWR;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.log;
import static java.lang.Math.toIntExact;
import static org.woheller69.weather.JobInsertRunnable.insert_locker;
import static org.woheller69.weather.activities.SplashActivity.cs;
import static org.woheller69.weather.activities.SplashActivity.groundTruthValues;
import static org.woheller69.weather.activities.SplashActivity.ground_truth_insert_locker;
import static org.woheller69.weather.activities.SplashActivity.sideChannelValues;


public class RainViewerActivity extends AppCompatActivity {

//    ISharedMem ShmMemService;
    private WebView webView;
    private ImageButton btnPrev, btnNext, btnStartStop;
    public static final String TAG = "RainViewerActivity";
    private static SharedPreferences sharedPreferences;
    private static Long timingCount;
    static int waitVal = 1000;


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Inside onPause");

        webView.destroy();   //clear webView memory

        finish();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "Inside onCreate");
        int waitVal = 100;
        long startTime = System.currentTimeMillis();
//        bindService(new Intent("com.example.myapplication.ShmService").setPackage("com.example.myapplication.weather")
//                , this, BIND_AUTO_CREATE);
//        ShmClientLib.setVal(5, 10);
//        Log.d("weather:AddressScan2", "#4_0#");
        int i = 15;
        while (i > 0) {
            startTime = System.currentTimeMillis();
//            while (System.currentTimeMillis() - startTime < waitVal) {
//            }
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Log.d("weather:AddressScan2", "#4_0_1#");
            recordGroundTruth("4_0_1", false);
int a=0;
//            a = method0();
            recordGroundTruth("4_1_1", false);
            method1(a);
            i--;
        }
//        temp switch off to allow method looping
//        Log.d("weather:AddressScan2", "#4_0_1#");
//        int a = method0(3);
        setContentView(R.layout.activity_rain_viewer);

        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        String API_KEY = prefManager.getOWMApiKey(getApplicationContext());
        float latitude = getIntent().getFloatExtra("latitude", -1);
        float longitude = getIntent().getFloatExtra("longitude", -1);

        int nightmode = 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getBoolean("pref_DarkMode", false) == TRUE) {
            int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) nightmode = 1;
        }

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/rainviewer.html?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&nightmode=" + nightmode);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {  //register buttons when loading of page finished
                super.onPageFinished(webView, url);
                btnNext = findViewById(R.id.rainviewer_next);
                btnPrev = findViewById(R.id.rainviewer_prev);
                btnStartStop = findViewById(R.id.rainviewer_startstop);

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:stop();showFrame(animationPosition + 1);");
                    }
                });

                btnPrev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:stop();showFrame(animationPosition - 1);");
                    }
                });

                btnStartStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:playStop();");
                    }
                });

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Inside onStart");

//        int waitVal = 2000;
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis()-startTime<waitVal){}


//        finish();
//        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    protected void recordGroundTruth(String label, boolean isFinish) {
//        int pp= setSharedMap();
//        Log.d("shared_data_shm", "pp "+pp);
//        int ans = getSharedMapVal();
//        Log.d("shared_data_shm ", " rainview get "+ans);
//        Log.d("sharedPref", " splash request " + timingCount);

//        timingCount = getSharedPreferences("SideChannelInfo", Context.MODE_MULTI_PROCESS)
//                .getLong("timeCount", -1l);
        timingCount = -1l;
//        Log.d("shared_data_shm splash sharedpref", " sp.toString() "+savedValueInWriterProcess);
//        Log.d("sharedPref", " splash " + timingCount);
//        Log.d("splash", "groundTruthValues count " + timingCount);

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


        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 2000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(100, 10000 + 1)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        int n = array.length;
        int result = binarySearch(array, 5, 0, n - 1);
        Log.d("weather:AddressScan2", "#4_1_1#");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        return result;
    }

    static int pow(int base, int power) {
        int result = 1;
        for (int i = 0; i < power; i++)
            result *= base;
        return result;
    }

    protected int method1(int result ) {
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
        List<Integer> tempList2 = new ArrayList<>();
        List<Integer> tempList3 = new ArrayList<>();
        List<Integer> tempList4 = new ArrayList<>();
        List<Integer> tempList5 = new ArrayList<>();
        List<Integer> tempList6 = new ArrayList<>();
        IntStream.range(0, 200000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        result += binarySearch(array, 10010, 0, array.length - 1);

//        IntStream.range(0, 200000).forEach(i -> tempList2.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
//        array = tempList.stream().mapToInt(i -> i).toArray();
//        result += binarySearch(array, 10310, 0, array.length - 1);
//
//        IntStream.range(0, 200000).forEach(i -> tempList3.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
//        array = tempList.stream().mapToInt(i -> i).toArray();
//        result += binarySearch(array, 30010, 0, array.length - 1);
//
//        IntStream.range(0, 200000).forEach(i -> tempList4.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
//        array = tempList.stream().mapToInt(i -> i).toArray();
//        result += binarySearch(array, 3010, 0, array.length - 1);
//
//        IntStream.range(0, 200000).forEach(i -> tempList5.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
//        array = tempList.stream().mapToInt(i -> i).toArray();
//        result += binarySearch(array, 60010, 0, array.length - 1);
//
//        IntStream.range(0, 200000).forEach(i -> tempList6.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
//        array = tempList.stream().mapToInt(i -> i).toArray();
//        result += binarySearch(array, 40010, 0, array.length - 1);

        // get input from user for element to be searched

        // call the binary search method
        // pass arguments: array, element, index of first and last element


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

    public native void stringFromJNI(int[] pattern, int length);

    public static native long GetTimingCount();
//    public static native int setSharedMap();
    public static native int getSharedMapVal();



}
