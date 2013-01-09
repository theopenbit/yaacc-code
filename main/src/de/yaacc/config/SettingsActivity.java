package de.yaacc.config;

import de.yaacc.R;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        //setContentView(R.layout.activity_settings);
    }
}
