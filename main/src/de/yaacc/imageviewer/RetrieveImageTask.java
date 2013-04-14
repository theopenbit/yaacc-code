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

import java.io.InputStream;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.Toast;

/**
 * Background task for retrieving network images.
 * 
 * @author Tobias Schoene (openbit)
 * 
 */
public class RetrieveImageTask extends AsyncTask<Uri, Void, Void> {

	private ImageViewerActivity imageViewerActivity;

	public RetrieveImageTask(ImageViewerActivity imageViewerActivity) {
		this.imageViewerActivity = imageViewerActivity;
	}

	@Override
	protected Void doInBackground(Uri... imageUris) {
		if (imageUris == null || imageUris.length == 0) {
			return null;
		}
		if (imageUris.length > 1) {
			throw new IllegalStateException("more than one uri to be retrieved");
		}
		retrieveImage(imageUris[0]);
		// This async task has no result
		return null;
	}

	/**
	 * retrieves an image an stores them in the image cache of the
	 * ImageViewerActivity.
	 * 
	 * @param imageUri
	 */
	private void retrieveImage(Uri imageUri) {
		{
			Log.d(getClass().getName(), "imgeUri: " + imageUri);			
			Drawable image = imageViewerActivity.getImageFormCache(imageUri);
			if (image == null) {
				Log.d(getClass().getName(), "Image not in cache");
				try {
					if (imageUri != null) {
						InputStream is = (InputStream) new java.net.URL(
								imageUri.toString()).getContent();
						Log.d(getClass().getName(), "InputStram: " + is);
						image = Drawable.createFromStream(is, "src");
						if (imageViewerActivity != null) {
							imageViewerActivity
									.addImageToCache(imageUri, image);
						}
						Log.d(getClass().getName(), "image: " + image);
					}
				} catch (final Exception e) {
					image = Drawable.createFromPath("@drawable/ic_launcher");
					Log.d(getClass().getName(), "Error while processing image",
							e);
					imageViewerActivity.runOnUiThread(new Runnable() {
						public void run() {
							Toast toast = Toast.makeText(imageViewerActivity,
									"Exception:" + e.getMessage(),
									Toast.LENGTH_LONG);
							toast.show();
						}
					});

				}
			}
			final Drawable finalImage = image;
			imageViewerActivity.runOnUiThread(new Runnable() {
				public void run() {
						imageViewerActivity.showImage(finalImage);
				}
			});		

		}
	}

}
