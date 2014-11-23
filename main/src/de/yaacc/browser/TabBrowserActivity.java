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
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;

import org.fourthline.cling.model.meta.Device;

import de.yaacc.R;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.UpnpClientListener;
import de.yaacc.upnp.server.YaaccUpnpServerService;

/**
 * Activity for browsing devices and folders. Represents the entrypoint for the whole application.
 *
 * @author Tobias Schoene (the openbit)
 */
public class TabBrowserActivity extends ActivityGroup implements OnClickListener,
        UpnpClientListener {
    private UpnpClient upnpClient = null;

    private SharedPreferences preferences = null;
    private Intent serverService = null;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_browse);
        // local server startup
        upnpClient = UpnpClient.getInstance(getApplicationContext());

        // load preferences
        preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        TabHost tabHost =(TabHost) findViewById(R.id.browserTabHost);
        tabHost.setup(this.getLocalActivityManager());
        tabHost.addTab(tabHost.newTabSpec("server").setIndicator(getResources().getString(R.string.title_activity_server_list), getResources().getDrawable(R.drawable.device_48_48)).setContent(new Intent(this,ServerListActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("content").setIndicator(getResources().getString(R.string.title_activity_content_list), getResources().getDrawable(R.drawable.cdtrack)).setContent(new Intent(this,ContentListActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("receiver").setIndicator(getResources().getString(R.string.title_activity_receiver_list), getResources().getDrawable(R.drawable.laptop_48_48)).setContent(new Intent(this,ReceiverListActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("player").setIndicator(getResources().getString(R.string.title_activity_player_list), getResources().getDrawable(R.drawable.player_play)).setContent(new Intent(this,PlayerListActivity.class)));
 

        // add ourself as listener
        upnpClient.addUpnpClientListener(this);
        if (upnpClient.getProviderDevice() !=null) {
            //setCurrentTab
           // startActivity(new Intent(this,ContentListActivity.class));
        } else {
           // startActivity(new Intent(this,ServerListActivity.class));
        }
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

    @Override
    public void onBackPressed() {
        Log.d(TabBrowserActivity.class.getName(), "onBackPressed() " );

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {

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

    @Override
    public void onClick(View view) {

    }
}