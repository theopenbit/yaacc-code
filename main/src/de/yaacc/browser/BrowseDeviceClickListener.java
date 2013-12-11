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

import org.fourthline.cling.model.meta.Device;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.yaacc.R;

/**
 * @author Christoph Hähnel (eyeless)
 */
public class BrowseDeviceClickListener implements OnItemClickListener {

	@Override
	public void onItemClick(AdapterView<?> listView, View arg1, int position, long id) {
		ListView a = (ListView) listView.findViewById(R.id.itemList);
		BrowseDeviceAdapter adapter = (BrowseDeviceAdapter) listView.getAdapter();
		
		BrowseActivity.uClient.setProviderDevice((Device)adapter.getItem(position));
		
		BrowseActivity.getNavigator().pushPosition(new Position(Navigator.ITEM_ROOT_OBJECT_ID, BrowseActivity.uClient.getProviderDevice()));
		
		BrowseItemAdapter bItemAdapter = new BrowseItemAdapter(
				listView.getContext(), Navigator.ITEM_ROOT_OBJECT_ID);
		a.setAdapter(bItemAdapter);
		
		BrowseItemClickListener bItemClickListener = new BrowseItemClickListener();
		a.setOnItemClickListener(bItemClickListener);
	}

}
