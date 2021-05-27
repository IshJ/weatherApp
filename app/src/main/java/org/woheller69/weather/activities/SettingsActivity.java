package org.woheller69.weather.activities;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import org.woheller69.weather.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends NavigationActivity {

    @Override
    protected void onRestart() {
        super.onRestart();

        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("weather:AddressScan2", "#9#0#1");

        setContentView(R.layout.activity_settings);
        Log.d("weather:AddressScan2", "#9_0#");


    }

    @Override
    protected void onStart() {
        super.onStart();
//        overridePendingTransition(0, 0);
//        int waitVal = 2000;
//        long startTime = System.currentTimeMillis();
//        while (System.currentTimeMillis()-startTime<waitVal){}

        finish();
        overridePendingTransition( 0, 0);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_settings;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);
        }
    }
}
