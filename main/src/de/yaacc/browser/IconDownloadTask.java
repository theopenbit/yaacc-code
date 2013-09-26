package de.yaacc.browser;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import org.teleal.cling.support.model.item.ImageItem;

import java.net.URL;

import de.yaacc.R;
import de.yaacc.browser.BrowseItemAdapter;
import de.yaacc.util.image.ImageDownloader;

/**
 * @author: Christoph Hähnel (eyeless)
 */
public class IconDownloadTask extends AsyncTask<ImageItem, Integer, Bitmap> {

    private Bitmap result;
    private ListView a;
    private int position;
    private IconDownloadCacheHandler cache;

    public IconDownloadTask(ListView a,int position){
        this.a = a;
        this.position = position;
        this.cache = IconDownloadCacheHandler.getInstance();
    }

    @Override
    protected Bitmap doInBackground(ImageItem... images) {
        result = cache.getBitmap(position);
        if (result == null){
            result = new ImageDownloader().retrieveIcon(Uri.parse(images[0].getFirstResource().getValue()));
            cache.addBitmap(position,result);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        int visiblePosition = a.getFirstVisiblePosition();
        View v = a.getChildAt(position - visiblePosition);
        if (v != null){
            ImageView c = (ImageView) v.findViewById(R.id.browseItemIcon);
            c.setImageBitmap(result);
            c.setMaxHeight(70);
        }
    }
}
