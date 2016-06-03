package com.ndtv.core.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.model.GraphUser;
import com.ndtv.core.R;
import com.ndtv.core.common.util.CommentConnectionManager;
import com.ndtv.core.common.util.NewsManager;
import com.ndtv.core.common.util.SplashAdManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.db.DatabaseManager;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.config.model.CommentsItem;
import com.ndtv.core.config.model.Reply;
import com.ndtv.core.config.model.UserInfo;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.share.FacebookHelper;
import com.ndtv.core.share.ShareApp;
import com.ndtv.core.share.TwitterHelper;
import com.ndtv.core.ui.adapters.CommentsListAdapter;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.net.URLDecoder;
import java.util.HashMap;

import retrofit.http.GET;
import retrofit.http.Query;

//import com.ndtv.core.config.model.UserInfo;

/**
 * Created by Chandan kumar on 2/27/2015.
 */
public class CommentsFragment extends BaseFragment implements ApplicationConstants.CommentConstants, BaseFragment.FacebookShareListener {
    private static final String LOG_TAG = "Comments";
    private ExpandableListView mCommentsList;
    private ProgressBar mProgressBar;
    private EditText mCommentText;
    private TextView mCommentsHeader;
    private UserInfo mUserInfo;
    private UserInfo mUserinfo;
    private final String SUBMIT_POST = "Post";
    private final String SUBMIT_SAVE = "Save";
    private int mListGroupPosition = 0;
    private CommentConnectionManager mCommentConManager;
    private FacebookHelper mFbHelper;
    String contentUrl, newsId, newsCategory, title, storyIdentifier;
    private Comments mComments;
    CommentsListAdapter mCommentsAdapter;
    private int mPageNum = 1;
    private int currentNewsPos, sectionPos;
    private TwitterAuthClient mAuthClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFbHelper = FacebookHelper.getInstance(getActivity());
        mFbHelper.oncreate(savedInstanceState);
        mFbHelper.setOnFBSignedInListener(null);

        if (null != getArguments()) {
            contentUrl = getArguments().getString("CONTENT_URL");
            title = getArguments().getString("CONTENT_TITLE");
            newsId = getArguments().getString("NEWS_ID");
            newsCategory = getArguments().getString("NEWS_CATEGORY");
            currentNewsPos = getArguments().getInt("CURRENT_NEWS_POS");
            sectionPos = getArguments().getInt("SECTION_POSITION");
            storyIdentifier = getArguments().getString("IDENTIFIER");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (null != getActivity()) {
            mListItemClkListner = castToListClkListner();
        }

        mCommentsHeader.setText(getActivity().getString(R.string.comments_header_text));
        if (null != getArguments()) {

            if (!TextUtils.isEmpty(storyIdentifier))
                getComments(storyIdentifier, mPageNum);
        }
    }

    @Override
    public void onLaunchFacebookAccountLogin(ShareApp appInfo) {
        mFbHelper.setOnFBSignedInListener(mFbSignedinListener);
        if (mFbHelper != null) {
            mFbHelper.getuserInfo();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFbHelper != null) {
            mFbHelper.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mFbHelper != null)
            mFbHelper.onActivityResult(requestCode, resultCode, data);

        if (mAuthClient != null) {
            mAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mFbHelper != null) {
            mFbHelper.onResume();
        }

        if (mCommentText.isFocused()) {
            if (getActivity() != null) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                // mCommentText.requestFocus();
                // TODO Hack to get focus on edit text and also pop the soft
                // keyboard
                Utility.showSoftKeyBoard(mCommentText);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.comments_layout, container, false);
        initViews(view);
        return view;
    }

