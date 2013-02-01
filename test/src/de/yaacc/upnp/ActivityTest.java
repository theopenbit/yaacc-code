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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import de.yaacc.ImageViewerActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
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
	
	
		
	
	public void testImageViewerActivityHDImage() throws Exception{
		String filesDir = getContext().getFilesDir().toString();
		String fileName = "CIMG5019_1920x1080.jpg";
		copyAssetsToSdCard(fileName, filesDir);
		Context context =  getContext();
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		intent = new Intent(context, ImageViewerActivity.class);		
		
		intent.setDataAndType(Uri.parse("file:///"+filesDir+"/"+fileName), "image/jpeg");

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
		
		context.startActivity(intent);
		myWait();
	}
	
	
	public void testImageViewerActivityBigImageFile() throws Exception{
		String filesDir = getContext().getFilesDir().toString();
		String fileName = "CIMG5019.JPG";
		copyAssetsToSdCard(fileName, filesDir);
		Context context =  getContext();
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		intent = new Intent(context, ImageViewerActivity.class);		
		
		intent.setDataAndType(Uri.parse("file:///"+filesDir+"/"+fileName), "image/jpeg");

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
		
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
	
	/*
	 * Copy all Test assets to the sdcard 
	 * in order to access them from the main app.
	 */
	private void copyAssetsToSdCard(String fileName, String filesDir) throws Exception
    {
		 	    
	    Context testContext = getContext().createPackageContext("de.yaacc.tests",
                Context.CONTEXT_IGNORE_SECURITY);
	    AssetManager assets = testContext.getAssets();	    
        InputStream in = null;
        OutputStream out = null;
        File file = new File(filesDir, fileName);
        try
        {
        	
            in = assets.open(fileName);
            out = getContext().openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e)
        {
            Log.e("tag", e.getMessage());
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

}
