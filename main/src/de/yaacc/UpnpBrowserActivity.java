package de.yaacc;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.registry.RegistryListener;

import de.yaacc.upnp.BrowseRegistryListener;
import de.yaacc.upnp.DeviceDisplay;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.support.v4.app.NavUtils;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.registry.RegistryListener;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import de.yaacc.upnp.DeviceDisplay;
import de.yaacc.upnp.BrowseRegistryListener;

/**
 * 
 * http://4thline.org/projects/cling/core/manual/cling-core-manual.html#chapter.
 * Android
 * 
 * @author Tobias Schöne
 * 
 */
public class UpnpBrowserActivity extends ListActivity {
	// FIXME
		public ArrayAdapter<DeviceDisplay> listAdapter;
		private ServiceConnection serviceConnection = new ServiceConnection() {
			
			public void onServiceConnected(ComponentName className, IBinder service) {
				upnpService = (AndroidUpnpService) service;

				// Refresh the list with all known devices
				listAdapter.clear();
				for (Device device : upnpService.getRegistry().getDevices()) {
					((BrowseRegistryListener) registryListener).deviceAdded(device);
				}

				// Getting ready for future device advertisements
				upnpService.getRegistry().addListener(registryListener);

				// Search asynchronously for all devices
				upnpService.getControlPoint().search();
			}

			public void onServiceDisconnected(ComponentName className) {
				upnpService = null;
			}

		};

		private AndroidUpnpService upnpService;

		private RegistryListener registryListener = new BrowseRegistryListener(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upnp_browser);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		listAdapter = new ArrayAdapter(this,
				android.R.layout.activity_list_item);
		setListAdapter(listAdapter);

		getApplicationContext().bindService(
				new Intent(this, AndroidUpnpServiceImpl.class),
				serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_upnp_browser, menu);
//		menu.add(0, 0, 0, R.string.search_lan).setIcon(
//				android.R.drawable.ic_menu_search);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			
		if (item.getItemId() == 0 && upnpService != null) {
			upnpService.getRegistry().removeAllRemoteDevices();
			upnpService.getControlPoint().search();
		}
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (upnpService != null) {
			upnpService.getRegistry().removeListener(registryListener);
		}
		getApplicationContext().unbindService(serviceConnection);
	}

	

}
