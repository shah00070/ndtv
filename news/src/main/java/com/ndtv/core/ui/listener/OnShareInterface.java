package com.ndtv.core.ui.listener;

import com.ndtv.core.share.ShareApp;
import com.ndtv.core.share.ShareItem;

/**
 * Created by laveen on 3/3/15.
 */
public interface OnShareInterface {
    public void onShareOnFacebook(ShareItem item);
    public void onShareOnGooglePlus(ShareItem item);
    public void onShareOnTwitter(ShareItem item, ShareApp app);
    public void onShareOnNormal(ShareItem item,ShareApp app);
}
