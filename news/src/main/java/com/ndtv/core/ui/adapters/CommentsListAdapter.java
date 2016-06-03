package com.ndtv.core.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ndtv.core.R;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.common.util.db.DatabaseManager;
import com.ndtv.core.common.util.views.AvatarDrawable;
import com.ndtv.core.config.model.Comments;
import com.ndtv.core.config.model.CommentsItem;
import com.ndtv.core.config.model.Reply;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.BaseActivity;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chandan kumar
 */
public class CommentsListAdapter extends BaseExpandableListAdapter implements ApplicationConstants.SocialShare {

    /**
     *
     */
    public static final int NO_REPLAY_POSITION = 999;
    private LayoutInflater mInflater;
    // private Comments mComments;
    private List<CommentsItem> mCommentsList = new ArrayList<CommentsItem>();
    private int mReplyPosition = NO_REPLAY_POSITION;
    private BaseFragment.CommentsLikeDislikeClickListener mListener;
    PopupWindow mAccountsPopup;
    private Context ctx;
    private ImageView userImageView;
    private String like_txt, dislike_txt, replies_txt;

    private BaseFragment.FacebookShareListener mFacebookShareListener;

    public CommentsListAdapter(Context context, Comments comments, BaseFragment.CommentsLikeDislikeClickListener listener,
                               BaseFragment.FacebookShareListener facebookShareListener) {
        mInflater = ((Activity) context).getLayoutInflater();
        mListener = listener;
        ctx = context;
        mFacebookShareListener = facebookShareListener;
        like_txt = context.getResources().getString(R.string.like);
        dislike_txt = context.getResources().getString(R.string.dislike);
        replies_txt = context.getResources().getString(R.string.replies);

        addCommentItemsToList(comments);
    }

    public void addCommentItemsToList(Comments comments) {
        for (CommentsItem commentsItem : comments.commentsItemsList) {
            mCommentsList.add(commentsItem);
            mCommentsList.add(commentsItem);
        }
    }

