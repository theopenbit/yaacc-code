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

import java.util.ArrayList;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.DIDLObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import de.yaacc.config.SettingsActivity;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.server.YaaccUpnpServerService;

public class BrowseActivity extends Activity implements OnClickListener {

	public static UpnpClient uClient = null;

	private BrowseItemAdapter bItemAdapter;
	
	BrowseItemClickListener bItemClickListener = null;
	
	

	private DIDLObject selectedDIDLObject;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);

		// local server startup
		uClient = new UpnpClient();
		uClient.initialize(getApplicationContext());
		
		// load preferences
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// initialize click listener
		bItemClickListener = new BrowseItemClickListener();
		
		if(preferences.getBoolean(getString(R.string.settings_local_server_chkbx), true)){

		if (preferences.getBoolean(
				getString(R.string.settings_local_server_chkbx), true)) {
			// Start upnpserver service for avtransport
			Intent svc = new Intent(getApplicationContext(),
					YaaccUpnpServerService.class);
			getApplicationContext().startService(svc);
		}

		final Button showDeviceNumber = (Button) findViewById(R.id.refreshMainFolder);
		showDeviceNumber.setOnClickListener(this);
		}
		
		//initialize buttons
		ImageButton btnPrev = (ImageButton) findViewById(R.id.controlPrev);
		btnPrev.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uClient.playbackPrev();
				
			}
		});
		
		
		ImageButton btnStop = (ImageButton) findViewById(R.id.controlStop);
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uClient.playbackStop();
				ImageButton btnStop = (ImageButton) findViewById(R.id.controlStop);
				btnStop.setVisibility(View.INVISIBLE);
			}
		});
		
		ImageButton btnNext = (ImageButton) findViewById(R.id.controlNext);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uClient.playbackNext();
				
			}
		});
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
		// Define where to show the folder contents
		final ListView deviceList = (ListView) findViewById(R.id.itemList);

		// Get Try to get selected device
		Device selectedDevice = null;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	       
    	if(preferences.getString(getString(R.string.settings_selected_provider_title), null) != null){
    		selectedDevice = uClient.getDevice(preferences.getString(getString(R.string.settings_selected_provider_title), null));
    	}
    	
    	// Load adapter if selected device is configured and found
    	if(selectedDevice != null){
	    	bItemAdapter = new BrowseItemAdapter(this,"0");
	    	deviceList.setAdapter(bItemAdapter);
	    	
	    	deviceList.setOnItemClickListener(bItemClickListener);
    	} else {
    		Context context = getApplicationContext();
    		CharSequence text = getString(R.string.browse_no_content_found);
    		int duration = Toast.LENGTH_SHORT;

    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    	}
		
		if (preferences.getString(
				getString(R.string.settings_selected_provider_title), null) != null) {
			selectedDevice = uClient
					.getDevice(preferences
							.getString(
									getString(R.string.settings_selected_provider_title),
									null));
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return bItemClickListener.onContextItemSelected(selectedDIDLObject, item, getApplicationContext());
	}
	
	
	@Override
	public void onBackPressed() {

		if ("0".equals(this.uClient.getCurrentObjectId())) {
			super.finish();
		}

		final ListView itemList = (ListView) findViewById(R.id.itemList);

		bItemAdapter = new BrowseItemAdapter(this,
				this.uClient.getLastVisitedObjectId());
		itemList.setAdapter(bItemAdapter);

		BrowseItemClickListener bItemClickListener = new BrowseItemClickListener();
		itemList.setOnItemClickListener(bItemClickListener);

		registerForContextMenu(itemList);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		if (v instanceof ListView) {
			ListView listView = (ListView) v;
			Object item = listView.getAdapter().getItem(info.position);
			if (item instanceof DIDLObject) {
				selectedDIDLObject = (DIDLObject) item;
			}
		}
		menu.setHeaderTitle(v.getContext().getString(
				R.string.browse_context_title));

		ArrayList<String> menuItems = new ArrayList<String>();

		// TODO: I think there might be some item dependent actions in the
		// future, so this is designed as a dynamic list
		menuItems.add(v.getContext().getString(R.string.browse_context_play));
		menuItems.add(v.getContext().getString(
				R.string.browse_context_add_to_playplist));
		menuItems.add(v.getContext()
				.getString(R.string.browse_context_download));

		// TODO: Check via bytecode whether listsize is calculated every loop or
		// just once, if do calculation before calling the loop
		for (int i = 0; i < menuItems.toArray(new String[menuItems.size()]).length; i++) {
			menu.add(Menu.NONE, i, i, menuItems.get(i));
		}
	}

}
