package de.yaacc.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.meta.StateVariableTypeDetails;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.StringDatatype;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import de.yaacc.MainActivity;
import de.yaacc.R;
import de.yaacc.upnp.UpnpClient;

public class SettingsActivity extends PreferenceActivity{
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        

        LinkedList<Device> devices = new LinkedList<Device>();
        // TODO: populate with found devices
		
       UpnpClient upnpClient = MainActivity.uClient;
        	
        if (upnpClient.isInitialized()){

	        devices.addAll(upnpClient.getDevices());
		}

		
        
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
