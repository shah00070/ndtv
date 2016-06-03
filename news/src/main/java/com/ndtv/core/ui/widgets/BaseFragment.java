package com.ndtv.core.ui.widgets;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ndtv.core.config.model.Comments;
import com.ndtv.core.deeplinking.io.OnDeepLinkingInterface;
import com.ndtv.core.share.ShareApp;
import com.ndtv.core.util.GAHandler;

//import com.ndtv.core.common.util.ImageLoader;

/**
 * Created by Srihari S Reddy on 13/01/15.
 */
public abstract class BaseFragment extends Fragment {

    //private static final String TAG = makeLogTag(BaseFragment.class);
    private String mScreenName;
    public OnPaginationListener paginationListener;
    protected OnAddDetailFragmentListener mDetailFragmentListener;
    protected OnDeepLinkingInterface mDeeplinkListener;
    protected static int mPageCount;
    protected static boolean bIsFullPhotoFragment = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mDetailFragmentListener = (OnAddDetailFragmentListener) activity;
        } catch (ClassCastException ex) {

        }
        try {
            mDeeplinkListener = (OnDeepLinkingInterface) activity;
        } catch (ClassCastException e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void refresh() {

    }

    public static interface GCMListener {
        void registerGCM();

        void unregisterGCM();
    }

    @Override
    public void onResume() {
        super.onResume();
        setScreenName();
        if (mScreenName != null) {
            GAHandler.getInstance(getActivity()).SendScreenView(mScreenName);
        }
    }

    public abstract void setScreenName();

    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }

    public interface OnAddDetailFragmentListener {
        void onAddDetailFragment(Fragment fragment, String tag);
    }

    public ListItemClkListener mListItemClkListner;

    public static interface ListItemClkListener {
        void onNotificationHubShareClick(String message);

        void onPrimeShowsItemClicked(String link, String title, int navigationPos);

    }

    public static interface OnPaginationListener {
        void onPaginationStarted();

        void onpPaginationCompleted();
    }

    public OnPaginationListener castToOnPaginationListener() {
        try {
            return (OnPaginationListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName() + " must implement "
                    + OnPaginationListener.class.getName());
        }
    }


    protected ListItemClkListener castToListClkListner() {
        try {
            return (ListItemClkListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getName() + " must implement "
                    + ListItemClkListener.class.getName());
        }
    }

    public static interface FacebookShareListener {
        void onLaunchFacebookAccountLogin(ShareApp appInfo);
    }

    public interface CommentsLikeDislikeClickListener {
        public void onLikeClick(int position);

        public void onDisLikeClick(int position);

        public void onChildLikeClick(int parentPosition, int position);

        public void onChildDisLikeClick(int parentposition, int position);
    }

    public interface CommentsDownloadListener {
        void onDownloadComplete(Comments comments);

        void onDownloadFailed();
    }

    public interface PostCommentsListener {
        void onPostCommentComplete(String response);

        void onPostCommentFailed();
    }

    public interface PostCommentLikeListener {
        void onPostCommentLikeComplete();

        void onPostCommentLikeFailed();
    }

    public interface PostCommentDisLikeListener {
        void onPostCommentDisLikeComplete();

        void onPostCommentDisLikeFailed();
    }

}
