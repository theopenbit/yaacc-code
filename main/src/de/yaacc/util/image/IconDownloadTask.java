package de.yaacc.util.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import org.fourthline.cling.support.model.item.ImageItem;

import java.io.IOException;

import de.yaacc.R;
import de.yaacc.browser.BrowseActivity;

/**
 * AsyncTask fpr retrieving icons while browsing.
 *
 * @author: Christoph Hähnel (eyeless)
 */
public class IconDownloadTask extends AsyncTask<Uri, Integer, Bitmap> {

   
    private ListView listView;
    private int position;
    private IconDownloadCacheHandler cache;

    /**
     * Initialize a new download by handing over the the list and the position with the icon to download
     * @param list contains all item
     * @param position position in list
     */
    public IconDownloadTask(ListView list,int position){
        this.listView = list;
        this.position = position;
        this.cache = IconDownloadCacheHandler.getInstance();
    }

    /**
     * Download image and convert it to icon
     * @param uri uri of resource
     * @return icon
     */
    @Override
    protected Bitmap doInBackground(Uri... uri) {
    	Bitmap result =null;
    	if(cache != null){
    		result = cache.getBitmap(uri[0]);
    	}
        if (result == null){
            result = new ImageDownloader().retrieveIcon(uri[0]);            
            if(cache != null){
            	cache.addBitmap(uri[0],result);
            }
        }
        return result;
    }

    /**
     * Replaces the icon in the list with the recently loaded icon
     * @param result downloaded icon
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        int visiblePosition = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(position - visiblePosition);
        if (v != null && result != null){
            ImageView c = (ImageView) v.findViewById(R.id.browseItemIcon);
            c.setImageBitmap(result);
        }
    }
}