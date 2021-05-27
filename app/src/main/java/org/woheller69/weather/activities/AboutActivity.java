package org.woheller69.weather.activities;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;

/**
 * Created by yonjuni on 15.06.16.
 */
public class AboutActivity extends NavigationActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("weather:AddressScan2", "#5#0#1");
        Log.d("weather:AddressScan2", "#5_0#");

        setContentView(R.layout.activity_about);

        overridePendingTransition(0, 0);

        ((TextView) findViewById(R.id.githubSecusoURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.githubURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.textFieldVersionName)).setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        int waitVal = 2000;
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - startTime < waitVal) {
//        }

        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_about;
    }
}

