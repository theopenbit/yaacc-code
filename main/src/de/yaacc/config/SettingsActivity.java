package de.yaacc.config;

import java.util.ArrayList;
import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import de.yaacc.R;

public class SettingsActivity extends PreferenceActivity{
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        

        LinkedList<Device> devices = new LinkedList<Device>();
        // TODO: populate with found devices
        
        
        //Looks like this can not be connected via R.string.*
        ListPreference providerLp = (ListPreference)findPreference("provider_list");
        
        // One entry per found device for providing media data
        ArrayList<CharSequence> providerEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> providerEntryValues = new ArrayList<CharSequence>();
        for(Device currentDevice: devices){
        	providerEntries.add(currentDevice.getDisplayString());
        	providerEntryValues.add(currentDevice.getIdentity().toString());
        }
        providerLp.setEntries(providerEntries.toArray(new CharSequence[providerEntries.size()]));
        providerLp.setEntryValues(providerEntryValues.toArray(new CharSequence[providerEntries.size()]));
        
        
        // One entry per found device for receiving media data
        ListPreference receiverLp = (ListPreference)findPreference("receiver_list");
        ArrayList<CharSequence> receiverEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> receiverEntryValues = new ArrayList<CharSequence>();
        for(Device currentDevice: devices){
        	receiverEntries.add(currentDevice.getDisplayString());
        	receiverEntryValues.add(currentDevice.getIdentity().toString());
        }
        receiverLp.setEntries(receiverEntries.toArray(new CharSequence[receiverEntries.size()]));
        receiverLp.setEntryValues(receiverEntryValues.toArray(new CharSequence[receiverEntries.size()]));
        
        
	}
	
	
	
}
