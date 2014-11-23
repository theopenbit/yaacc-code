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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/
package de.yaacc.browser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import org.fourthline.cling.model.meta.Device;

import java.util.LinkedList;

import de.yaacc.R;
import de.yaacc.settings.SettingsActivity;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.UpnpClientListener;
import de.yaacc.upnp.server.YaaccUpnpServerService;
import de.yaacc.util.AboutActivity;
import de.yaacc.util.YaaccLogActivity;
import de.yaacc.util.image.IconDownloadCacheHandler;

/**
 * Activity for browsing devices and folders. Represents the entrypoint for the whole application.
 *
 * @author Tobias Schoene (the openbit)
 */
public class ReceiverListActivity extends Activity implements OnClickListener,
        UpnpClientListener {
    private static final String RECEIVER_LIST_NAVIGATOR = "RECEIVER_LIST_NAVIGATOR";
    private UpnpClient upnpClient = null;


    BrowseReceiverDeviceClickListener bReceiverDeviceClickListener = null;

    private SharedPreferences preferences = null;
    private Intent serverService = null;
    protected ListView contentList;
    private static Navigator navigator = null;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RECEIVER_LIST_NAVIGATOR, navigator);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState.getSerializable(RECEIVER_LIST_NAVIGATOR) == null) {
            navigator = new Navigator();
        } else {
            navigator = (Navigator) savedInstanceState.getSerializable(RECEIVER_LIST_NAVIGATOR);
        }
        setContentView(R.layout.activity_browse);
// local server startup
        upnpClient = UpnpClient.getInstance(getApplicationContext());

// load preferences
        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
// initialize click listener
        bReceiverDeviceClickListener = new BrowseReceiverDeviceClickListener();
// Define where to show the folder contents for media
        contentList = (ListView) findViewById(R.id.receiverList);
        registerForContextMenu(contentList);
// add ourself as listener
        upnpClient.addUpnpClientListener(this);
        populateReceiverDeviceList();
    }

    /**
     * load app preferences
     *
     * @return app preferences
     */
    private SharedPreferences getPrefereces() {
        if (preferences == null) {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
        }
        return preferences;
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
     *
     * @return
     */
    private Intent getYaaccUpnpServerService() {
        if (serverService == null) {
            serverService = new Intent(getApplicationContext(),
                    YaaccUpnpServerService.class);
        }
        return serverService;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
/**
 * Navigation in option menu
 */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.yaacc_about:
                AboutActivity.showAbout(this);
                return true;
            case R.id.yaacc_log:
                YaaccLogActivity.showLog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {


    }


    /**
     * Stepps 'up' in the folder hierarchy or closes App if on device level.
     */
    @Override
    public void onBackPressed() {
        Log.d(ReceiverListActivity.class.getName(), "onBackPressed() CurrentPosition: " + navigator.getCurrentPosition());

       //Fixme navigation to previous tab

    }


    /**
     * Shows all available devices in the receiver device list.
     */
    private void populateReceiverDeviceList() {
        IconDownloadCacheHandler.getInstance().resetCache();
        this.runOnUiThread(new Runnable() {
            public void run() {
// Define where to show the folder contents
                ListView deviceList = (ListView) findViewById(R.id.receiverList);
                deviceList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                LinkedList<Device> receiverDevices = new LinkedList<Device>(upnpClient.getDevicesProvidingAvTransportService());
                BrowseReceiverDeviceAdapter bDeviceAdapter = new BrowseReceiverDeviceAdapter(getApplicationContext(), receiverDevices, upnpClient.getReceiverDevices());
                deviceList.setAdapter(bDeviceAdapter);
                deviceList.setOnItemClickListener(bReceiverDeviceClickListener);
            }
        });
    }


    /**
     * Refreshes the shown devices when device is added.
     */
    @Override
    public void deviceAdded(Device<?, ?, ?> device) {

        if (upnpClient.getReceiverDevices().contains(device)) {
            populateReceiverDeviceList();
        }
    }


    /**
     * Refreshes the shown devices when device is removed.
     */
    @Override
    public void deviceRemoved(Device<?, ?, ?> device) {
        Log.d(this.getClass().toString(), "device removal called");
        if (upnpClient.getReceiverDevices().contains(device)) {
            populateReceiverDeviceList();
        }
    }

    @Override
    public void deviceUpdated(Device<?, ?, ?> device) {

    }


}