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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

import java.util.ArrayList;
import java.util.List;

import de.yaacc.R;
import de.yaacc.player.Player;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.util.image.IconDownloadCacheHandler;

/**
 * Clicklistener when browsing folders.
 * @author Tobias Schoene (the openbit)
 */
public class ContentListClickListener implements OnItemClickListener{
    //FIXME: just for easter egg to play all items on prev button
    public static DIDLObject currentObject;
    private UpnpClient upnpClient;
    private Navigator navigator;

    public ContentListClickListener(UpnpClient upnpClient,Navigator navigator) {
        this.upnpClient = upnpClient;
        this.navigator = navigator;
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View arg1, int position,
                            long id) {
        ListView a = (ListView) listView.findViewById(R.id.contentList);
        BrowseItemAdapter adapter = (BrowseItemAdapter) listView.getAdapter();
        currentObject = adapter.getFolder(position);
        if (currentObject instanceof Container) {
            //Fixme: Cache should store information for different folders....
            IconDownloadCacheHandler.getInstance().resetCache();
            // if the current id is null, go back to the top level
            String newObjectId = currentObject.getId() == null ? Navigator.ITEM_ROOT_OBJECT_ID: adapter
                    .getFolder(position).getId();
            navigator.pushPosition(new Position(newObjectId, upnpClient.getProviderDevice()));
            BrowseItemAdapter bItemAdapter = new BrowseItemAdapter(
                    upnpClient, newObjectId);
            a.setAdapter(bItemAdapter);
            ContentListClickListener bItemClickListener = new ContentListClickListener(upnpClient,navigator);
            a.setOnItemClickListener(bItemClickListener);
        } else {
            List<Player> players = upnpClient.initializePlayers(currentObject);
            for (Player player : players) {
                if(player != null){
                    player.play();
                }
            }
        }
    }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(v.getContext().getString(R.string.browse_context_title));
        ArrayList<String> menuItems = new ArrayList<String>();
        //TODO: I think there might be some item dependent actions in the future, so this is designed as a dynamic list
        menuItems.add(v.getContext().getString(R.string.browse_context_play));
        //menuItems.add(v.getContext().getString(R.string.browse_context_add_to_playplist));
        menuItems.add(v.getContext().getString(R.string.browse_context_download));
        //TODO: Check via bytecode whether listsize is calculated every loop or just once, if do calculation before calling the loop
        for (int i = 0; i<menuItems.toArray(new String[menuItems.size()]).length; i++) {
            menu.add(Menu.NONE, i, i, menuItems.get(i));
        }
    }
    /**
     * Reacts on selecting an entry in the context menu.
     *
     * Since this is the onContextClickListener also the reaction on clicking something in the context menu resides in this class
     * @param item
     * @return
     */
    public boolean onContextItemSelected(DIDLObject selectedDIDLObject, MenuItem item, Context applicationContext) {
        if (item.getTitle().equals(applicationContext.getString(R.string.browse_context_play))) {
            List<Player> players = upnpClient.initializePlayers(selectedDIDLObject);
            for (Player player : players) {
                if(player != null){
                    player.play();
                }
            }
        } /*else if (item.getTitle().equals(applicationContext.getString(R.string.browse_context_add_to_playplist))){
            Toast toast = Toast.makeText(applicationContext, "add to playlist pressed (Not yet implemented)", Toast.LENGTH_SHORT);
            toast.show();
        } */else if (item.getTitle().equals(applicationContext.getString(R.string.browse_context_download))){
            try {
                upnpClient.downloadItem(selectedDIDLObject);
            }catch (Exception ex){
                Toast toast = Toast.makeText(applicationContext, "Can't download item: " + ex.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(applicationContext, "Magic key pressed (Neither implemented nor defined ;))", Toast.LENGTH_SHORT);
            toast.show();
        }
        return true;
    }
} 