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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.yaacc.R;
import de.yaacc.player.Player;
import de.yaacc.upnp.UpnpClient;

/**
 * Adapter for browsing player.
 *
 * @author Tobias Schoene (the openbit)
 */
public class PlayerListItemAdapter extends BaseAdapter {
    private LayoutInflater inflator;
    private List<Player> players;
    private UpnpClient upnpClient;


    public PlayerListItemAdapter(UpnpClient upnpClient) {
        this.upnpClient = upnpClient;
        initialize();
    }

    private void initialize() {
        inflator = LayoutInflater.from(upnpClient.getContext());
        players = upnpClient.getCurrentPlayers();

    }

    @Override
    public int getCount() {
        if (players == null) {
            return 0;
        }
        return players.size();
    }

    @Override
    public Object getItem(int arg0) {
        return players.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View arg1, ViewGroup parent) {
        ViewHolder holder;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(parent.getContext());
        Context context = parent.getContext();
        if (arg1 == null) {
            arg1 = inflator.inflate(R.layout.browse_item, parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView) arg1.findViewById(R.id.browseItemIcon);
            holder.name = (TextView) arg1.findViewById(R.id.browseItemName);
            arg1.setTag(holder);
        } else {
            holder = (ViewHolder) arg1.getTag();
        }
        Player player = players.get(position);
        if (player != null) {
            holder = holder == null ? holder = new ViewHolder() : holder;
            holder.name.setText(player.getName() +
                    " : " + upnpClient.getDevice(player.getDeviceId()).getDetails().getFriendlyName());
            holder.icon.setImageResource(player.getIconResourceId());
        }
        return arg1;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
    }


}