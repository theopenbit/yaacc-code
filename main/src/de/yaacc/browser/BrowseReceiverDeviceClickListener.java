package de.yaacc.browser;

import org.teleal.cling.model.meta.Device;

import android.app.ActivityManager;
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
