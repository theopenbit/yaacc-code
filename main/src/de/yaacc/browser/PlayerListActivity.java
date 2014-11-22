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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;

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
public class PlayerListActivity extends Activity implements OnClickListener,
        UpnpClientListener {

    private UpnpClient upnpClient = null;
    private PlayerListItemAdapter itemAdapter;
    PlayerListItemClickListener itemClickListener = null;
    private SharedPreferences preferences = null;
    private Intent serverService = null;
    protected ListView contentList;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init( savedInstanceState);
    }
    private void init(Bundle savedInstanceState) {

        setContentView(R.layout.activity_player_list);
// local server startup
        upnpClient = UpnpClient.getInstance(getApplicationContext());

// load preferences
        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
// initialize click listener
        itemClickListener = new PlayerListItemClickListener();

// Define where to show the folder contents for media
        contentList = (ListView) findViewById(R.id.playerList);
        registerForContextMenu(contentList);
// add ourself as listener
        upnpClient.addUpnpClientListener(this);
        if (upnpClient.getProviderDevice() !=null) {
            showMainFolder();
        } else {
            clearItemList();
        }
    }

    private void clearItemList(){
        this.runOnUiThread(new Runnable() {
            public void run() {
                contentList.setAdapter(new PlayerListItemAdapter(getApplicationContext(),new Position(Navigator.ITEM_ROOT_OBJECT_ID,null)));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /**
     * load app preferences
     * @return app preferences
     */
    private SharedPreferences getPrefereces(){
        if (preferences == null){
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
        Device providerDevice = upnpClient.getProviderDevice();
        if (providerDevice != null) {
            populateItemList(providerDevice);
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
        Device providerDevice = upnpClient.getProviderDevice();
        populateItemList(providerDevice);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return itemClickListener.onContextItemSelected(null,
                item, getApplicationContext());
    }
    @Override
/**
 * Stepps 'up' in the folder hierarchy or closes App if on device level.
 */
    public void onBackPressed() {

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {

    }
    /**
     * Shows/Hides the controls
     *
     * @param activated
     * true if the controls should be shown
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
     * device to access
     */
    private void populateItemList(Device providerDevice) {

        IconDownloadCacheHandler.getInstance().resetCache();
        this.runOnUiThread(new Runnable() {
            public void run() {
// Load adapter if selected device is configured and found
                Position pos = new Position(Navigator.ITEM_ROOT_OBJECT_ID, upnpClient.getProviderDevice());

                itemAdapter = new PlayerListItemAdapter(getApplicationContext(),
                        pos);
                contentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                contentList.setAdapter(itemAdapter);
                contentList.setOnItemClickListener(itemClickListener);
            }
        });
    }



    @Override
    public void deviceAdded(Device<?, ?, ?> device) {

    }
    @Override
    public void deviceRemoved(Device<?, ?, ?> device) {

    }
    @Override
    public void deviceUpdated(Device<?, ?, ?> device) {

    }

} 