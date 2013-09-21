package de.yaacc.util.image;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import org.teleal.cling.support.model.item.ImageItem;

import java.net.URL;

/**
 * @author: Christoph Hähnel (eyeless)
 */
public class IconDownloadTask extends AsyncTask<ImageItem, Integer, Bitmap> {

    private Bitmap result;

    @Override
    protected Bitmap doInBackground(ImageItem... images) {
        result = new ImageDownloader().retrieveIcon(Uri.parse(images[0].getFirstResource().getValue()));
        return result;
    }

    public Bitmap getResult() {
        return result;
    }
}
