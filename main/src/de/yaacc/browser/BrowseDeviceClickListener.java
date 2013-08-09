package de.yaacc.browser;

import org.teleal.cling.model.meta.Device;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.yaacc.R;

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
