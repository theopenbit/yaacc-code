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
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.test.AndroidTestCase;
import android.util.Log;
import de.yaacc.imageviewer.ImageViewerActivity;


/**
 * test cases for testing activities
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class ActivityTest extends AndroidTestCase {
	
	private static String[] imageFileNames = { "CIMG5019_1920x1080.jpg",  "CIMG5019.JPG" };
		
	
	public void testImageViewerActivityHDImage() throws Exception{
		String filesDir = getContext().getFilesDir().toString();
		String fileName = "CIMG5019_1920x1080.jpg";
		copyAssetsToSdCard(fileName, filesDir);
		Context context =  getContext();
		
		Intent intent = new Intent(context, ImageViewerActivity.class);		
		
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
		Intent intent = new Intent(context, ImageViewerActivity.class);		
		
		intent.setDataAndType(Uri.parse("file:///"+filesDir+"/"+fileName), "image/jpeg");

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
		
		context.startActivity(intent);
		myWait();
	}
	
	
	
	public void testImageViewerActivityPictureShow() throws Exception{
		ArrayList<Uri> uris = new ArrayList<Uri>();
//		String filesDir = getContext().getFilesDir().toString();
//		String fileName = "CIMG5019.JPG";
		//copyAssetsToSdCard(fileName, filesDir);
		//uris.add(Uri.parse("file:///"+filesDir+"/"+fileName));
		uris.add(Uri.parse("http://kde-look.org/CONTENT/content-files/156304-DSC_0089-2-1600.jpg"));
		uris.add(Uri.parse("http://kde-look.org/CONTENT/content-files/156246-DSC_0021-1600.jpg"));
		uris.add(Uri.parse("http://kde-look.org/CONTENT/content-files/156225-raining-bolt-1920x1200.JPG"));
		uris.add(Uri.parse("http://kde-look.org/CONTENT/content-files/156223-kungsleden1900x1200.JPG"));
		uris.add(Uri.parse("http://kde-look.org/CONTENT/content-files/156218-DSC_0012-1600.jpg"));		
		Context context =  getContext();		
		Intent intent = new Intent(context, ImageViewerActivity.class);				
		intent.putExtra(ImageViewerActivity.URIS,uris);
		intent.putExtra(ImageViewerActivity.AUTO_START_SHOW, true);
		//Starting an activity form outside any other activity have to be allowed 
		//by this flag
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
		
		context.startActivity(intent);
		//while(true);
		myWait(30000);
	}
	
	
	
	public void testAddAssetsToMediaStore() throws Exception{
		addAssetsToMediaStore();
	}
	
	public void testMediaStoreAccess() throws Exception{
		addAssetsToMediaStore();
		 
		// Query for all images on external storage
	    String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME ,MediaStore.Images.Media.DATA};
	    String selection = "";
	    String [] selectionArgs = null;
	    Cursor mImageCursor = getContext().getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                                 projection, selection, selectionArgs, null );

	     
	    if ( mImageCursor != null ) {
	        mImageCursor.moveToFirst();
	        while(!mImageCursor.isAfterLast()){
	        	String id = mImageCursor.getString(mImageCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
	        	String name = mImageCursor.getString(mImageCursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
	        	String data = mImageCursor.getString(mImageCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
	        	Log.d(getClass().getName(), "Image: " + id + " Name: " + name + " Data: " + data);
	        	mImageCursor.moveToNext();
	        }
	    } else {
	        Log.d(getClass().getName(), "System media store is empty.");
	    }
	    mImageCursor.close();
	    removeAssestsFromMediaStore();
	}


	
	
	public void testImageViewerActivityByUsingMediaStore() throws Exception{
		addAssetsToMediaStore();		
		Context context =  getContext();
		// Query for all images on external storage
	    String[] projection = { MediaStore.Images.Media.DATA };
	    String selection = "";
	    String [] selectionArgs = null;
	    Cursor mImageCursor = context.getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                                 projection, selection, selectionArgs, null );
		
	    if ( mImageCursor != null ) {
	        mImageCursor.moveToFirst();
	        while(!mImageCursor.isAfterLast()){	        	
	        	String data = mImageCursor.getString(mImageCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
	        	Log.d(getClass().getName(), "Image: " + " Data: " + data);
	        	Intent intent = new Intent(Intent.ACTION_VIEW);
	        	
	        	intent = new Intent(context, ImageViewerActivity.class);		
	        	
	        	intent.setDataAndType(Uri.parse("file:///"+data), "image/jpeg");
	        	
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
	        	
	        	context.startActivity(intent);
	        	myWait(5000L);
	        	mImageCursor.moveToNext();
	        }
	    } else {
	        Log.d(getClass().getName(), "System media store is empty.");
	    }
	    mImageCursor.close();
		myWait();
		removeAssestsFromMediaStore();
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

    
    private void addAssetsToMediaStore() throws Exception{    	
    	String filesDir = getContext().getFilesDir().toString();
    	for (String fileName : imageFileNames) {
    		copyAssetsToSdCard(fileName, filesDir);
    		ContentValues values = new ContentValues(3);
    		values.put(MediaStore.Video.Media.TITLE, fileName);
    		values.put(MediaStore.Video.Media.MIME_TYPE, "image/jpg");
    		values.put(MediaStore.Video.Media.DATA, getContext().getFilesDir().getAbsolutePath() + "/" + fileName);
    		getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);			
		}
    }
    
    /**
	 * 
	 */
	private void removeAssestsFromMediaStore() {		
    	for (String fileName : imageFileNames) {
    		getContext().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.TITLE + "='"+fileName+"'", null);
    	}
	}
}
