/*
 * Copyright (C) 2013 www.yaacc.de 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.yaacc;

import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import de.yaacc.config.SettingsActivity;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.server.YaaccUpnpServerService;

public class MainActivity extends Activity implements OnClickListener {

	public static UpnpClient uClient = null;

	private BrowseItemAdapter bItemAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		uClient = new UpnpClient();
		uClient.initialize(getApplicationContext());

		// Start upnpserver service for avtransport
		Intent svc = new Intent(getApplicationContext(), YaaccUpnpServerService.class);
		getApplicationContext().startService(svc);

		final Button showDeviceNumber = (Button) findViewById(R.id.nbDev);
		showDeviceNumber.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		final ListView deviceList = (ListView) findViewById(R.id.deviceList);
		LinkedList<Device> devices = new LinkedList<Device>();
		devices.addAll(uClient.getDevices());
		Device first = devices.peekFirst();

		bItemAdapter = new BrowseItemAdapter(this);

		deviceList.setAdapter(bItemAdapter);

		/**
		 * ContentDirectoryBrowseResult result = uClient.browseSync(first, "1",
		 * BrowseFlag.DIRECT_CHILDREN, "", 0, 999l, null); List<Container>
		 * folders = result.getResult().getContainers();
		 **/

		String toShow = devices.size() + " devices found";
		for (Device device : devices) {
			toShow += device.getDisplayString() + "\n";
		}
		toShow += first.getDetails().getFriendlyName();

	}

}
