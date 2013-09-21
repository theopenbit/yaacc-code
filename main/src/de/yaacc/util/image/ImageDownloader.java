package de.yaacc.util.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by eyeless on 21.09.13.
 */
public class ImageDownloader {

    public ImageDownloader(){}

    public Bitmap retrieveIcon(Uri imageUri){
        Bitmap result = null;
        try{
            result = decodeSampledBitmapFromStream(imageUri, 48, 48);
        } catch (IOException e){
            Log.d(this.getClass().getName(),"while decoding image: "+e.getMessage());
        }
        return result;
    }

    private Bitmap decodeSampledBitmapFromStream(Uri imageUri, int reqWidth,
                                                 int reqHeight) throws IOException {
        InputStream is = getUriAsStream(imageUri);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.outHeight = reqHeight;
        options.outWidth = reqWidth;
        options.inPreferQualityOverSpeed = false;
        options.inDensity = DisplayMetrics.DENSITY_LOW;
        options.inTempStorage = new byte[7680016];
        Log.d(this.getClass().getName(),
                "displaying image size width, height, inSampleSize "
                        + options.outWidth + "," + options.outHeight + ","
                        + options.inSampleSize);
        Log.d(this.getClass().getName(), "free memory before image load: "
                + Runtime.getRuntime().freeMemory());
        Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(is),
                null, options);
        Log.d(this.getClass().getName(), "free memory after image load: "
                + Runtime.getRuntime().freeMemory());
        return bitmap;
    }

    private InputStream getUriAsStream(Uri imageUri)
            throws FileNotFoundException, IOException, MalformedURLException {
        InputStream is = null;
        Log.d(getClass().getName(), "Start load: " + System.currentTimeMillis());

        is = (InputStream) new java.net.URL(imageUri.toString())
                .getContent();
        Log.d(getClass().getName(), "Stop load: " + System.currentTimeMillis());
        Log.d(getClass().getName(), "InputStream: " + is);
        return is;
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int byte_ = read();
                    if (byte_ < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }


}