    /**
     * @param view
     */
    private void initViews(View view) {
        mCommentsList = (ExpandableListView) view.findViewById(R.id.comments_list_view);
        mCommentsHeader = (TextView) view.findViewById(R.id.comments_header);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mCommentsList.setGroupIndicator(null);

        mCommentText = (EditText) view.findViewById(R.id.comment_text);
        mCommentText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (v.getText().toString().trim().length() > 0) {
                        new PostAsyncTask(mUserinfo).execute();
                        mCommentText.setVisibility(View.GONE);
                        if (getActivity() != null)
                            Utility.hideSoftKeyboard(getActivity());
                        return true;
                    } else {
                        Toast.makeText(getActivity(), R.string.blank_comment, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });

        mCommentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                if (i3 != 0 && i3 == i + i2 && (!mDowloadingComment && mDowloadComment)) {
                    mPageNum++;
                    getComments(storyIdentifier, mPageNum);
                    Log.d("TEST", "PAQGINATION:" + mPageNum);
                }
            }
        });

    }

    public void onLaunchTwitterAccountLogin(ShareApp appInfo) {
        TwitterHelper twitterHelper = TwitterHelper.getInstance(getActivity());
        if (twitterHelper.isLoggedIn()) {
            final TwitterSession activeTwitterSession =
                    Twitter.getSessionManager().getActiveSession();
            processForComment(activeTwitterSession);
        } else {
            login();
        }
    }

    public void login() {
        mAuthClient = new TwitterAuthClient();
        mAuthClient.authorize(getActivity(), new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                Toast.makeText(getActivity(), R.string.login_success_msg, Toast.LENGTH_SHORT).show();
                final TwitterSession twitterSession =
                        Twitter.getSessionManager().getActiveSession();
                processForComment(twitterSession);
            }

            @Override
            public void failure(TwitterException ex) {
                Toast.makeText(getActivity(), R.string.login_failure_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processForComment(TwitterSession twitterSession) {

        TwitterAuthToken authToken = twitterSession.getAuthToken();
        final String accessToken = authToken.token;
        // String secret = authToken.secret;
        new MyTwitterApiClient(twitterSession).getUsersService().show(12L, null, true,
                new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        mUserInfo = new UserInfo();
                        mUserInfo.uid = String.valueOf(result.data.id);
                        mUserInfo.first_name = result.data.screenName;
                        mUserInfo.last_name = "";
                        mUserInfo.profile_image = result.data.profileImageUrlHttps;
                        mUserInfo.site_name = "Twitter";
                        mUserInfo.access_token = accessToken;
                        postComment(mUserInfo);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("twittercommunity", "exception is " + exception);
                    }
                });
    }

    private FacebookHelper.OnFaceBookSignedInListener mFbSignedinListener = new FacebookHelper.OnFaceBookSignedInListener() {

        @Override
        public void onFBSignedOut() {

        }

        @Override
        public void onFBSignedIn(GraphUser user) {
            mUserInfo = new UserInfo();
            if (user != null) {
                mUserInfo.uid = user.getId();
                mUserInfo.first_name = user.getFirstName();
                mUserInfo.last_name = user.getLastName();
                mUserInfo.profile_image = user.getLink();
                mUserInfo.site_name = "Facebook";
                mUserInfo.access_token = mFbHelper.getAccessTocken();
            }
            postComment(mUserInfo);
            mFbHelper.setOnFBSignedInListener(null);
        }

    };


    public void postComment(final UserInfo user_info) {
        if (user_info != null) {
            //To avoid splash ads
            if (SplashAdManager.getSplashAdMngrInstance(getActivity()) != null) {
                SplashAdManager.getSplashAdMngrInstance(getActivity()).signInBtnClicked(false);
            }
            mCommentText.setVisibility(View.VISIBLE);
            mCommentText.setText("");
            if (getActivity() != null) {
                //  getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                // mCommentText.requestFocus();
                // TODO Hack to get focus on edit text and also pop the soft
                // keyboard
                Utility.showSoftKeyBoard(mCommentText);
                mUserinfo = user_info;
            }
        }
    }

    class PostAsyncTask extends AsyncTask<Void, Void, String> {

        String response;
        UserInfo mUserInfo;

        /**
         * @param userInfo
         */
        public PostAsyncTask(UserInfo userInfo) {
            mUserInfo = userInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            response = post(mUserInfo);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            if (null != getActivity()) {
                if (result != null) {
                    Toast.makeText(getActivity().getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.post_comment_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    protected String post(UserInfo userInfo) {
        mCommentConManager = new CommentConnectionManager("");
        String url = ConfigManager.getInstance().getCustomApiUrl(POST_USERINFO);
        String response = getUserIdForPost(url); // returns user id from http
        if (response != null) {// network change response beom null
            if (response.contains("Error")) {
                if (null != getActivity())
                    Toast.makeText(getActivity().getApplicationContext(), R.string.post_error, Toast.LENGTH_SHORT)
                            .show();
            }
        }
        url = ConfigManager.getInstance().getCustomApiUrl(POST_COMMENTS);
        response = postComment(url, response);
        return response;
    }

    private String getUserIdForPost(String url) {

        Bundle params = new Bundle();
        params.putString(COMMENT_METHOD, "POST");
        params.putString(COMMENT_AUTH_ID, mUserinfo.uid + "");
        params.putString(COMMENT_FIRTS_NAME, mUserinfo.first_name);
        params.putString(COMMENT_LAST_NAME, mUserinfo.last_name);
        params.putString(COMMENT_PROFILE_IMAGE, mUserinfo.profile_image);
        params.putString(COMMENT_SITE_NAME, mUserinfo.site_name);
        mUserinfo.access_token = null;
        // Log.d("FB", "Before :" + mUserinfo.access_token);
        if (mUserinfo.access_token != null) {
            // Log.d("FB", "inside not null" + mUserinfo.access_token);
            params.putString("access_token", URLDecoder.decode(mUserinfo.access_token));
        }

        return mCommentConManager.HttpPostRequest(url, params, SUBMIT_SAVE);
    }

    private String postComment(String url, String userid) {
        String parentId = "0";
        if (mComments != null && mCommentsAdapter != null
                && mCommentsAdapter.getReplyPosition() != CommentsListAdapter.NO_REPLAY_POSITION) {
            final int finalPos = mCommentsAdapter.getReplyPosition() / 2;
            parentId = mComments.commentsItemsList.get(finalPos).cid;
            mCommentsAdapter.setReplyPosition(CommentsListAdapter.NO_REPLAY_POSITION);
        }
        if (null != contentUrl && null != title) {
            String mTitle = title;
            String mGuid = storyIdentifier;
            String mWebLink = contentUrl;

            Bundle params = new Bundle();
            params.putString(COMMENT_METHOD, "POST");
            params.putString(COMMENT_USERID, userid);
            params.putString(COMMENT_PAGE_TITLE, mTitle);
            params.putString(COMMENT_PAGE_URL, mWebLink);
            params.putString(COMMENT_CTYPE, "story");
            params.putString(COMMENT_IDENTIFIER, mGuid);
            params.putString(COMMENT_TEXT, mCommentText.getText().toString());
            params.putString(PARENT_ID, parentId);
            return mCommentConManager.HttpPostRequest(url, params, SUBMIT_POST);
        }
        return null;
    }

    private boolean mDowloadComment = true;
    private boolean mDowloadingComment = false;

    /**
     *
     */
    private void getComments(final String storyIdentifier, int pageNum) {
        mDowloadingComment = true;
        String url = ConfigManager.getInstance().getCustomApiUrl(GET_COMMENTS_API);
        String strToReplace[] = new String[]{"@identifier"};
        String replacement[] = new String[]{storyIdentifier};
        url = Utility.getFinalUrl(strToReplace, replacement, url, getActivity(), pageNum + "");
        mProgressBar.setVisibility(View.VISIBLE);
        NewsManager.getNewsInstance().downloadComments(getActivity(), url, new CommentsDownloadListener() {

            @Override
            public void onDownloadFailed() {
                mDowloadingComment = false;
                mDowloadComment = false;
                if (null != getActivity()) {
                    mCommentsList.setEmptyView((getView()).findViewById(R.id.empty_view));
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDownloadComplete(Comments comments) {
                if (getActivity() == null)
                    return;

                mDowloadingComment = false;

                if (comments == null || comments.commentsItemsList == null || comments.commentsItemsList.size() == 0) {
                    mDowloadComment = false;
                    if (mCommentsAdapter != null)
                        mCommentsAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                    return;
                } else {
                    mDowloadComment = true;
                }

                if (mComments == null)
                    mComments = comments;
                else
                    mComments.commentsItemsList.addAll(comments.commentsItemsList);


                if (mCommentsAdapter == null) {
                    mCommentsAdapter = new CommentsListAdapter(getActivity(), mComments, mLikeDisLikeListener,
                            CommentsFragment.this);
                    mCommentsList.setAdapter(mCommentsAdapter);
                    mCommentsList.setSelectedGroup(mListGroupPosition);
                } else {
                    mCommentsAdapter.addCommentItemsToList(comments);
                    mCommentsAdapter.notifyDataSetChanged();
                }

                if (null != mCommentsList && null != getView())
                    mCommentsList.setEmptyView((getView()).findViewById(R.id.empty_view));

                mProgressBar.setVisibility(View.GONE);


            }
        });
    }


    CommentsLikeDislikeClickListener mLikeDisLikeListener = new CommentsLikeDislikeClickListener() {

        @Override
        public void onLikeClick(int position) {
            final int finalPos = position / 2;

            mListGroupPosition = position;

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(COMMENT_KEY, mComments.commentsItemsList.get(finalPos).uid);
            final CommentsItem commentsItem = mComments.commentsItemsList.get(finalPos);
            commentsItem.isLiked = true;
            commentsItem.isDisliked = false;
            NewsManager.getNewsInstance().likeComment(getActivity(), params, new PostCommentLikeListener() {

                @Override
                public void onPostCommentLikeFailed() {
                    Toast.makeText(getActivity().getApplicationContext(), "error",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPostCommentLikeComplete() {
                    if (null != getActivity()) {
                        DatabaseManager.getInstance(getActivity()).setCommentLikeDislikeItem(commentsItem,
                                getActivity());
                        if (null != getActivity()) {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.comment_liked_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        getComments(storyIdentifier, mPageNum);
                    }
                }
            });
        }

        @Override
        public void onDisLikeClick(int position) {
            HashMap<String, String> params = new HashMap<String, String>();
            final int finalPos = position / 2;

            mListGroupPosition = position;

            params.put(COMMENT_KEY, mComments.commentsItemsList.get(finalPos).cid);
            final CommentsItem commentsItem = mComments.commentsItemsList.get(finalPos);
            commentsItem.isLiked = false;
            commentsItem.isDisliked = true;
            NewsManager.getNewsInstance().disLikeComment(getActivity(), params, new PostCommentDisLikeListener() {

                @Override
                public void onPostCommentDisLikeFailed() {

                }

                @Override
                public void onPostCommentDisLikeComplete() {
                    if (null != getActivity()) {
                        DatabaseManager.getInstance(getActivity()).setCommentLikeDislikeItem(commentsItem,
                                getActivity());
                        if (null != getActivity()) {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.comment_disliked_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        getComments(storyIdentifier, mPageNum);
                    }

                }
            });
        }

        @Override
        public void onChildLikeClick(int parentPosition, final int position) {
            final int finalParentPos = parentPosition / 2;

            mListGroupPosition = parentPosition;

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(COMMENT_KEY, mComments.commentsItemsList.get(finalParentPos).replyItems.get(position).getCid());
            final Reply commentsItem = mComments.commentsItemsList.get(finalParentPos).replyItems.get(position);
            commentsItem.isLiked = true;
            commentsItem.isDisliked = false;
            NewsManager.getNewsInstance().likeComment(getActivity(), params, new PostCommentLikeListener() {

                @Override
                public void onPostCommentLikeFailed() {

                }

                @Override
                public void onPostCommentLikeComplete() {
                    if (null != getActivity()) {
                        DatabaseManager.getInstance(getActivity()).setCommentLikeDislikeItem(commentsItem,
                                getActivity());
                        if (null != getActivity()) {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.comment_liked_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        getComments(storyIdentifier, mPageNum);
                    }
                }
            });
        }

        @Override
        public void onChildDisLikeClick(int parentposition, final int position) {
            final int finalParentPos = parentposition / 2;

            mListGroupPosition = parentposition;

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(COMMENT_KEY, mComments.commentsItemsList.get(finalParentPos).replyItems.get(position).getCid());
            final Reply commentsItem = mComments.commentsItemsList.get(finalParentPos).replyItems.get(position);
            commentsItem.isLiked = false;
            commentsItem.isDisliked = true;
            NewsManager.getNewsInstance().disLikeComment(getActivity(), params, new PostCommentDisLikeListener() {

                @Override
                public void onPostCommentDisLikeFailed() {

                }

                @Override
                public void onPostCommentDisLikeComplete() {
                    if (null != getActivity()) {
                        DatabaseManager.getInstance(getActivity()).setCommentLikeDislikeItem(commentsItem,
                                getActivity());
                        if (null != getActivity()) {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.comment_disliked_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                        getComments(storyIdentifier, mPageNum);
                    }
                }
            });
        }
    };

    @Override
    public void refresh() {
        if (getActivity() != null && Utility.isInternetOn(getActivity())) {
            if (storyIdentifier != null) {
                mCommentsList.setEmptyView(null);
                mCommentsAdapter = null;
                mCommentsList.setAdapter(mCommentsAdapter);
                mProgressBar.setVisibility(View.VISIBLE);
                getComments(storyIdentifier, mPageNum);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() != null) {
            // getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

            ((HomeActivity) getActivity()).menu.findItem(R.id.menu_comment).setVisible(true);
            ((HomeActivity) getActivity()).menu.findItem(R.id.menu_share).setVisible(true);
            ((HomeActivity) getActivity()).menu.findItem(R.id.menu_post_comment).setVisible(false);
        }
    }

    @Override
    public void setScreenName() {
        setScreenName(LOG_TAG + " - " + storyIdentifier + " - " + title);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFbHelper != null) {
            mFbHelper.onDestroy();
            mFbHelper.clear();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mFbHelper != null) {
            mFbHelper.onPause();
        }
    }

    class MyTwitterApiClient extends TwitterApiClient {
        public MyTwitterApiClient(TwitterSession session) {
            super(session);
        }

        public UsersService getUsersService() {
            return getService(UsersService.class);
        }
    }

    interface UsersService {
        @GET("/1.1/users/show.json")
        void show(@Query("user_id") Long userId,
                  @Query("screen_name") String screenName,
                  @Query("include_entities") Boolean includeEntities,
                  Callback<User> cb);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity != null) {
            ((HomeActivity) activity).menu.findItem(R.id.menu_comment).setVisible(false);
            ((HomeActivity) activity).menu.findItem(R.id.menu_share).setVisible(false);
            ((HomeActivity) activity).menu.findItem(R.id.menu_post_comment).setVisible(true);
        }
    }
}