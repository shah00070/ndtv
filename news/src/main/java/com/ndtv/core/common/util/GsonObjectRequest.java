package com.ndtv.core.common.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/*import com.july.ndtv.news.dto.Comments;
import com.july.ndtv.news.dto.RelatedNews;
import com.july.ndtv.photos.io.PhotosConnectionManager;*/

public class GsonObjectRequest<T> extends Request<T> {

    public static final String LOG_TAG = GsonObjectRequest.class.getSimpleName();
    private final Gson gson;
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Listener<T> listener;
    public Context mApplicationContext;

    public GsonObjectRequest(int method, String url, Class<T> clazz, Map<String, String> headers, Listener<T> listener,
                             ErrorListener errorListener, Context context) {
        super(method, url, errorListener);
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
        mApplicationContext = context;
        // setShouldCache(false);
        setTag(LOG_TAG);
        DefaultRetryPolicy policy = new DefaultRetryPolicy(/*PhotosConnectionManager.DEFAULT_SEARCH_TIMEOUT*/60000, 1, 1f);
        setRetryPolicy(policy);
    }

    public GsonObjectRequest(int method, String url, Class<T> clazz, Map<String, String> headers, Listener<T> listener,
                             ErrorListener errorListener, DefaultRetryPolicy defaultRetryPolicy) {
        super(method, url, errorListener);
        // setShouldCache(false);
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
        setTag(LOG_TAG);
        setRetryPolicy(defaultRetryPolicy);
    }

    public GsonObjectRequest(int method, String url, Class<T> clazz,  Listener<T> listener,
                             ErrorListener errorListener) {
        super(method, url, errorListener);
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
        this.clazz = clazz;
        this.listener = listener;
        this.headers = null;
        // setShouldCache(false);
        setTag(LOG_TAG);
        DefaultRetryPolicy policy = new DefaultRetryPolicy(/*PhotosConnectionManager.DEFAULT_SEARCH_TIMEOUT*/60000, 1, 1f);
        setRetryPolicy(policy);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        if (null != listener)
            listener.onResponse(response);

    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            // TODO
            // Has to be removed , this was added since the related news api was
            // returning []
            /*if (clazz.getSimpleName() == RelatedNews.class.getSimpleName() && json.equalsIgnoreCase("[]")) {
				json = "{}";
			}
			// This is to avoid parsing of empty array this is server issue if
			// no content it should have returned empty object but curretnly
			// returns empty array hence not parsing .
			// Also note it is not just empty array its space thempty array
			// " []"
			if (clazz == Comments.class && json.equalsIgnoreCase(" []")) {
				Exception e = new Exception();
				return Response.error(new ParseError(e));
			}*/

            return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            Log.d(LOG_TAG, clazz.getSimpleName());
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }
}