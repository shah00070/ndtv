package com.ndtv.core.common.util;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by laveen on 05-02-2015.
 */
public class XMLRequest<T> extends Request<T> {

    private Response.Listener<T> mListener;
    private Class<T> mClass;
    private Map<String, String> mHeaders;

    public XMLRequest(int method, String url, Class<T> tClass, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mClass = tClass;
    }

    public XMLRequest(int method, String url, Class<T> tClass, Map<String, String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(method, url, tClass, listener, errorListener);
        mHeaders = headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders == null ? super.getHeaders() : mHeaders;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
        String parsed;
        try {
            parsed = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(networkResponse.data);
        }
        Serializer serializer = new Persister();
        try {
            return Response.success(serializer.read(mClass, parsed), HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T t) {
        mListener.onResponse(t);
    }
}
