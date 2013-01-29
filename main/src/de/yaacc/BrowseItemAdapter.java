package de.yaacc;

import java.util.List;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.yaacc.upnp.ContentDirectoryBrowseResult;

public class BrowseItemAdapter extends BaseAdapter{
	
	private LayoutInflater inflator;
	private List<Container> folders;
	
	public BrowseItemAdapter(Context ctx, Device selectedDevice){
		inflator = LayoutInflater.from(ctx);
		
    	ContentDirectoryBrowseResult result = MainActivity.uClient.browseSync(selectedDevice,"0");
    	DIDLContent a = result.getResult(); //.getContainers();
		folders = a.getContainers();
    	
	}
	
	public BrowseItemAdapter(Context ctx, Container selectedContainer){
		inflator = LayoutInflater.from(ctx);
		folders = selectedContainer.getContainers();
    	
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
				
		holder.name.setText(((Container)getItem(position)).getTitle());
		holder.icon.setImageResource(R.drawable.folder);

		return arg1;
	}
	
	
	static class ViewHolder{
		ImageView icon;
		TextView name;
	}
	
	public Container getFolder(int position){
		if(folders == null){
			return null;
		}
		return folders.get(position);
	}



}