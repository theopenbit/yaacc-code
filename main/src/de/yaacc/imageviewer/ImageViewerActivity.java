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
package de.yaacc.imageviewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.yaacc.config.ImageViewerSettingsActivity;
import de.yaacc.config.SettingsActivity;
import de.yaacc.util.ActivitySwipeDetector;
import de.yaacc.util.SwipeReceiver;
import de.yaacc.R;

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
public class ImageViewerActivity extends Activity implements SwipeReceiver {

	public static final String URIS = "URIS_PARAM";
	private ImageView imageView;
	private RetrieveImageTask retrieveImageTask;
	private LruCache<Uri, Drawable> imageCache;

	private List<Uri> imageUris; // playlist
	private int currentImageIndex = 0;

	private boolean pictureShowActive = false;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);
		imageView = (ImageView) findViewById(R.id.imageView);
		ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(
				this);
		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.layout);
		layout.setOnTouchListener(activitySwipeDetector);
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
			// display first image
			retrieveImageTask = new RetrieveImageTask(this);
			retrieveImageTask.execute(imageUris.get(0));
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
		case R.id.menu_next:
			next();
			return true;
		case R.id.menu_pause:
			pause();
			return true;
		case R.id.menu_play:
			play();
			return true;
		case R.id.menu_previous:
			previous();
			return true;
		case R.id.menu_stop:
			stop();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Create and start a timer for the next picture change. The timer runs only
	 * once.
	 */
	public void startTimer() {

		Timer pictureShowTimer = new Timer();
		pictureShowTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				Log.d(getClass().getName(), "TimerEvent" + this);
				ImageViewerActivity.this.next();

			}
		}, getDuration());

	}

	/**
	 * Start playing the picture show.
	 */
	public void play() {
		if (currentImageIndex < imageUris.size()) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast toast = Toast.makeText(ImageViewerActivity.this,
							R.string.play, Toast.LENGTH_SHORT);
					toast.show();
				}
			});
			loadImage();
			// Start the pictureShow
			pictureShowActive = true;
			startTimer();

		}
	}

	/**
	 * 
	 */
	private void loadImage() {
		if (retrieveImageTask.getStatus() == Status.RUNNING) {
			return;
		}
		retrieveImageTask = new RetrieveImageTask(this);
		retrieveImageTask.execute(imageUris.get(currentImageIndex));
	}

	/**
	 * Stop picture show timer and reset the current playlist index. Display
	 * default image;
	 */
	public void stop() {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = Toast.makeText(ImageViewerActivity.this,
						R.string.stop, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		currentImageIndex = 0;
		showDefaultImage();

		pictureShowActive = false;
	}

	/**
	 * 
	 */
	private void showDefaultImage() {
		imageView.setImageDrawable(Drawable
				.createFromPath("@drawable/ic_launcher"));
	}

	/**
	 * Stop the timer.
	 */
	public void pause() {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = Toast.makeText(ImageViewerActivity.this,
						R.string.pause, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		pictureShowActive = false;
	}

	/**
	 * show the previous image
	 */
	public void previous() {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = Toast.makeText(ImageViewerActivity.this,
						R.string.previous, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		currentImageIndex--;
		if (currentImageIndex < 0) {
			if (imageUris.size() > 0) {
				currentImageIndex = imageUris.size() - 1;
			} else {
				currentImageIndex = 0;
			}
		}
		loadImage();
	}

	/**
	 * show the next image.
	 */
	public void next() {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = Toast.makeText(ImageViewerActivity.this,
						R.string.next, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		currentImageIndex++;
		if (currentImageIndex > imageUris.size() - 1) {
			currentImageIndex = 0;
			pictureShowActive = false;
		}
		loadImage();
		if (pictureShowActive) {
			startTimer();
		}
	}

	/**
	 * Displays an image and start the picture show timer.
	 * 
	 * @param image
	 */
	public void showImage(final Drawable image) {
		if (image == null) {
			showDefaultImage();
			return;
		}
		runOnUiThread(new Runnable() {
			public void run() {
				imageView.setImageDrawable(image);
			}
		});

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

	// interface SwipeReceiver
	@Override
	public void onRightToLeftSwipe() {
		next();
	}

	@Override
	public void onLeftToRightSwipe() {
		previous();

	}

	@Override
	public void onTopToBottomSwipe() {
		// do nothing

	}

	@Override
	public void onBottomToTopSwipe() {
		// do nothing

	}

	private void initializeCache() {
		// initialize Cache
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Use 1/4th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 4;
		Log.d(getClass().getName(), "memory cache size: " + cacheSize);
		imageCache = new LruCache<Uri, Drawable>(cacheSize) {

			// @SuppressLint("NewApi")
			@Override
			protected int sizeOf(Uri key, Drawable drawable) {
				if (drawable == null) {
					return 0;
				}
				// The cache size will be measured in kilobytes rather than
				// number of items.
				// New API: ((BitmapDrawable)
				// drawable).getBitmap().getByteCount() / 1024;
				// otherwise: assumption 32 bit per Pixel i.e. 3 byte
				// this does not work correctly
				return drawable.getBounds().height()
						* drawable.getBounds().width() * 4 / 1024;
				// return ((BitmapDrawable) drawable).getBitmap().getByteCount()
				// / 1024;
			}
		};
	}

	public void addImageToCache(Uri key, Drawable drawable) {
		if (key == null || drawable == null) {
			return;
		}
		if (getImageFormCache(key) != null) {
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
}