    public Object getChild(int groupPosition, int childPosition) {
        if ((groupPosition % 2 == 0)) {
            return null;
        } else {
            if (mCommentsList.get(groupPosition).replyItems != null) {
                return mCommentsList.get(groupPosition).replyItems.get(childPosition);
            } else {
                return null;
            }
        }
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        CommentGroupHolder holder = new CommentGroupHolder();
        final Reply item = (Reply) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.comment_reply_item, null);

            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.likeCount = (TextView) convertView.findViewById(R.id.like_count);
            holder.dislikeCount = (TextView) convertView.findViewById(R.id.dislike_count);
            holder.like = (ImageView) convertView.findViewById(R.id.like_btn);
            holder.disLike = (ImageView) convertView.findViewById(R.id.dislike_btn);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (CommentGroupHolder) convertView.getTag();
        }

        holder.comment.setText(item.getComment());
        holder.name.setText(item.getName());
        holder.time.setText(item.getCreated());
        if (item.getLikes() != null) {
            holder.likeCount.setText(item.getLikes());
        } else {
            holder.likeCount.setText("0");
        }
        if (item.getUnlikes() != null) {
            holder.dislikeCount.setText(item.getUnlikes());
        } else {
            holder.dislikeCount.setText("0");
        }
        DatabaseManager.getInstance(convertView.getContext()).isChildCommentLiked(item.cid, holder,
                new DatabaseManager.IsCommentLiked() {

                    @Override
                    public void onFetch(CommentsItem item, CommentGroupHolder commentGroupHolder) {

                    }

                    @Override
                    public void onFetchChild(Reply responseItem, CommentGroupHolder commentGroupHolder) {
                        if (responseItem != null)
                            if (responseItem.isLiked) {
                                if (!TextUtils.isEmpty(responseItem.likes)) {
                                    commentGroupHolder.likeCount.setText(((Integer.parseInt(responseItem.likes)) + 1)
                                            + "");
                                } else {
                                    commentGroupHolder.likeCount.setText(((Integer.parseInt("0")) + 1) + "");
                                }
                                commentGroupHolder.like.setEnabled(false);
                                commentGroupHolder.disLike.setEnabled(true);
                            } else if (responseItem.isDisliked) {
                                if (!TextUtils.isEmpty(responseItem.unlikes)) {
                                    commentGroupHolder.dislikeCount.setText(((Integer.parseInt(responseItem.unlikes)) + 1)
                                            + "");
                                } else {
                                    commentGroupHolder.dislikeCount.setText(((Integer.parseInt("0")) + 1) + "");
                                }
                                commentGroupHolder.disLike.setEnabled(false);
                                commentGroupHolder.like.setEnabled(true);
                            } else {
                                commentGroupHolder.disLike.setEnabled(true);
                                commentGroupHolder.like.setEnabled(true);
                            }

                    }
                });
        holder.like.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Utility.displayWIPToast(v.getContext());
                mListener.onChildLikeClick(groupPosition, childPosition);
            }
        });
        holder.disLike.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Utility.displayWIPToast(v.getContext());
                mListener.onChildDisLikeClick(groupPosition, childPosition);
            }
        });
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        if ((groupPosition % 2 == 0)) {
            return 0;
        } else {
            if (mCommentsList.get(groupPosition).replyItems != null) {
                return mCommentsList.get(groupPosition).replyItems.size();
            } else {
                return 0;
            }
        }
    }

    public Object getGroup(int groupPosition) {
        return mCommentsList.get(groupPosition);
    }

    public int getGroupCount() {
        return mCommentsList.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public static class CommentGroupHolder {
        TextView comment;
        TextView likeCount;
        TextView dislikeCount;
        ImageView like;
        ImageView disLike;
        TextView name;
        TextView time;
        ImageView replyBtn;
        ImageView userImage;

    }

    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        CommentsItem item = (CommentsItem) getGroup(groupPosition);
        if (item != null && item.likes != null && item.unlikes != null) {
            // final int likeCountInt = Integer.parseInt(item.likes);
            // final int disLikeCountInt = Integer.parseInt(item.unlikes);
        }
        if ((groupPosition % 2 == 0)) {
            CommentGroupHolder NameHolder;
            // if (convertView == null) {
            convertView = mInflater.inflate(R.layout.comment_name_item, null);
            NameHolder = new CommentGroupHolder();
            NameHolder.name = (TextView) convertView.findViewById(R.id.name);
            NameHolder.time = (TextView) convertView.findViewById(R.id.time);
            NameHolder.replyBtn = (ImageView) convertView.findViewById(R.id.more);
            NameHolder.userImage = (ImageView) convertView.findViewById(R.id.round_image);

            // convertView.setTag(NameHolder);
            // } else {
            // NameHolder = (CommentGroupHolder) convertView.getTag();
            // }
            NameHolder.name.setText(Utility.decodeString(item.name));
            NameHolder.time.setText(Utility.decodeString(item.created));

            //   final String url = "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRQrxz5YkQ9zytShcuifBmUl1m6IS_KjKXk_ebFgfD7p3YfN8bKivq30hY";

            Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.profile_placeholder);
            NameHolder.userImage.setImageBitmap(Utility.drawableToBitmap(new AvatarDrawable(Utility.getResizedBitmap(defaultBitmap, 50, 50))));

            // loadUserProfileImage(NameHolder.userImage, url);

            NameHolder.replyBtn.setTag(groupPosition);
            NameHolder.replyBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Utility.displayWIPToast(v.getContext());
                    mReplyPosition = (Integer) v.getTag();
                    ((BaseActivity) v.getContext()).isAllowShare = false;
                    ((BaseActivity) v.getContext()).createCustomLoginPopup();
                }
            });
        } else {
            CommentGroupHolder itemHolder;
            // if (convertView == null) {
            convertView = mInflater.inflate(R.layout.comment_list_item, null);
            itemHolder = new CommentGroupHolder();
            itemHolder.comment = (TextView) convertView.findViewById(R.id.comment);
            itemHolder.likeCount = (TextView) convertView.findViewById(R.id.like_count);
            itemHolder.dislikeCount = (TextView) convertView.findViewById(R.id.dislike_count);
            itemHolder.like = (ImageView) convertView.findViewById(R.id.like_btn);
            itemHolder.disLike = (ImageView) convertView.findViewById(R.id.dislike_btn);
            // convertView.setTag(itemHolder);
            // } else {
            // itemHolder = (CommentGroupHolder) convertView.getTag();
            // }

            DatabaseManager.getInstance(convertView.getContext()).isCommentLiked(item.cid, itemHolder,
                    new DatabaseManager.IsCommentLiked() {

                        @Override
                        public void onFetch(CommentsItem cmtItem, CommentGroupHolder commentGroupHolder) {
                            if (cmtItem != null)
                                if (cmtItem.isLiked) {
                                    if (!TextUtils.isEmpty(cmtItem.likes)) {

                                        commentGroupHolder.likeCount.setText(like_txt + " (" + ((Integer.parseInt(cmtItem.likes)) + 1) + ")");
                                    } else {
                                        commentGroupHolder.likeCount.setText(like_txt + " (" + ((Integer.parseInt("0")) + 1) + ")");
                                    }
                                    commentGroupHolder.like.setEnabled(false);
                                    commentGroupHolder.disLike.setEnabled(true);
                                } else if (cmtItem.isDisliked) {
                                    if (!TextUtils.isEmpty(cmtItem.unlikes)) {
                                        commentGroupHolder.dislikeCount.setText(dislike_txt + " (" + ((Integer.parseInt(cmtItem.unlikes)) + 1)
                                                + ")");
                                    } else {
                                        commentGroupHolder.dislikeCount.setText(dislike_txt + " (" + ((Integer.parseInt("0")) + 1) + ")");
                                    }
                                    commentGroupHolder.disLike.setEnabled(false);
                                    commentGroupHolder.like.setEnabled(true);
                                } else {
                                    commentGroupHolder.disLike.setEnabled(true);
                                    commentGroupHolder.like.setEnabled(true);
                                }
                        }

                        @Override
                        public void onFetchChild(Reply item, CommentGroupHolder commentGroupHolder) {
                            // TODO Auto-generated method stub

                        }
                    });

            itemHolder.comment.setText(Utility.decodeString(item.comment));
            if (item.likes != null) {
                itemHolder.likeCount.setText(like_txt + " (" + Utility.decodeString(item.likes) + ")");
            } else {
                itemHolder.likeCount.setText(like_txt + " (" + "0" + ")");
            }
            if (mCommentsList.get(groupPosition).unlikes != null) {
                itemHolder.dislikeCount.setText(dislike_txt + " (" + Utility.decodeString(item.unlikes) + ")");
            } else {
                itemHolder.dislikeCount.setText(dislike_txt + " (" + "0" + ")");
            }
            itemHolder.like.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mListener.onLikeClick(groupPosition);
                }
            });
            itemHolder.disLike.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Utility.displayWIPToast(v.getContext());
                    mListener.onDisLikeClick(groupPosition);
                }
            });
        }
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * @return the mReplyPosition
     */
    public int getReplyPosition() {
        return mReplyPosition;
    }

    /**
     * @param mReplyPosition the mReplyPosition to set
     */
    public void setReplyPosition(int mReplyPosition) {
        this.mReplyPosition = mReplyPosition;
    }

    public void loadUserProfileImage(ImageView imageView, String url) {
        userImageView = imageView;
        new BitmapWorkerTask().execute(url);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        String imageUrl;

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                bitmap = Glide.
                        with(ctx).
                        load(imageUrl).
                        asBitmap().
                        into(-1, -1). // for full Width and height
                        get();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            userImageView.setImageBitmap(Utility.drawableToBitmap(new AvatarDrawable(Utility.getResizedBitmap(bitmap, 50, 50))));
        }
    }
}
