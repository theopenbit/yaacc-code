package de.yaacc;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
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
		    menu.setHeaderTitle("Context menu");
		    String[] menuItems = new String[2];
		    menuItems[0] = "Play whole folder";
		    menuItems[1] = "Download whole folder";
		    for (int i = 0; i<menuItems.length; i++) {
		      menu.add(Menu.NONE, i, i, menuItems[i]);
		    }
		  }
		
	
}
