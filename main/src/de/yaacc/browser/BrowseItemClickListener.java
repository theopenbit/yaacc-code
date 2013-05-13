package de.yaacc.browser;

import java.util.ArrayList;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

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
import de.yaacc.R;

public class BrowseItemClickListener implements OnItemClickListener
		 {

	@Override
	public void onItemClick(AdapterView<?> listView, View arg1, int position,
			long id) {

		ListView a = (ListView) listView.findViewById(R.id.itemList);
		BrowseItemAdapter adapter = (BrowseItemAdapter) listView.getAdapter();

		DIDLObject currentObject = adapter.getFolder(position);

		if (currentObject instanceof Container) {
			// if the current id is null, go back to the top level
			String newObjectId = currentObject.getId() == null ? "0" : adapter
					.getFolder(position).getId();

			BrowseItemAdapter bItemAdapter = new BrowseItemAdapter(
					listView.getContext(), newObjectId);
			a.setAdapter(bItemAdapter);

			BrowseItemClickListener bItemClickListener = new BrowseItemClickListener();
			a.setOnItemClickListener(bItemClickListener);

		} else {
			BrowseActivity.uClient.initializePlayer(currentObject).play();
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
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
	
	/**
	 * Reacts on selecting an entry in the context menu.
	 * 
	 * Since this is the onContextClickListener also the reaction on clicking something in the context menu resides in this class
	 * @param item
	 * @return
	 */
	public boolean onContextItemSelected(DIDLObject selectedDIDLObject, MenuItem item, Context applicationContext) {
		if (item.getTitle().equals(applicationContext.getString(R.string.browse_context_play))) {
			    		BrowseActivity.uClient.initializePlayer(selectedDIDLObject).play();
    	} else if (item.getTitle().equals(applicationContext.getString(R.string.browse_context_add_to_playplist))){
			Toast toast = Toast.makeText(applicationContext, "add to playlist pressed (Not yet implemted)", Toast.LENGTH_SHORT);
    		toast.show();
			
		} else if (item.getTitle().equals(applicationContext.getString(R.string.browse_context_download))){
			Toast toast2 = Toast.makeText(applicationContext, "download pressed (Not yet implemted)", Toast.LENGTH_SHORT);
    		toast2.show();
			
		} else {
			Toast toast3 = Toast.makeText(applicationContext, "Magic key pressed (Neither implemented nor defined ;))", Toast.LENGTH_SHORT);
    		toast3.show();
		}
		return true;
	}
	
  
	
}
