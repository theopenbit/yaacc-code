/**
 *
 * Copyright (C) 2012 Tobias Schoene www.schoenesnetz.de kontakt@schoenesnetz.de
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
import java.util.concurrent.ExecutionException;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

/*
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * Background retriever for network images. 
 * @author Tobias Schöne (openbit)  
 * 
 */
public class RetrieveImageTask extends AsyncTask<Uri, Void, Drawable>{

	private ImageViewerActivity imageViewerActivity;

	
	public RetrieveImageTask(ImageViewerActivity imageViewerActivity) {
		this.imageViewerActivity = imageViewerActivity;
	}
	
	@Override
	protected Drawable doInBackground(Uri... imageUris) {
		if(imageUris == null) return null;
		Drawable image;
		try {
			Uri imageUri = imageUris[0];
			System.out.println("imgeUri: " + imageUri);
			InputStream is = (InputStream) new java.net.URL(imageUri.toString())
					.getContent();
			System.out.println("InputStram: " + is);
			image = Drawable.createFromStream(is, "src");
			System.out.println("image: " + image);
		} catch (Exception e) {
			image = Drawable.createFromPath("@drawable/ic_launcher");
			e.printStackTrace();

		}
		return image;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Drawable result) {		
		super.onPostExecute(result);
		if(imageViewerActivity != null){
			try {
				imageViewerActivity.showImage(get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	

}
