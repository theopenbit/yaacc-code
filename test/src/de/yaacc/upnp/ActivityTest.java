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
package de.yaacc.upnp;

import java.net.URI;

import de.yaacc.ImageViewerActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;


/**
 * test cases for testing activities
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class ActivityTest extends AndroidTestCase {
	
	public void testImageViewerActivity() throws Exception{
		Context context = getContext();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		intent = new Intent(context, ImageViewerActivity.class);		
		
		intent.setDataAndType(Uri.parse("file:///android_asset/CIMG5019_1920x1080.jpg"), "image/jpeg");

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		for (String element : context.getAssets().list(".")) {
			Log.d(this.getClass().getName(), "element: " + element);
			
		}
		
		context.startActivity(intent);
		myWait();
	}
	
	protected void myWait() {
		myWait(30000l);
	}
	
	protected void myWait(final long millis) {

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
