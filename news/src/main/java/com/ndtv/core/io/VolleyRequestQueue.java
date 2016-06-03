package com.ndtv.core.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.ndtv.core.util.FileUtils;
import com.ndtv.core.util.OkHttpStack;

/**
 * Created by Srihari S Reddy on 11/11/14.
 */
public class VolleyRequestQueue {

    private static final String CACHE_DIR = "feeds_and_images";
    private int mMaxCacheByteSize = 1024 * 1024 * 50;

    private static VolleyRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private VolleyRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized VolleyRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {

            //mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            //Using OkHttp for i/o transport
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), new OkHttpStack());
            Network network = new BasicNetwork(new HurlStack());

            Cache cache = new DiskBasedCache(FileUtils.getDiskCacheDir(mCtx, CACHE_DIR),
                    mMaxCacheByteSize); // 5MB default
            mRequestQueue = new RequestQueue(cache, network);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
