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
 * @author @author Tobias Schoene (the openbit)
 */
public class ServerListActivity extends Activity implements
        UpnpClientListener {
    private UpnpClient upnpClient = null;


    BrowseDeviceClickListener bDeviceClickListener = null;


    private SharedPreferences preferences = null;
    private Intent serverService = null;
    protected ListView contentList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_server_list);
// local server startup
        upnpClient = new UpnpClient();
        upnpClient.initialize(getApplicationContext());
// load preferences
        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

// initialize click listener
        bDeviceClickListener = new BrowseDeviceClickListener();

// Define where to show the folder contents for media
        contentList = (ListView) findViewById(R.id.serverList);
        registerForContextMenu(contentList);

// add ourself as listener
        upnpClient.addUpnpClientListener(this);
        if (upnpClient.getProviderDevice() != null) {
            //Fixme navigate to content tab
        } else {
            populateDeviceList();
        }
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
    public void onBackPressed() {
        Log.d(ServerListActivity.class.getName(), "onBackPressed()");
        upnpClient.shutdown();
        super.finish();

    }


    /**
     * Shows all available devices in the main device list.
     */
    private void populateDeviceList() {
        //FIXME: Cache should be able to decide whether it is used for browsing or for devices lists
        IconDownloadCacheHandler.getInstance().resetCache();
        this.runOnUiThread(new Runnable() {
            public void run() {
// Define where to show the folder contents
                ListView deviceList = (ListView) findViewById(R.id.itemList);
                deviceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                BrowseDeviceAdapter bDeviceAdapter = new BrowseDeviceAdapter(getApplicationContext(), new LinkedList<Device>(upnpClient.getDevicesProvidingContentDirectoryService()));
                deviceList.setAdapter(bDeviceAdapter);
                deviceList.setOnItemClickListener(bDeviceClickListener);
            }
        });
    }


    /**
     * Refreshes the shown devices when device is added.
     */
    @Override
    public void deviceAdded(Device<?, ?, ?> device) {

        populateDeviceList();

    }

    /**
     * Refreshes the shown devices when device is removed.
     */
    @Override
    public void deviceRemoved(Device<?, ?, ?> device) {
        Log.d(this.getClass().toString(), "device removal called");

        populateDeviceList();

    }

    @Override
    public void deviceUpdated(Device<?, ?, ?> device) {
        populateDeviceList();
    }


} 