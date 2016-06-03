package com.ndtv.core.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.ads.utility.AdConstants;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.ui.NewsListingFragment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.ndtv.core.provider.ContentProviderUtils.IsItemRead;
import static com.ndtv.core.util.ApplicationUtils.date4App;

/**
 * Created by Srihari S Reddy on 12/01/15.
 */
public class NewsListAdapter extends CursorAdapter {

    private final NewsListingFragment.AdNativeConsumptionListener mAdConsumptionListener;
    //    private final NewsListingFragment.AdNativeConsumptionListener mAdConsumptionListener;
    Context mContext;
    private NativeContentAd mNativeContentAd;
    private int mPrevAdPosition = Integer.valueOf(ConfigManager.getInstance().getCustomApiUrl(AdConstants.NATIVE_ADS_POSITION)) - 1;
    private int mAdRecurringPos = Integer.valueOf(ConfigManager.getInstance().getCustomApiUrl(AdConstants.ADS_INMOBI_NATIVE_FREQ_CAP));
    private int mCursorPos;
    private boolean isAdAdded = false;
    ColorMatrix matrix = new ColorMatrix();
    ColorMatrixColorFilter filter;
    //Linked list to store ad objects
    private LinkedList<NativeContentAd> mNativeAdArray = new LinkedList<NativeContentAd>();
    private HashMap<Integer, NativeContentAd> mNativeAdMap;

    public NewsListAdapter(Context context, Cursor c, boolean autoQuery, NewsListingFragment.AdNativeConsumptionListener listener) {
        super(context, c, autoQuery);
        mContext = context;

        matrix.setSaturation(0);
        filter = new ColorMatrixColorFilter(matrix);
        mNativeAdMap = new HashMap<Integer, NativeContentAd>();
        mAdConsumptionListener = listener;
    }

    public void setContentAd(NativeContentAd contentAd) {
        if (mNativeAdArray != null)
            mNativeAdArray.add(contentAd);
        mNativeContentAd = contentAd;
    }

    public boolean isNativeAdEnabled() {
        String status = ConfigManager.getInstance().getCustomApiUrl(AdConstants.ADS_INMOBI_NATIVE_AD_STATUS);
        if (AdConstants.ENABLED.equalsIgnoreCase(status))
            return true;
        return false;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.news_list_item_with_ad, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        mCursorPos = cursor.getPosition();
        if (isNativeAdEnabled() && isAdSpot(cursor.getPosition(), mPrevAdPosition, mAdRecurringPos)) {
            NativeContentAd nativeContentAd = mNativeAdMap.get(mCursorPos);
            setNativeAdData(view, mCursorPos, nativeContentAd);
        } else {
            view.findViewById(R.id.native_ad_content).setVisibility(View.GONE);
        }
        setNewsData(view, context, cursor);
    }

    private void setNativeAdData(View view, int cp, NativeContentAd nativeContentAd) {
        NativeContentAdView nativeContentAdView = (NativeContentAdView) view.findViewById(R.id.native_ad_content);
        View nativeAdLayout = view.findViewById(R.id.relativeLayoutNativeAd);
        nativeAdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing - to handle issue of click on blank space within Ad redirecting to previous news item.
            }
        });

        if (nativeContentAd == null && mNativeAdArray != null) {
            nativeContentAd = mNativeAdArray.poll();
            mNativeAdMap.put(cp, nativeContentAd);
            mAdConsumptionListener.onNativeResponseConsumed(nativeContentAd);
            if (nativeContentAd != null && nativeContentAdView != null) {
                nativeContentAdView.setVisibility(View.VISIBLE);

                TextView adHeadline = (TextView) nativeContentAdView.findViewById(R.id.ad_title);
                if(adHeadline != null)
                    adHeadline.setText(nativeContentAd.getHeadline());
                nativeContentAdView.setHeadlineView(adHeadline);

                TextView adBody = (TextView) nativeContentAdView.findViewById(R.id.ad_description);
                if(adBody != null)
                    adBody.setText(nativeContentAd.getBody());
                nativeContentAdView.setBodyView(adBody);

                nativeContentAdView.setImageView(nativeContentAdView.findViewById(R.id.ad_thumbnail_img));

                List<NativeAd.Image> images = nativeContentAd.getImages();

                if (images != null && images.size() > 0) {
                    if(nativeContentAdView.getImageView() != null) {
                        ((ImageView) nativeContentAdView.getImageView())
                                .setImageDrawable(images.get(0).getDrawable());
                    }
                }

                nativeContentAdView.setNativeAd(nativeContentAd);
                /*final TextView adTitle = (TextView) view.findViewById(R.id.ad_title);
                TextView adDescription = (TextView) view.findViewById(R.id.ad_description);
                ImageView adThumb = (ImageView) view.findViewById(R.id.ad_thumbnail_img);

                adTitle.setText(nativeContentAd.getHeadline());
                adDescription.setText(nativeContentAd.getBody());
                adThumb.setImageDrawable(nativeContentAd.getImage());

                final NativeContentAd finalNativeContentAd = nativeContentAd;

                //Added click for entire Ad item.
                nativeAdLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //To avoid clicking of entire Ad item.
                        finalNativeContentAd.performClick(NativeContentAd.ASSET_HEADLINE);
                    }
                });

                //Track impression for native ad
                nativeContentAd.recordImpression();*/
            }
        }
    }

    private void setNewsData(View view, Context context, Cursor cursor) {
        if (!TextUtils.isEmpty(cursor.getString(13)) && cursor.getString(13).equalsIgnoreCase("collage")) {
            cursor.moveToNext();
        }

        view.findViewById(R.id.news_layout).setVisibility(View.VISIBLE);
        TextView newsTitle = (TextView) view.findViewById(R.id.news_item_title);
        TextView newsSubline = (TextView) view.findViewById(R.id.news_item_subline);
        ImageView newsThumb = (ImageView) view.findViewById(R.id.news_item_thumbnail);

        newsTitle.setText(Html.fromHtml(cursor.getString(2)));
        if (TextUtils.isEmpty(cursor.getString(7))) {
            newsSubline.setText(date4App(cursor.getString(3), context));
            newsSubline.setTextColor(mContext.getResources().getColor(R.color.news_listing_dateline));
        } else {
            newsSubline.setText(cursor.getString(7));
            newsSubline.setTextColor(Color.parseColor(cursor.getString(8)));
        }
        Glide.with(context).load(cursor.getString(4)).placeholder(R.drawable.place_holder).into(newsThumb);


        if (IsItemRead(mContext, cursor.getString(1))) {
            newsTitle.setTextColor(mContext.getResources().getColor(R.color.body_text_3));
            newsThumb.setColorFilter(filter);
            newsSubline.setTextColor(mContext.getResources().getColor(R.color.body_text_3));
        } else {
            newsTitle.setTextColor(mContext.getResources().getColor(R.color.body_text_1));
            newsThumb.setColorFilter(null);
        }
    }

    public static boolean isAdSpot(int cp, int fp, int rp) {

        if (cp - fp < 0)
            return false;

        if ((cp - fp) % rp == 0) {
            return true;
        }

        return false;
    }
}
