package org.woheller69.weather.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Created by yonjuni on 24.10.16.
 */

public class SplashActivity extends AppCompatActivity {
    public static final String TAG = "SplashActivity";
    public static CacheScan cs = null;

    static {
        System.loadLibrary("native-lib");
    }

    private AppPreferencesManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Intent intent = getIntent();
//        String viewVal = intent.getStringExtra("viewVal");
//        Log.d("## ", "viewVal "+viewVal);
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));

//        getOdexBeginAddress();


        Intent begin = new Intent(this, SideChannelJob.class);
        startForegroundService(begin);

        if (true) {  //First time got to TutorialActivity
            Intent mainIntent = new Intent(SplashActivity.this, TutorialActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("viewVal" , viewVal==null?"View4":viewVal);
//            mainIntent.putExtras(bundle);
            SplashActivity.this.startActivity(mainIntent);
        } else { //otherwise directly start ForecastCityActivity

            Intent mainIntent = new Intent(SplashActivity.this, ForecastCityActivity.class);
            SplashActivity.this.startActivity(mainIntent);
        }

//        SplashActivity.this.finish();
//        int waitVal = 5000;
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - startTime < waitVal) {
//        }
//        finishAndRemoveTask();
//        overridePendingTransition( 0, 0);

    }

    public String getOdexBeginAddress() {

        // get Process ID of the running app
        int pid = android.os.Process.myPid();
        Log.d(TAG, "%%%% spLASH! " + pid);

        try {
            Log.d(TAG, "%%%% spLASH! grep woheller69 /proc/self/maps | grep odex");
            Optional<String> odc = Files.lines(Paths.get("/proc/self/maps")).collect(Collectors.toList())
                    .stream().sequential().filter(s-> s.contains("woheller69") && s.contains("base.odex"))
                    .findFirst().map(s-> new StringTokenizer(s, "-")).filter(StringTokenizer::hasMoreElements)
                    .map(StringTokenizer::nextToken);
            Log.d(TAG, "%%%% spLASH! odc " + odc);
            if(odc.isPresent())
            {
                return odc.get();
            }
        } catch (Exception e) {
            Log.d(TAG, "ERROR!!!!" + e.toString());
        }
        return "";
    }

}
