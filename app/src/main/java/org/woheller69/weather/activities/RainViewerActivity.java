package org.woheller69.weather.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Debug;
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import static android.system.OsConstants.O_RDONLY;
import static android.system.OsConstants.O_RDWR;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.log;
import static java.lang.Math.negateExact;
import static java.lang.Math.toIntExact;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;
import static org.woheller69.weather.JobInsertRunnable.insert_locker;
import static org.woheller69.weather.activities.RainViewerActivity.waitVal;
import static org.woheller69.weather.activities.SplashActivity.cs;
import static org.woheller69.weather.activities.SplashActivity.fd;
import static org.woheller69.weather.activities.SplashActivity.groundTruthValues;
import static org.woheller69.weather.activities.SplashActivity.ground_truth_insert_locker;
import static org.woheller69.weather.activities.SplashActivity.readAshMem;
import static org.woheller69.weather.activities.SplashActivity.sideChannelValues;


public class RainViewerActivity extends AppCompatActivity {

    private WebView webView;
    private ImageButton btnPrev, btnNext, btnStartStop;
    public static final String TAG = "RainViewerActivity";
    private static SharedPreferences sharedPreferences;
    private static Long timingCount;
    static int waitVal = 1000;
    Map<String, String> configMap = new HashMap<>();
    List<String> targetMethods = new ArrayList<>();
    int delayBetween;
    int delayWithin;
    int innerLoopCount;
    List<Integer> delayLimits = Arrays.asList(10, 100, 250, 500, 800, 1000, 1200, 1500, 1700, 2000);
    int delayLimitLength = delayLimits.size();


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
//        Debug.startMethodTracing("sample"+System.currentTimeMillis());
        Log.d(TAG, "Inside onCreate");
        Log.d("weather:AddressScan2", "#4_0#");

        Bundle extras = getIntent().getExtras();


        assert extras != null;
        Log.d("configMap rain extras.getString(\"targetMethods\") ", extras.getString("targetMethods"));
        targetMethods = Arrays.asList(Objects.requireNonNull(extras.getString("targetMethods")).split(","));
//        delayLimits = Arrays.stream(Objects.requireNonNull(extras.getString("delayLimits")).split(","))
//                .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        delayBetween = Integer.parseInt(Objects.requireNonNull(extras.getString("delayBetween")));
        delayWithin = Integer.parseInt(Objects.requireNonNull(extras.getString("delayWithin")));
        waitVal = Integer.parseInt(Objects.requireNonNull(extras.getString("waitVal")));


        innerLoopCount = Integer.parseInt(Objects.requireNonNull(extras.getString("innerLoopCount")));

        Log.d("configMap rain ", "" + targetMethods + " " + delayBetween + " " + delayWithin + " " + innerLoopCount);
        int i = 0;


        int a = 0;
        long startTime;
        ChildC childC = new ChildC();
        ChildB childB = new ChildB();
        int delay=delayWithin;
        while (i < innerLoopCount) {

            if (targetMethods.contains("ChildC.methodB")) {

                delay = delay*(i+1);
                Log.d("delay: ", String.valueOf(delay));
                startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < delay) {
                }

                Log.d("weather:AddressScan2", "#4_0_1#");
                a = childC.methodB(delayWithin);
                Log.d("weather:AddressScan2", "#4_0_0#");
            }


            if (targetMethods.contains("ChildC.methodC")) {
                delay = (int) (Math.random()*delayWithin)*i;
                Log.d("delay: ", String.valueOf(delay));
                startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < delay) {
                }

                Log.d("weather:AddressScan2", "#4_1_1#");
                a = childC.methodC(delayWithin);
                Log.d("weather:AddressScan2", "#4_1_0#" + a);
            }

            if (targetMethods.contains("ChildB.methodB")) {
                delay = (int) (Math.random()*delayWithin)*(i+1);
//                delay=10000;
                Log.d("delay: ", String.valueOf(delay));
                startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < delay) {
                }

                Log.d("weather:AddressScan2", "#4_2_1#");

