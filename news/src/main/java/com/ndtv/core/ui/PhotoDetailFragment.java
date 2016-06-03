package com.ndtv.core.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.ndtv.core.R;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.model.Photos;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.deeplinking.io.DeepLinkingManager;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.util.BitmapCache;
import com.ndtv.core.util.TouchImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PhotoDetailFragment extends BaseFragment implements ApplicationConstants.PreferenceKeys {

    private static final String TAG = "Gallery Image";


    private String mTitle;
    private String mStoryImage;
    private String mContentUrl;
    protected int position;
    protected int totalCount;
    private List<Photos> photolist;
    private PhotoDetailFragment mPhotoDetailFragment;
    private int mNavigationPos;
    private int mSectionPos;
    private String mSection;
    private BaseActivity mActivity;
    private ProgressBar progressBar;

    public static PhotoDetailFragment newInstance(List<Photos> photolist, int pos, int count, int navigationPos, int sectionPos, String section) {

        PhotoDetailFragment photoFrgment = new PhotoDetailFragment();
        photoFrgment.photolist = photolist;
        photoFrgment.position = pos;
        photoFrgment.mSection = section;
        photoFrgment.totalCount = count;
        photoFrgment.mNavigationPos = navigationPos;
        photoFrgment.mSectionPos = sectionPos;
        return photoFrgment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoDetailFragment = this;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActivity) {
            mActivity = (BaseActivity) activity;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = null;
        if (photolist.get(position) != null && !photolist.get(position).isAdPage) {
            view = inflater.inflate(R.layout.photo_item_fragment, container, false);
            final TextView photoTitle = (TextView) view.findViewById(R.id.photoTitle);
            final TextView photoNumbr = (TextView) view.findViewById(R.id.photoNumber);
            TouchImageView thumbImage = (TouchImageView) view.findViewById(R.id.thumbImage);
            final TextView photoDescription = (TextView) view.findViewById(R.id.photo_description);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            //  ImageLoader imageLoader = VolleyRequestQueue.getInstance(getActivity()).getImageLoader();

            if (photolist.get(position).getTitle().toString().length() > 0)
                photoTitle.setText(Html.fromHtml(Utility.decodeString(photolist.get(position).getTitle())).toString());

            String photoNumStr = "PHOTO " + (position + 1) + " of " + totalCount;
            photoNumbr.setText(photoNumStr);

            if (photolist.get(position).getDescription().toString().length() > 0)
                photoDescription.setText(Html.fromHtml(Utility.decodeString(photolist.get(position).getDescription())).toString());

            boolean isExpanded = PreferencesManager.getInstance(getActivity()).getIsExpanded(PreferencesManager.IS_EXPANDED);
            if (isExpanded) {
                photoDescription.setMaxLines(Integer.MAX_VALUE);
                photoDescription.setEllipsize(null);
            } else {
                photoDescription.setMaxLines(3);
                photoDescription.setEllipsize(TextUtils.TruncateAt.END);
            }
            // thumbImage.setDefaultImageResId(R.drawable.place_photos_large_black);
            /// NetworkImageView.class.cast(thumbImage).setImageUrl(photoDetail.getThumbimage(), imageLoader);

            loadBitmap("FULL_IMAGE" + photolist.get(position).getId(), thumbImage);
            //Glide.with(getActivity()).load(photoDetail.getThumbimage()).placeholder(R.drawable.place_holder).into(thumbImage);

            thumbImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.addToBackStack(mPhotoDetailFragment.getClass().getName());
                    if (DeepLinkingManager.getInstance() != null && DeepLinkingManager.getInstance().isFromDeepLink())
                        fragmentTransaction.replace(R.id.deep_link_frame_body, FullPhotoFragment.newInstance(position, photolist, mSection)).commit();
                    else
                        fragmentTransaction.replace(R.id.container, FullPhotoFragment.newInstance(position, photolist, mSection)).commit();
                }
            });

            photoDescription.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean isExpanded = PreferencesManager.getInstance(getActivity()).getIsExpanded(PreferencesManager.IS_EXPANDED);
                    if (!isExpanded) {
                        photoDescription.setMaxLines(Integer.MAX_VALUE);
                        photoDescription.setEllipsize(null);
                        PreferencesManager.getInstance(getActivity()).setIsExpanded(PreferencesManager.IS_EXPANDED, true);
                    } else {
                        photoDescription.setMaxLines(3);
                        photoDescription.setEllipsize(TextUtils.TruncateAt.END);
                        PreferencesManager.getInstance(getActivity()).setIsExpanded(PreferencesManager.IS_EXPANDED, false);
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.photo_detail_native_ad_layout, container, false);
            TextView adTitle = (TextView) view.findViewById(R.id.adTitle);
            final TextView adDescription = (TextView) view.findViewById(R.id.adDescription);
            ImageView adImage = (ImageView) view.findViewById(R.id.adThumbImage);

            if (photolist.get(position) != null && photolist.get(position).nativeContentAd != null) {
                if (adTitle != null)
                    adTitle.setText(photolist.get(position).nativeContentAd.getHeadline());
                ((NativeContentAdView) view).setHeadlineView(adTitle);

                if (adDescription != null)
                    adDescription.setText(photolist.get(position).nativeContentAd.getBody());
                ((NativeContentAdView) view).setBodyView(adDescription);

                List<NativeAd.Image> images = photolist.get(position).nativeContentAd.getImages();
                if (images != null && images.size() > 0) {
                    if (adImage != null)
                        adImage.setImageDrawable(images.get(0).getDrawable());
                    ((NativeContentAdView) view).setImageView(adImage);
                }
//                adImage.setImageDrawable(photolist.get(position).nativeContentAd.getImage());

                boolean isExpanded = PreferencesManager.getInstance(getActivity()).getIsExpanded(PreferencesManager.IS_EXPANDED);
                if (isExpanded) {
                    adDescription.setMaxLines(Integer.MAX_VALUE);
                    adDescription.setEllipsize(null);
                } else {
                    adDescription.setMaxLines(3);
                    adDescription.setEllipsize(TextUtils.TruncateAt.END);
                }

                adDescription.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
//                        if (isEnableExpand) {
//                            isEnableExpand = false;
//                            adDescription.setMaxLines(Integer.MAX_VALUE);
//                            adDescription.setEllipsize(null);
//
//                        } else {
//                            isEnableExpand = true;
//                            adDescription.setMaxLines(3);
//                            adDescription.setEllipsize(TextUtils.TruncateAt.END);
//                        }

                        boolean isExpanded = PreferencesManager.getInstance(getActivity()).getIsExpanded(PreferencesManager.IS_EXPANDED);
                        if (!isExpanded) {
                            adDescription.setMaxLines(Integer.MAX_VALUE);
                            adDescription.setEllipsize(null);
                            PreferencesManager.getInstance(getActivity()).setIsExpanded(PreferencesManager.IS_EXPANDED, true);
                        } else {
                            adDescription.setMaxLines(3);
                            adDescription.setEllipsize(TextUtils.TruncateAt.END);
                            PreferencesManager.getInstance(getActivity()).setIsExpanded(PreferencesManager.IS_EXPANDED, false);
                        }

                    }
                });

                ((NativeContentAdView) view).setNativeAd((photolist.get(position).nativeContentAd));

                //Track impression for native ad
