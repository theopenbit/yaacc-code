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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

import org.fourthline.cling.model.meta.Device;

import de.yaacc.R;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.UpnpClientListener;

/**
 * Activity for browsing devices and folders. Represents the entrypoint for the whole application.
 *
 * @author Tobias Schoene (the openbit)
 */
public class PlayerListActivity extends Activity implements
        UpnpClientListener {

    private UpnpClient upnpClient = null;
    private PlayerListItemAdapter itemAdapter;
    PlayerListItemClickListener itemClickListener = null;
    protected ListView contentList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {

        setContentView(R.layout.activity_player_list);
        upnpClient = UpnpClient.getInstance(getApplicationContext());
        itemClickListener = new PlayerListItemClickListener();
        contentList = (ListView) findViewById(R.id.playerList);
        registerForContextMenu(contentList);
        upnpClient.addUpnpClientListener(this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /**
     * load app preferences
     *
     * @return app preferences
     */
    private SharedPreferences getPreferences() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

    }


    @Override
    public void onBackPressed() {
        if (getParent() instanceof TabBrowserActivity) {
            ((TabBrowserActivity) getParent()).setCurrentTab(TabBrowserActivity.Tabs.RECEIVER);
        }
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