package de.yaacc.util.image;
import android.graphics.Bitmap;
import android.util.LruCache;
/**
 * Provides cache functionality for Bitmap images in lists. Implemented as singleton to assure the
 * is always just one instance. Since there is always only one list shown at once there must not be
 * any other caches.
 * @author Christoph H채hnel (eyeless)
 */
public class IconDownloadCacheHandler {
    private LruCache<Integer, Bitmap> cache;
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
     * @param position position in list the image corresponds to
     * @return required image
     */
    public Bitmap getBitmap(int position){
        return cache.get(position);
    }
    /**
     * Adds image to cache
     * @param position position in list the image belongs to
     * @param img image to save
     */
    public void addBitmap(int position, Bitmap img){
        cache.put(position,img);
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
        cache = new LruCache<Integer, Bitmap>(cacheSize);
    }
} 