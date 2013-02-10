package de.yaacc;

import java.util.ArrayList;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class BrowseItemClickListener implements OnItemClickListener, OnCreateContextMenuListener{


	@Override
	public void onItemClick(AdapterView<?> listView, View arg1, int position, long id) {

		ListView a = (ListView) listView.findViewById(R.id.deviceList);
		BrowseItemAdapter adapter = (BrowseItemAdapter) listView.getAdapter();
		
		DIDLObject currentObject = adapter.getFolder(position);
		
		if(currentObject instanceof Container){
			// if the current id is null, go back to the top level
			String newObjectId = currentObject.getId()==null?"0":adapter.getFolder(position).getId();
			
		BrowseItemAdapter bItemAdapter = new BrowseItemAdapter(listView.getContext(),newObjectId);
    	a.setAdapter(bItemAdapter);
    	
    	BrowseItemClickListener bItemClickListener = new BrowseItemClickListener();
    	a.setOnItemClickListener(bItemClickListener);
		
		} else {
			MainActivity.uClient.play((Item)currentObject);
		}
		
		
				    	
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
			
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		
	    menu.setHeaderTitle(v.getContext().getString(R.string.browse_context_title));
	    
	    ArrayList<String> menuItems = new ArrayList<String>();
	    
	    //TODO: I think there might be some item dependent actions in the future, so this is designed as a dynamic list
	    menuItems.add(v.getContext().getString(R.string.browse_context_play));
	    menuItems.add(v.getContext().getString(R.string.browse_context_add_to_playplist));
	    menuItems.add(v.getContext().getString(R.string.browse_context_download));
	    
	    //TODO: Check via bytecode whether listsize is calculated every loop or just once, if do calculation before calling the loop
	    for (int i = 0; i<menuItems.toArray(new String[menuItems.size()]).length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems.get(i));
	     }
	}
	
	
		
	
}