//                photolist.get(position).nativeContentAd.recordImpression();
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //  isEnableExpand = true;
        /*if (mHideActionToolBarInterface != null) {
            mHideActionToolBarInterface.showActionBarShareIcon();
        }*/
    }

    @Override
    public void setScreenName() {
        setScreenName(TAG + " - " + photolist.get(position).getId() + " " + photolist.get(position).getTitle());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mActivity instanceof BaseActivity) {
            mActivity.showOverflowMenu(false);
        }
        DeepLinkingManager.getInstance().setFromDeepLink(false);
    }

    ///////////// Image Cache //////////////////

    public void loadBitmap(String resKey, ImageView mImageView) {
        final String imageKey = resKey;

        final Bitmap bitmap = BitmapCache.getInstance((getActivity()).getSupportFragmentManager()).getBitmap(resKey);

        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
        } else {
            // Glide.with(getActivity()).load(photolist.get(position).getFullimage()).placeholder(R.drawable.place_holder).into(mImageView);
            Glide.with(mActivity)
                    .load(photolist.get(position).getFullimage())
                    .placeholder(R.drawable.place_holder_black_new)
                    .into(new GlideDrawableImageViewTarget(mImageView) {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            JSONObject json = new JSONObject();
            try {
                json.put("key", imageKey);
                json.put("url", photolist.get(position).getFullimage());
                new BitmapWorkerTask().execute(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class BitmapWorkerTask extends AsyncTask<JSONObject, Void, Bitmap> {
        JSONObject imageDetail;

        @Override
        protected Bitmap doInBackground(JSONObject... params) {
            imageDetail = params[0];
            Bitmap bitmap = null;
            try {
                bitmap = Glide.
                        with(mActivity).
                        load(imageDetail.get("url").toString()).
                        asBitmap().
                        into(-1, -1). // for full Width and height
                        get();
                if (bitmap != null)
                    BitmapCache.getInstance((getActivity()).getSupportFragmentManager()).addBitmapToCache(imageDetail.get("key").toString(), bitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }
    }

}
