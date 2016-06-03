package com.ndtv.core.common.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.ui.BaseActivity;

import java.util.List;

/**
 * Created by root on 3/2/15.
 */
public class BaseArrayAdapter<T> extends ArrayAdapter<T> {

    /**
     * @param context
     * @param resource
     * @param objects
     */
    public ImageLoader imageLoader;

    public BaseArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        if (context instanceof BaseActivity) {
            imageLoader = VolleyRequestQueue.getInstance(context).getImageLoader();
        }
    }

}