//                startActivity(intent);

                a = childB.methodB(delayWithin);
                Log.d("weather:AddressScan2", "#4_2_0#" + a);
            }



            i++;
        }

        setContentView(R.layout.activity_radius_search_result);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Inside onStart");

        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    protected int method1(int result) {
        int x = 1 + 3;
        Log.d("weather:AddressScan2", "#4_1_0#" + x);
//        long count = GetTimingCount();

//        Log.d("rainviewer", "groundTruthValues count " + count);
        Log.d("rainviewer", "groundTruthValues " + groundTruthValues.size());
        long startTime = System.currentTimeMillis();
        int delay = ThreadLocalRandom.current().nextInt(0, 1000);
//        while (System.currentTimeMillis() - startTime < (waitVal+delay)) {
//        }
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
//        while (System.currentTimeMillis() - startTime < (waitVal+delay)) {
//        }
        IntStream.range(0, 200000).forEach(i -> tempList2.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        array = tempList2.stream().mapToInt(i -> i).toArray();
        result += binarySearch(array, 10310, 0, array.length - 1);
//
        IntStream.range(0, 200000).forEach(i -> tempList3.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        array = tempList3.stream().mapToInt(i -> i).toArray();
        result += binarySearch(array, 30010, 0, array.length - 1);
//
        IntStream.range(0, 200000).forEach(i -> tempList4.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        array = tempList4.stream().mapToInt(i -> i).toArray();
        result += binarySearch(array, 3010, 0, array.length - 1);
//
        IntStream.range(0, 200000).forEach(i -> tempList5.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        array = tempList5.stream().mapToInt(i -> i).toArray();
        result += binarySearch(array, 60010, 0, array.length - 1);
//
        IntStream.range(0, 200000).forEach(i -> tempList6.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        array = tempList6.stream().mapToInt(i -> i).toArray();
        result += binarySearch(array, 40010, 0, array.length - 1);

        // get input from user for element to be searched

        // call the binary search method
        // pass arguments: array, element, index of first and last element


        return result;
    }


    protected long method10() {

        long timing = System.currentTimeMillis();
        long x = timing + 3;
        return x;
    }

    protected long method11() {

        long x = 5l;
        long timing = System.currentTimeMillis() + x;
        return timing;
    }


    static int pow(int base, int power) {
        int result = 1;
        for (int i = 0; i < power; i++)
            result *= base;
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

abstract class Parent {
    abstract int methodA();

    abstract int methodB(int delay);


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
}

class ChildA extends Parent {

    @Override
    int methodA() {
        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 200000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        int x = 1 + 3;
        return binarySearch(array, ThreadLocalRandom.current().nextInt(1000, 100000 + 1), 0, array.length - 1);
    }

    @Override
    int methodB(int delay) {
        Log.d("weather:AddressScan2", "#4_0_0#");
        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 200).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
        int x = 1 + 3;
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        sort(array);
        return array[0];
    }


}

class ChildB extends Parent {

    @Override
    int methodA() {
        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 50000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(1000, 10000 + 1)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        return binarySearch(array, ThreadLocalRandom.current().nextInt(1000, 100000 + 1), 0, array.length - 1);
    }

    @Override

    int methodB(int delay) {
        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 20000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(1000, 100000 + 1)));
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - startTime < (waitVal + delay)) {
//        }

        Integer[] array = new Integer[tempList.size()];
        IntStream.range(0, tempList.size()).forEach(i->array[i]=tempList.get(i));
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("test", "test124");
        intent.getPackage();
        swap(array, ThreadLocalRandom.current().nextInt(0, tempList.size())
                , ThreadLocalRandom.current().nextInt(0, tempList.size()),9);
        return array[0]+9;
    }

    private static void swap(Object[] x, int a, int b, int i) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

}

class ChildC extends Parent {

    @Override
    int methodA() {
        return 0;
    }

    @Override
    int methodB(int delay) {
        int x = 10;
        int y = x * x + x;
        int z = x + y + y * 3 + x * 10000 + y * x;
        long startTime = System.currentTimeMillis();

//        while (System.currentTimeMillis() - startTime < (waitVal + delay)) {
//        }
        List<Integer> tempList = new ArrayList<>();
        IntStream.range(0, 2000).forEach(i -> tempList.add(ThreadLocalRandom.current().nextInt(100, 10000)));
        int[] array = tempList.stream().mapToInt(i -> i).toArray();
        int n = array.length;
        int result = binarySearch(array, (int) (Math.random() * 3000), n, n - 1);
        return result;
    }

    protected int methodC(int delay) {
        int x = 1 + 3;
        String sClassName = "android.content.Intent";
        try {
            Class classToInvestigate = Class.forName(sClassName);
            String strNewFieldName = "EXTRA_CHANGED_PACKAGE_LIST";
            Field newIn22 = classToInvestigate.getField(strNewFieldName);
            sClassName=strNewFieldName.concat(newIn22.getName());

        } catch (ClassNotFoundException e) {
            // Class not found
        } catch (NoSuchFieldException e) {
            // Field does not exist, likely we are on Android 2.1 or older
            // provide alternative functionality to support older devices
        } catch (SecurityException e) {
            // Access denied!
        } catch (Exception e) {
            // Unknown exception
        }

        return sClassName.contains(String.valueOf(delay))?1:0;
    }
}
