package de.yaacc;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BrowseItemClickListener implements OnItemClickListener {


	@Override
	public void onItemClick(AdapterView<?> listView, View arg1, int position, long id) {

		ListView a = (ListView) listView.findViewById(R.id.deviceList);
		BrowseItemAdapter adapter = (BrowseItemAdapter) listView.getAdapter();
		
		BrowseItemAdapter bItemAdapter = new BrowseItemAdapter(listView.getContext(),adapter.getFolder(position));
    	a.setAdapter(bItemAdapter);
    	
    	BrowseItemClickListener bItemClickListener = new BrowseItemClickListener();
    	a.setOnItemClickListener(bItemClickListener);
		
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(listView.getContext(), adapter.getFolder(position).getTitle()+": "+adapter.getFolder(position).getItems().size()+" items", duration);
		toast.show();
		    	
	}

}
