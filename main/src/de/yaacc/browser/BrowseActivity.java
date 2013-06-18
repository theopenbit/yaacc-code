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
package de.yaacc.browser;

import java.util.ArrayList;
import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.DIDLObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.yaacc.R;
import de.yaacc.player.Player;
import de.yaacc.player.PlayerFactory;
import de.yaacc.settings.SettingsActivity;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.UpnpClientListener;
import de.yaacc.upnp.server.YaaccUpnpServerService;
import de.yaacc.util.AboutActivity;

public class BrowseActivity extends Activity implements OnClickListener, OnLongClickListener,
		UpnpClientListener {

	private boolean displayingSomething = false;

	public static UpnpClient uClient = null;

	private BrowseItemAdapter bItemAdapter;

	BrowseItemClickListener bItemClickListener = null;
	
	BrowseDeviceClickListener bDeviceClickListener = null;

	private DIDLObject selectedDIDLObject;

	private SharedPreferences preferences = null;
	
	private Intent serverService = null;

	protected ListView contentList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);

		// local server startup
		uClient = new UpnpClient();
		uClient.initialize(getApplicationContext());

		// load preferences
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		// initialize click listener
		bItemClickListener = new BrowseItemClickListener();

		// initialize click listener
		bDeviceClickListener = new BrowseDeviceClickListener();


		// Define where to show the folder contents for media
		contentList = (ListView) findViewById(R.id.itemList);
		registerForContextMenu(contentList);

		// remove the buttons if local playback is enabled and background
		// playback is not enabled
		// FIXME: Include background playback
		if (uClient.isLocalPlaybackEnabled()) {
			activateControls(false);
		}

		// initialize buttons
		ImageButton btnPrev = (ImageButton) findViewById(R.id.controlPrev);
		btnPrev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// FIXME: uClient.playbackPrev();
				//FIXME: Until context menu isn't working using the prev-button for playAll
				//a little easter egg	
				if(BrowseItemClickListener.currentObject != null){
					uClient.initializePlayer(BrowseItemClickListener.currentObject).play();
				}

			}
		});

		ImageButton btnStop = (ImageButton) findViewById(R.id.controlStop);
		btnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(PlayerFactory.getCurrentPlayers().size() > 0){
				    Player player = PlayerFactory.getCurrentPlayers().get(0);
				    player.stop();
				}
				// FIXME: uClient.playbackStop();
				// FIXME: ImageButton btnStop = (ImageButton) findViewById(R.id.controlStop);
				// FIXME: btnStop.setVisibility(View.INVISIBLE);
			}
		});

		ImageButton btnNext = (ImageButton) findViewById(R.id.controlNext);
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// FIXME: uClient.playbackNext();
				//FIXME: Until there is no refresh button using the next button
				uClient.searchDevices();
			}
		});

		// add ourself as listener
		uClient.addUpnpClientListener(this);

		if (!displayingSomething) {
			//showMainFolder();
			populateItemList();

		}
		
		
	}
	
	@Override
	public void onResume() {
		
		// Intent svc = new Intent(getApplicationContext(), YaaccUpnpServerService.class);

		if (preferences.getBoolean(
				getString(R.string.settings_local_server_chkbx), false)) {
			// Start upnpserver service for avtransport
			getApplicationContext().startService(getYaaccUpnpServerService());
			Log.d(this.getClass().getName(), "Starting local service");
		} else {
			getApplicationContext().stopService(getYaaccUpnpServerService());
			Log.d(this.getClass().getName(), "Stopping local service");
		}
		
		
		super.onResume();
	}
	
	/**
	 * Singleton to avoid multiple instances when switch
	 * @return
	 */
	private Intent getYaaccUpnpServerService(){
		if (serverService == null){
			serverService = new Intent(getApplicationContext(),
					YaaccUpnpServerService.class);
		}
		
		return serverService;
	}

	/**
	 * Tries to populate the browsing area if a providing device is configured
	 */
	private void showMainFolder() {
		Device providerDevice = getProviderDevice();

		if (providerDevice != null) {
			populateItemList(providerDevice);

			displayingSomething = true;

		} else {

			this.runOnUiThread(new Runnable() {
				  public void run() {
			Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.browse_no_content_found), Toast.LENGTH_SHORT);
			toast.show();
				  }
			});
		}
		
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
		case R.id.yaacc_about:
			AboutActivity.showAbout(this);
			return true;				
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		Device providerDevice = getProviderDevice();
		populateItemList(providerDevice);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return bItemClickListener.onContextItemSelected(selectedDIDLObject,
				item, getApplicationContext());
	}

	@Override
	public void onBackPressed() {

		// FIXME: Since there are now some magic values in here, they should be removed...
		if ("0".equals(this.uClient.getCurrentObjectId())) {
			populateItemList();
		} else if ("-1".equals(this.uClient.getCurrentObjectId())){
			super.finish();
		} else {

			final ListView itemList = (ListView) findViewById(R.id.itemList);
	
			bItemAdapter = new BrowseItemAdapter(this,
					this.uClient.getLastVisitedObjectId());
			itemList.setAdapter(bItemAdapter);
	
			BrowseItemClickListener bItemClickListener = new BrowseItemClickListener();
			itemList.setOnItemClickListener(bItemClickListener);
		}

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

	/**
	 * Shows/Hides the controls
	 * 
	 * @param activated
	 *            true if the controls should be shown
	 */
	public void activateControls(boolean activated) {
		RelativeLayout controls = (RelativeLayout) findViewById(R.id.controls);
		if (activated) {
			controls.setVisibility(View.GONE);
		} else {
			controls.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Selects the place in the UI where the items are shown and renders the
	 * content directory
	 * 
	 * @param providerDevice
	 *            device to access
	 */
	private void populateItemList(Device providerDevice) {


		this.runOnUiThread(new Runnable() {
			public void run() {
				
				// Load adapter if selected device is configured and found
				bItemAdapter = new BrowseItemAdapter(getApplicationContext(),
						"0");
				contentList.setAdapter(bItemAdapter);

				contentList.setOnItemClickListener(bItemClickListener);
				
				
				

			}
		});

	}
	
	private void populateItemList(){
		this.runOnUiThread(new Runnable() {
			public void run() {
				
				// Define where to show the folder contents
				ListView deviceList = (ListView) findViewById(R.id.itemList);

				BrowseDeviceAdapter bDeviceAdapter = new BrowseDeviceAdapter(getApplicationContext(), new LinkedList<Device>(uClient.getDevices()));
				
				deviceList.setAdapter(bDeviceAdapter);

				deviceList.setOnItemClickListener(bDeviceClickListener);

			}
		});
	}

	/**
	 * Loads the device providing media files, as it is configured in the
	 * settings
	 * 
	 * @return configured device
	 */
	private Device getProviderDevice() {
		// Get Try to get selected device
		Device selectedDevice = null;

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (preferences.getString(
				getString(R.string.settings_selected_provider_title), null) != null) {
			selectedDevice = uClient
					.getDevice(preferences
							.getString(
									getString(R.string.settings_selected_provider_title),
									null));
		}

		return selectedDevice;
	}

	@Override
	public void deviceAdded(Device<?, ?, ?> device) {
		if (!displayingSomething) {
			// showMainFolder();
			populateItemList();
		}

	}

	@Override
	public void deviceRemoved(Device<?, ?, ?> device) {
		Log.d(this.getClass().toString(), "device removal called");
		if (!displayingSomething) {
			// showMainFolder();
			populateItemList();
		}

	}

	@Override
	public void deviceUpdated(Device<?, ?, ?> device) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(View v) {
		Toast toast = Toast.makeText(getApplicationContext(), "Long click", Toast.LENGTH_SHORT);
		toast.show();
		return true;
	}

}
