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

import java.io.InputStream;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
		if (imageUris == null)
			return null;
		for (Uri imageUri : imageUris)
			retrieveImage(imageUri);
		// This async task has no result
		return null;
	}

	/**
	 * retrieves an image an stores them in the image cache of the ImageViewerActivity.
	 * @param imageUri
	 */
	public void retrieveImage(Uri imageUri) {
		{
			Drawable image;
			try {
				Log.d(getClass().getName(), "imgeUri: " + imageUri);
				if (imageUri != null) {
					InputStream is = (InputStream) new java.net.URL(
							imageUri.toString()).getContent();
					Log.d(getClass().getName(), "InputStram: " + is);
					image = Drawable.createFromStream(is, "src");
					if (imageViewerActivity != null) {
						imageViewerActivity.addImageToCache(imageUri, image);
					}
					Log.d(getClass().getName(), "image: " + image);
				}
			} catch (final Exception e) {
				image = Drawable.createFromPath("@drawable/ic_launcher");
				Log.d(getClass().getName(), "Error while processing image", e);
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
	}

	
	
	
		
	
	

}
