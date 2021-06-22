package org.woheller69.weather;

import android.app.Application;
import android.content.SharedPreferences;

public class APP extends Application {
    private final SharedPreferences settings = getSharedPreferences("prefs", 0);
    private final SharedPreferences.Editor editor = settings.edit();

    public SharedPreferences.Editor editSharePrefs() {
        return editor;
    }
}