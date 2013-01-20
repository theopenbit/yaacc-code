package de.yaacc;

import java.util.List;
import java.util.NoSuchElementException;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.yaacc.upnp.ContentDirectoryBrowseResult;

public class BrowseItemAdapter extends BaseAdapter {
	
	private final LayoutInflater inflator;
	private final List<Container> folders;
	
	public BrowseItemAdapter(Context ctx){
		inflator = LayoutInflater.from(ctx);
		
		boolean foundNone = true;
		Device first = null;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
	       
    	if(preferences.getString("provider_list", null) != null){
    		first = MainActivity.uClient.getDevice(preferences.getString("provider_list", null));
    	}
		
    	if(first == null){
			while(foundNone){
			try{
				first = MainActivity.uClient.getDevices().iterator().next();
				foundNone = false;
			} catch (NoSuchElementException e){
				
			}
			}
    	}
		
		ContentDirectoryBrowseResult result = MainActivity.uClient.browseSync(first,"1");
		DIDLContent a = result.getResult(); //.getContainers();
		folders = a.getContainers();
		List <Item> b = a.getItems();
	}

	@Override
	public int getCount() {
		if(folders == null){
			return 0;
		}
		return folders.size();
	}

	@Override
	public Object getItem(int arg0) {
		return folders.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
		//return folders.get(arg0).getId();
	}

	@Override
	public View getView(int position, View arg1, ViewGroup parent) {
		ViewHolder holder;
		
		if(arg1 == null){
			arg1 = inflator.inflate(R.layout.browse_item,parent,false);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) arg1.findViewById(R.id.browseItemIcon);
			holder.name = (TextView) arg1.findViewById(R.id.browseItemName);
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}
		
		Context ctx = parent.getContext();
		
		BrowseItem bItem = new BrowseItem(((Container)getItem(position)).getTitle());
		holder.name.setText(bItem.getName());
		holder.icon.setImageResource(R.drawable.folder);
		
		return arg1;
	}
	
	
	static class ViewHolder{
		ImageView icon;
		TextView name;
	}


}