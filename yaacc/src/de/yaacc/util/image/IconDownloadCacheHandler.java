package de.yaacc.util.image;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.LruCache;
/**
 * Provides cache functionality for Bitmap images in lists. Implemented as singleton to assure the
 * is always just one instance. Since there is always only one list shown at once there must not be
 * any other caches.
 * @author Christoph Hähnel (eyeless)
 */
public class IconDownloadCacheHandler {
    private LruCache<Uri, Bitmap> cache;
    private static IconDownloadCacheHandler instance;
    private IconDownloadCacheHandler(){
        initializeCache();
    }
    /**
     * Provides access to the current instance.If none exists a new one is created.
     * @return instance with empty cache
     */
    public static IconDownloadCacheHandler getInstance(){
        if (instance == null){
            instance = new IconDownloadCacheHandler();
        }
        return instance;
    }
    /**
     * Loads image from cache
     * @param uri uri the image is saved at
     * @return required image
     */
    public Bitmap getBitmap(Uri uri){
        return cache.get(uri);
    }
    /**
     * Adds image to cache
     * @param uri uri the image is saved at
     * @param img image to save
     */
    public void addBitmap(Uri uri, Bitmap img){
        cache.put(uri,img);
    }
    /**
     * Clear the whole cache.
     */
    public void resetCache(){
        initializeCache();
    }
    /**
     * Initializes a new cache with one eight of the currently available memory. This cache replaces
     * older caches if existing.
     */
    private void initializeCache(){
        Long maxCacheSize = Runtime.getRuntime().maxMemory();
        int cacheSize = maxCacheSize.intValue() / 1024 / 8;
        cache = new LruCache<Uri, Bitmap>(cacheSize);
    }
} 