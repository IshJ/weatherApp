package org.woheller69.weather.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import org.woheller69.weather.preferences.AppPreferencesManager;

import org.woheller69.weather.R;

import static java.lang.Boolean.TRUE;


public class RainViewerActivity extends AppCompatActivity {

    private WebView webView;
    private ImageButton btnPrev, btnNext, btnStartStop;

    @Override
    protected void onPause() {
        super.onPause();
        webView.destroy();   //clear webView memory
        finish();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        Log.d("weather:AddressScan2", "#4#0#1");
        Log.d("weather:AddressScan2", "#4_0#");

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
//        int waitVal = 2000;
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis()-startTime<waitVal){}
        finish();
        overridePendingTransition( 0, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
