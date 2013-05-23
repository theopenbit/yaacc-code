package de.yaacc.browser;

import java.util.Collection;
import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.yaacc.R;
import de.yaacc.browser.BrowseItemAdapter.ViewHolder;

public class BrowseDeviceAdapter extends BaseAdapter {
	
	LinkedList<Device> devices;
	private LayoutInflater inflator;	
	

	public BrowseDeviceAdapter(Context ctx, LinkedList <Device> devices) {
		super();

		BrowseActivity.uClient.storeNewVisitedObjectId("-1");
		
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
