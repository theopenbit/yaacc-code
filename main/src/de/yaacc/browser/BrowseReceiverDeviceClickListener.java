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

import org.teleal.cling.model.meta.Device;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.yaacc.R;

public class BrowseReceiverDeviceClickListener implements OnItemClickListener {

	private BrowseActivity activity;
	public BrowseReceiverDeviceClickListener(BrowseActivity activity){
		this.activity=activity;
	}
	@Override
	public void onItemClick(AdapterView<?> listView, View arg1, int position, long id) {
		ListView a = (ListView) listView.findViewById(R.id.itemList);
		BrowseDeviceAdapter adapter = (BrowseDeviceAdapter) listView.getAdapter();
		
		BrowseActivity.uClient.setReceiverDevice((Device)adapter.getItem(position));
		
		activity.onBackPressed();
				
	}

}
