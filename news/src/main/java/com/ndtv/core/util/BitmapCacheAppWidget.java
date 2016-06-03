/**
 Project      : Awaaz
 Filename     : BitmapCacheAppWidget.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * @author anudeep
 */
public class BitmapCacheAppWidget implements ImageCache {
    private static final String TAG = BitmapCacheAppWidget.class.getSimpleName();

    // Default memory cache size as a percent of device memory class
    public static final float DEFAULT_MEM_CACHE_PERCENT = 0.25f;

    private LruCache<String, Bitmap> mMemoryCache = null;

    @Override
    public Bitmap getBitmap(String key) {
        return getBitmapFromMemCache(key);
    }

    public BitmapCacheAppWidget(int memCacheSize) {
        init(memCacheSize);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        addBitmapToCache(key, bitmap);
    }

    /**
     * Initialize the cache.
     */
    private void init(int memCacheSize) {
        // Set up memory cache
        // Log.d(TAG, "Memory cache created (size = " + memCacheSize + "KB)");
        // mMemoryCache = new LruCache<String, Bitmap>(memCacheSize) {
        // /**
        // * Measure item size in kilobytes rather than units which is more
        // * practical for a bitmap cache
        // */
        // @Override
        // protected int sizeOf(String key, Bitmap bitmap) {
        // final int bitmapSize = getBitmapSize(bitmap) / 1024;
        // return bitmapSize == 0 ? 1 : bitmapSize;
        // }
        // };
    }

    /**
     * Get the size in bytes of a bitmap.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static int getBitmapSize(Bitmap bitmap) {
        if (ApplicationUtils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data   Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }
        if (mMemoryCache != null)
            synchronized (mMemoryCache) {

                // Add to memory cache
                if (mMemoryCache.get(data) == null) {
                    Log.d(TAG, "Memory cache put - " + data);
                    mMemoryCache.put(data, bitmap);
                }
            }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (data != null) {
            if (mMemoryCache != null) {
                synchronized (mMemoryCache) {

                    final Bitmap memBitmap = mMemoryCache.get(data);
                    if (memBitmap != null) {
                        Log.d(TAG, "Memory cache hit - " + data);
                        return memBitmap;
                    }
                }
            }
            Log.d(TAG, "Memory cache miss - " + data);
        }
        return null;
    }

    /**
     * Clears the memory cache.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            Log.d(TAG, "Memory cache cleared");
        }
    }

    /**
     * Sets the memory cache size based on a percentage of the max available VM
     * memory. Eg. setting percent to 0.2 would set the memory cache to one
     * fifth of the available memory. Throws {@link IllegalArgumentException} if
     * percent is < 0.05 or > .8. memCacheSize is stored in kilobytes instead of
     * bytes as this will eventually be passed to construct a LruCache which
     * takes an int in its constructor.
     * <p/>
     * This value should be chosen carefully based on a number of factors Refer
     * to the corresponding Android Training class for more discussion:
     * http://developer.android.com/training/displaying-bitmaps/
     *
     * @param percent Percent of memory class to use to size memory cache
     * @return Memory cache size in KB
     */
    public static int calculateMemCacheSize(float percent) {
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                    + "between 0.05 and 0.8 (inclusive)");
        }
        return Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
    }

}
