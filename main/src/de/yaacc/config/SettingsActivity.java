package de.yaacc.config;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.yaacc.R;

public class SettingsActivity extends PreferenceActivity{
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        
	}
	
	
	
}
