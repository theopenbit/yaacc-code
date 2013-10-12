package de.yaacc.util.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import org.teleal.cling.support.model.item.ImageItem;

import java.io.IOException;

import de.yaacc.R;
import de.yaacc.browser.BrowseActivity;

/**
 * AsyncTask fpr retrieving icons while browsing.
 *
 * @author: Christoph Hähnel (eyeless)
 */
public class IconDownloadTask extends AsyncTask<ImageItem, Integer, Bitmap> {

    private Bitmap result;
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
     * @param images DIDLObject containing the ressource URL
     * @return icon
     */
    @Override
    protected Bitmap doInBackground(ImageItem... images) {
        result = cache.getBitmap(position);
        if (result == null){
             result = new ImageDownloader().retrieveIcon(Uri.parse(images[0].getFirstResource().getValue()));
             cache.addBitmap(position,result);
        }
        return result;
    }

    /**
     * Replaces the icon in the list with the freshly loaded icon
     * @param result downloaded icon
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        int visiblePosition = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(position - visiblePosition);
        if (v != null){
            ImageView c = (ImageView) v.findViewById(R.id.browseItemIcon);
            c.setImageBitmap(result);
        }
    }
}
