package de.yaacc.settings;

import java.util.ArrayList;
import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

import de.yaacc.R;
import de.yaacc.browser.BrowseActivity;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.UpnpClientListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements UpnpClientListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference);
		
		populateDeviceLists();
		
		BrowseActivity.uClient.addUpnpClientListener(this);
	}
	
	public void addDevice(){
		
	}

	private void populateDeviceLists() {
		LinkedList<Device> devices = new LinkedList<Device>();
		// TODO: populate with found devices

		UpnpClient upnpClient = BrowseActivity.uClient;

		if (upnpClient != null) {
			if (upnpClient.isInitialized()) {
				devices.addAll(upnpClient
						.getDevicesProvidingContentDirectoryService());
			}

			ListPreference providerLp = (ListPreference) findPreference(getString(R.string.settings_selected_provider_title));

			// One entry per found device for providing media data
			ArrayList<CharSequence> providerEntries = new ArrayList<CharSequence>();
			ArrayList<CharSequence> providerEntryValues = new ArrayList<CharSequence>();
			for (Device currentDevice : devices) {
				providerEntries.add(currentDevice.getDisplayString());
				providerEntryValues.add(currentDevice.getIdentity().getUdn()
						.getIdentifierString());
			}

			providerLp.setEntries(providerEntries
					.toArray(new CharSequence[providerEntries.size()]));
			providerLp.setEntryValues(providerEntryValues
					.toArray(new CharSequence[providerEntries.size()]));

			devices = new LinkedList<Device>();
			devices.addAll(upnpClient.getDevicesProvidingAvTransportService());

			// One entry per found device for receiving media data
			ListPreference receiverLp = (ListPreference) findPreference(getString(R.string.settings_selected_receiver_title));
			ArrayList<CharSequence> receiverEntries = new ArrayList<CharSequence>();
			ArrayList<CharSequence> receiverEntryValues = new ArrayList<CharSequence>();
			for (Device currentDevice : devices) {
				receiverEntries.add(currentDevice.getDisplayString());
				receiverEntryValues.add(currentDevice.getIdentity().getUdn()
						.getIdentifierString());
			}

			// Add a default entry for the local device
			receiverEntries.add(android.os.Build.MODEL);
			receiverEntryValues.add(UpnpClient.LOCAL_UID);

			receiverLp.setEntries(receiverEntries
					.toArray(new CharSequence[receiverEntries.size()]));
			receiverLp.setEntryValues(receiverEntryValues
					.toArray(new CharSequence[receiverEntries.size()]));
		}
	}

	@Override
	public void deviceAdded(Device<?, ?, ?> device) {
		if (this.isVisible()){
			populateDeviceLists();
		}
	}

	@Override
	public void deviceRemoved(Device<?, ?, ?> device) {
		if(this.isVisible()){
			populateDeviceLists();
		}
	}

	@Override
	public void deviceUpdated(Device<?, ?, ?> device) {
		// TODO Auto-generated method stub
		
	}

}
