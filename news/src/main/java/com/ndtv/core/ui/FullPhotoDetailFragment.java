package com.ndtv.core.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.ads.formats.NativeAd;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.config.model.Photos;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.deeplinking.io.DeepLinkingManager;
import com.ndtv.core.newswidget.NewsWidgetManager;
import com.ndtv.core.ui.widgets.BaseFragment;
import com.ndtv.core.util.BitmapCache;
import com.ndtv.core.util.TouchImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FullPhotoDetailFragment extends BaseFragment implements ApplicationConstants.PreferenceKeys {

    private static final String TAG = "Fullscreen Photo";
    private int position;
    private boolean isFullScreen;
    private List<Photos> photolist;
    private ProgressBar progressBar;
    private BannerAdFragment.AdListener mAdUpdateListener;
    private BaseActivity mActivity;

    public static FullPhotoDetailFragment newInstance(List<Photos> photolist, int pos) {

        FullPhotoDetailFragment photoFrgment = new FullPhotoDetailFragment();
        photoFrgment.photolist = photolist;
        photoFrgment.isFullScreen = true;
        photoFrgment.position = pos;
        return photoFrgment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActivity) {
            mActivity = (BaseActivity) activity;
            mActivity.getSupportActionBar().hide();
        }

        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAdUpdateListener.hideIMBannerAd();

        if (activity != null)
            PreferencesManager.getInstance(activity).setIsPhotoFullscreen(true);
    }

    @Override
    public void setScreenName() {
        setScreenName(TAG + " - " + (position + 1) + " " + photolist.get(position).getTitle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.full_photo_fragment, container, false);
        TouchImageView fullImageView = (TouchImageView) view.findViewById(R.id.fullPhoto);
        RelativeLayout mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        if (photolist.get(position) != null && photolist.get(position).isAdPage) {
            if(photolist.get(position).nativeContentAd != null) {
                List<NativeAd.Image> images = photolist.get(position).nativeContentAd.getImages();
                fullImageView.setImageDrawable(images.get(0).getDrawable());
            }
            progressBar.setVisibility(View.GONE);
        } else {
            loadBitmap("FULL_IMAGE" + photolist.get(position).getId(), fullImageView);
            //Glide.with(getActivity()).load(photoDetail.getFullimage()).placeholder(R.drawable.place_holder).into(fullImageView);
        }

        fullImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This is do handle photos in case of Deep Linking from Twitter,etc.
                if (DeepLinkingManager.getInstance() != null) {
                    if (DeepLinkingManager.getInstance().isFromDeepLink())
                        DeepLinkingManager.getInstance().saveItemPos(position);
                }
                // this to handle photo from news widget
                if (NewsWidgetManager.getInstance() != null) {
                    if (NewsWidgetManager.getInstance().getMIsFromNewsWidget()) {
                        NewsWidgetManager.getInstance().setmFullPhotoClickPosition(position);
                    }
                }

                if (mActivity != null)
                    mActivity.getSupportFragmentManager().popBackStack();
            }
        });

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This is do handle photos in case of Deep Linking from Twitter,etc.
                if (DeepLinkingManager.getInstance() != null) {
                    if (DeepLinkingManager.getInstance().isFromDeepLink())
                        DeepLinkingManager.getInstance().saveItemPos(position);
                }
                if (NewsWidgetManager.getInstance() != null) {
                    if (NewsWidgetManager.getInstance().getMIsFromNewsWidget()) {
                        NewsWidgetManager.getInstance().setmFullPhotoClickPosition(position);
                    }
                }
                if (mActivity != null)
                    mActivity.getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        // getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().invalidateOptionsMenu();
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).getSupportActionBar().show();

        if (getActivity() != null)
            PreferencesManager.getInstance(getActivity()).setIsPhotoFullscreen(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NewsWidgetManager.getInstance() != null) {
            if (getUserVisibleHint()) {
                NewsWidgetManager.getInstance().setmFullPhotoClickPosition(position);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    ///////////// Image Cache ////////

    public void loadBitmap(String resKey, ImageView mImageView) {
        final Bitmap bitmap = BitmapCache.getInstance((getActivity()).getSupportFragmentManager()).getBitmap(resKey);

        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
        } else {
            // Glide.with(getActivity()).load(photolist.get(position).getFullimage()).placeholder(R.drawable.place_holder).into(mImageView);
            Glide.with(getActivity())
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
                json.put("key", resKey);
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
                        with(getActivity()).
                        load(imageDetail.get("url").toString()).
                        asBitmap().
                        into(-1, -1). // For full width height
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
