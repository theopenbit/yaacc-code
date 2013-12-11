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

import java.util.Collection;
import java.util.LinkedList;

import org.fourthline.cling.model.meta.Device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.yaacc.R;
import de.yaacc.browser.BrowseItemAdapter.ViewHolder;

/**
 * @author Christoph Hähnel (eyeless)
 */
public class BrowseDeviceAdapter extends BaseAdapter {
	
	LinkedList<Device> devices;
	private LayoutInflater inflator;	
	

	public BrowseDeviceAdapter(Context ctx, LinkedList <Device> devices) {
		super();
		
		this.devices = devices;

		inflator = LayoutInflater.from(ctx);
	}

	@Override
	public int getCount() {
		return devices.size();
	}

	@Override
	public Object getItem(int position) {
		return devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null){
			convertView = inflator.inflate(R.layout.browse_item,parent,false);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.browseItemIcon);
			holder.name = (TextView) convertView.findViewById(R.id.browseItemName);			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.icon.setImageResource(R.drawable.device);
		holder.name.setText(((Device)getItem(position)).getDisplayString());
		
		return convertView;
	}
	
	public void setDevices(Collection <Device> devices){
		this.devices = new LinkedList<Device>();
		this.devices.addAll(devices);
	}

}
