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
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
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
 * 
 * The image viewer retrieves all images in a background task
 * (RetrieveImageTask). The images are written in a memory cache. The picture
 * show is processed by the ImageViewerActivity using the images in the cache.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
//Fixme New API
@SuppressLint("NewApi")
public class ImageViewerActivity extends Activity {

	public static final String URIS = "URIS_PARAM";
	private ImageView imageView;
	private RetrieveImageTask retrieveImageTask;
	private LruCache<Uri, Drawable> imageCache;
	private List<Uri> imageUris; // playlist
	private int currentImageIndex = 0;
	private Timer pictureShowTimer;

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
			initializeCache();
			// start async task for showing images
			retrieveImageTask = new RetrieveImageTask(this);
			retrieveImageTask.execute(imageUris.toArray(new Uri[imageUris
					.size()]));
			play();
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

	private void initializeCache() {
		// initialize Cache
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;
		imageCache = new LruCache<Uri, Drawable>(cacheSize) {

			@Override
			protected int sizeOf(Uri key, Drawable drawable) {
				if (drawable == null) {
					return 0;
				}
				// The cache size will be measured in kilobytes rather than
				// number of items.
				// New API: ((BitmapDrawable)
				// drawable).getBitmap().getByteCount() / 1024;
				// otherwise: assumption 24 bit per Pixel i.e. 3 byte
				//return drawable.getBounds().height()
				//		* drawable.getBounds().width() * 3 / 1024;
				return ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024;
			}
		};
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

	public void stopTimer() {
		if (pictureShowTimer != null) {
			pictureShowTimer.cancel();
			pictureShowTimer.purge();
			pictureShowTimer = null;
		}
	}

	/**
	 * Create and start a timer for picture changing
	 */
	public void startTimer() {
		if (pictureShowTimer == null) {
			pictureShowTimer = new Timer();
			pictureShowTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					ImageViewerActivity.this.next();

				}
			}, 0, getDuration());
		}
	}

	/**
	 * Start playing the picture show.
	 */
	public void play() {
		if (currentImageIndex < imageUris.size()) {
			showImage(imageUris.get(currentImageIndex));
		}
	}

	/**
	 * Stop picture show timer and reset the current playlist index. Display
	 * default image;
	 */
	public void stop() {
		stopTimer();
		currentImageIndex = 0;
		imageView.setImageDrawable(Resources.getSystem().getDrawable(
				R.drawable.ic_launcher));
	}

	/**
	 * Stop the timer.
	 */
	public void pause() {
		stopTimer();
	}

	/**
	 * show the previous image
	 */
	public void previous() {
		currentImageIndex--;
		if (currentImageIndex < 0) {
			if (imageUris.size() > 0) {
				currentImageIndex = imageUris.size() - 1;
			} else {
				currentImageIndex = 0;
			}
		}
		play();
	}

	/**
	 * show the next image.
	 */
	public void next() {
		currentImageIndex++;
		if (currentImageIndex > imageUris.size() - 1) {
			currentImageIndex = 0;
		}
		play();
	}

	/**
	 * Displays an image and start the picture show timer.
	 * 
	 * @param uri
	 */
	public void showImage(Uri uri) {
		if (uri != null) {
			Drawable img = getImageFormCache(uri);
			if (img == null) {
				// Cache miss we have to load the image synchronous
				// first we have to stop the timer, because loading might by
				// slow
				stopTimer();
				retrieveImageTask.retrieveImage(uri);
				img = getImageFormCache(uri);
			}
			final Drawable finalImg = img;
			runOnUiThread(new Runnable() {
				public void run() {
					imageView.setImageDrawable(finalImg);					
				}
			});
			startTimer();
		}

	}

	public void addImageToCache(Uri key, Drawable drawable) {
		if(getImageFormCache(key) != null){
			removeImageFromCache(key);
		}
		imageCache.put(key, drawable);

	}

	public void removeImageFromCache(Uri key) {
		imageCache.remove(key);

	}
	
	public Drawable getImageFormCache(Uri key) {
		return imageCache.get(key);
	}

	/**
	 * Return the configured slide stay duration
	 */
	private int getDuration() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		return Integer
				.parseInt(preferences.getString(
						getString(R.string.image_viewer_settings_duration_key),
						"2000"));
	}

}
