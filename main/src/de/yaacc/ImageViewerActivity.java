/*
 *
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
package de.yaacc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import de.yaacc.config.ImageViewerSettingsActivity;
import de.yaacc.config.SettingsActivity;

/**
 * a simple ImageViewer based on the android ImageView component;
 * 
 * you are able to start the activity either by using intnet.setData(anUri) or
 * by intent.putExtra(ImageViewerActivity.URIS, aList<Uri>); in the later case
 * the activity needed to be started with Intent.ACTION_SEND_MULTIPLE
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class ImageViewerActivity extends Activity {

	public static final String URIS = "URIS_PARAM";
	private ImageView imageView;
	private RetrieveImageTask retrieveImageTask;
	private LruCache<String, Drawable> imageCache;
	private List<Uri> imageUris = null; // playlist

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);
		imageView = (ImageView) findViewById(R.id.imageView);
		Intent i = getIntent();
		imageUris = new ArrayList<Uri>();
		Serializable urisData = i.getSerializableExtra(URIS);
		if (urisData != null) {
			if (urisData instanceof List) {
				imageUris = (List<Uri>) urisData;
			}

		} else {
			if (i.getData() != null) {
				imageUris.add(i.getData());
			}
		}
		if (imageUris.size() > 0) {
			// initialize Cache
			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			// Use 1/8th of the available memory for this memory cache.
			final int cacheSize = maxMemory / 8;
			imageCache = new LruCache<String, Drawable>(cacheSize) {
				//FIXME new API
				@SuppressLint("NewApi")
				@Override
				protected int sizeOf(String key, Drawable drawable) {
					// The cache size will be measured in kilobytes rather than
					// number of items.
					return ((BitmapDrawable) drawable).getBitmap()
							.getByteCount() / 1024;
				}
			};

			// start async task for showing images
			retrieveImageTask = new RetrieveImageTask(this);
			retrieveImageTask.execute(imageUris.toArray(new Uri[imageUris
					.size()]));
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(ImageViewerActivity.this,
							R.string.no_valid_uri_data_found_to_display,
							Toast.LENGTH_LONG);
					toast.show();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_image_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			i = new Intent(this, ImageViewerSettingsActivity.class);
			startActivity(i);
			return true;
		case R.id.yaacc_menu_settings:
			i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void play(){
		
	}
	
	public void stop(){
		
	}
	
	public void pause(){
		
	}
	
	public void previous(){
		
	}
	
	
	public void next(){
		
	}

	public void showImage(String uri) {
		final Drawable img = getImageFormCache(uri);
		if (uri != null) {
			runOnUiThread(new Runnable() {
				public void run() {
					imageView.setImageDrawable(img);
				}
			});
		}else{
			//load image
		}		

	}

	public void addImageToCache(String key, Drawable drawable) {
		if (getImageFormCache(key) == null) {
			imageCache.put(key, drawable);
		}
	}

	public Drawable getImageFormCache(String key) {
		return imageCache.get(key);
	}

}
