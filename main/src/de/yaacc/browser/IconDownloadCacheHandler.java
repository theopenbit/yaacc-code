package de.yaacc.browser;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by eyeless on 26.09.13.
 */
public class IconDownloadCacheHandler {

    private LruCache<Integer, Bitmap> cache;
    private static IconDownloadCacheHandler instance;

    private IconDownloadCacheHandler(){
        initializeCache();
    }

    public static IconDownloadCacheHandler getInstance(){
        if (instance == null){
            instance = new IconDownloadCacheHandler();
        }
        return instance;
    }

    public Bitmap getBitmap(int position){
        return cache.get(position);
    }

    public void addBitmap(int position, Bitmap img){
        cache.put(position,img);
    }

    public void resetCache(){
        initializeCache();
    }

    private void initializeCache(){
        Long maxCacheSize = Runtime.getRuntime().maxMemory();
        int cacheSize =  maxCacheSize.intValue() / 1024 / 8;
        cache = new LruCache<Integer, Bitmap>(cacheSize);
    }




}
