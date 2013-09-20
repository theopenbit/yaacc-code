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

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.PlaylistItem;
import org.teleal.cling.support.model.item.TextItem;
import org.teleal.cling.support.model.item.VideoItem;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.yaacc.R;
import de.yaacc.upnp.ContentDirectoryBrowseResult;

/**
 * Adapter for browsing devices.
 * @author Christoph Hähnel (eyeless)
 */
public class BrowseItemAdapter extends BaseAdapter{
	
	private LayoutInflater inflator;
	private List<DIDLObject> objects;
	
	public BrowseItemAdapter(Context ctx, String objectId){
		Position pos = new Position(objectId, BrowseActivity.uClient.getProviderDevice());
		initialize(ctx, pos);
	}
	
	public BrowseItemAdapter(Context ctx, Position pos){
		initialize(ctx, pos);
	}
	
	private void initialize(Context ctx, Position pos){
				
		inflator = LayoutInflater.from(ctx);
		
		ContentDirectoryBrowseResult result = BrowseActivity.uClient.browseSync(pos);
    	if(result == null) return; 
    	
		DIDLContent a = result.getResult();
		
		if(a != null){
			
			objects = new LinkedList<DIDLObject>();
			
			//Add all children in two steps to get containers first
			objects.addAll(a.getContainers());
			objects.addAll(a.getItems());
			
		}else  {

			String text = ctx.getString(R.string.error_upnp_generic);
			int duration = Toast.LENGTH_SHORT;

			if(result.getUpnpFailure() != null){
				text = ctx.getString(R.string.error_upnp_specific)+" "+result.getUpnpFailure();
			}
			
			Log.e("ResolveError",text + "("+ pos.getObjectId() +")");
			
    		Toast toast = Toast.makeText(ctx, text, duration);
    		toast.show();
		}
		
		
		
    	
	}

	@Override
	public int getCount() {
		if(objects == null){
			return 0;
		}
		return objects.size();
	}

	@Override
	public Object getItem(int arg0) {
		return objects.get(arg0);
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
				
		DIDLObject currentObject = (DIDLObject)getItem(position);

		holder.name.setText(currentObject.getTitle());
		
		if(currentObject instanceof Container){
			holder.icon.setImageResource(R.drawable.folder);
		} else if(currentObject instanceof AudioItem){			
			holder.icon.setImageResource(R.drawable.cdtrack);
		} else if(currentObject instanceof ImageItem){
			holder.icon.setImageResource(R.drawable.image);
		} else if(currentObject instanceof VideoItem){
			holder.icon.setImageResource(R.drawable.video);
		} else if(currentObject instanceof PlaylistItem){
			holder.icon.setImageResource(R.drawable.playlist);
		} else if(currentObject instanceof TextItem){
			holder.icon.setImageResource(R.drawable.txt);
		} else {
			holder.icon.setImageResource(R.drawable.unknown);
		}

		return arg1;
	}
	
	
	static class ViewHolder{
		ImageView icon;
		TextView name;
	}
	
	public DIDLObject getFolder(int position){
		if(objects == null){
			return null;
		}
		return objects.get(position);
	}
	
	private Bitmap containedCoverImage(Container currentObject){
		List<Item> a = currentObject.getItems();
		while(!a.isEmpty()){
			Item toTest = a.remove(0);
			if (toTest instanceof ImageItem && ((ImageItem)toTest).getTitle().equals("cover.jpg")){
				Uri a = new Uri();
				URI b = ((ImageItem)toTest).getFirstResource().getImportUri();
				Bitmap bitmap = null; //decodeSampledBitmapFromStream(((ImageItem)toTest).getFirstResource().getImportUri(),	48, 48);
				return bitmap;
			}
		}
		return null;
	}

	private Bitmap decodeSampledBitmapFromStream(Uri imageUri, int reqWidth,
			int reqHeight) throws IOException {
		InputStream is = getUriAsStream(imageUri);

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.outHeight = reqHeight;
		options.outWidth = reqWidth;
		options.inPreferQualityOverSpeed = false;
		options.inDensity = DisplayMetrics.DENSITY_LOW;
		options.inTempStorage = new byte[7680016];
		Log.d(this.getClass().getName(),
				"displaying image size width, height, inSampleSize "
						+ options.outWidth + "," + options.outHeight + ","
						+ options.inSampleSize);
		Log.d(this.getClass().getName(), "free meomory before image load: "
				+ Runtime.getRuntime().freeMemory());
		Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is),
				null, options);
		Log.d(this.getClass().getName(), "free meomory after image load: "
				+ Runtime.getRuntime().freeMemory());
		return bitmap;
	}
	
	private InputStream getUriAsStream(Uri imageUri)
			throws FileNotFoundException, IOException, MalformedURLException {
		InputStream is = null;
		Log.d(getClass().getName(), "Start load: " + System.currentTimeMillis());
		
			is = (InputStream) new java.net.URL(imageUri.toString())
					.getContent();
		Log.d(getClass().getName(), "Stop load: " + System.currentTimeMillis());
		Log.d(getClass().getName(), "InputStream: " + is);
		return is;
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int byte_ = read();
					if (byte_ < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

}