/**
 Project      : Awaaz
 Filename     : NewsConnectionManger.java
 Author       : anudeep
 Comments     :
 Copyright    : © Copyright NDTV Convergence Limited 2011
 Developed under contract by Robosoft Technologies
 History      : NA
 */

package com.ndtv.core.common.util;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.constants.ApplicationConstants;

import java.util.Map;

/**
 * @author Chandan kumar
 */
public class NewsConnectionManger implements ApplicationConstants.CustomApiType, ApplicationConstants.UrlKeys {

    /**
     * Error message to be displayed when there is no network
     */
    private static final String NO_NETWORK_MSG = "No Network";
    private static final String TAG = NewsConnectionManger.class.getSimpleName();


    public void downloadComments(Context ctx, String url, Listener<Comments> successListener,
                                 ErrorListener errorListener) {
        if (!TextUtils.isEmpty(url)) {
            final NdtvApplication application = NdtvApplication.getApplication(ctx);
            final GsonObjectRequest<Comments> jsObjRequest = new GsonObjectRequest<Comments>(Method.GET, url,
                    Comments.class, null, successListener, errorListener, ctx.getApplicationContext());
            jsObjRequest.setShouldCache(false);
            application.mRequestQueue.add(jsObjRequest);
        }
    }

    public void postComment(final Context context, final Map<String, String> questionParams, Listener<String> listener,
                            ErrorListener errorListener, boolean isUserInfo) {
        String postcommentUrl;
        if (isUserInfo) {
            postcommentUrl = ConfigManager.getInstance().getCustomApiUrl(POST_USERINFO);
        } else {
            postcommentUrl = ConfigManager.getInstance().getCustomApiUrl(POST_COMMENTS);
        }
        final NdtvApplication application = NdtvApplication.getApplication(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, postcommentUrl, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {

                return questionParams;
            }
        };

        application.mRequestQueue.add(postRequest);
    }

    public void commentLike(final Context context, final Map<String, String> params, Listener<String> listener,
                            ErrorListener errorListener) {

        final NdtvApplication application = NdtvApplication.getApplication(context);
        String strToReplace[] = new String[]{"@comment_id"};
        String replacement[] = new String[]{params.get(ApplicationConstants.UrlKeys.COMMENT_KEY)};
        String cmntLikeUrl = Utility.getFinalUrl(strToReplace, replacement, ConfigManager.getInstance()
                .getCustomApiUrl(POST_COMMENTS_LIKE), context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, cmntLikeUrl, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {

                return params;
            }
        };
        postRequest.setShouldCache(false);
        application.mRequestQueue.add(postRequest);
    }

    public void commentDisLike(final Context context, final Map<String, String> params, Listener<String> listener,
                               ErrorListener errorListener) {
        String strToReplace[] = new String[]{"@comment_id"};
        String replacement[] = new String[]{params.get(ApplicationConstants.UrlKeys.COMMENT_KEY)};
        String cmntDisLikeUrl = Utility.getFinalUrl(strToReplace, replacement, ConfigManager.getInstance()
                .getCustomApiUrl(POST_COMMENTS_DISLIKE), context);
        final NdtvApplication application = NdtvApplication.getApplication(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, cmntDisLikeUrl, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        postRequest.setShouldCache(false);
        application.mRequestQueue.add(postRequest);
    }

}
