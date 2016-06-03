package com.ndtv.core.newswidget.ui.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.NdtvApplication;
import com.ndtv.core.R;
import com.ndtv.core.newswidget.ui.BreakingNewsWidget;

/**
 * Created by laveen on 8/12/14.
 */
public class TextWidget extends BreakingNewsWidget {

    private NetworkImageView mWidgetImage;
    private TextView mWidgetText;
    protected ImageView mWidgetType;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wiget_item, container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mWidgetImage = (NetworkImageView) view.findViewById(R.id.news_widget_image);
        mWidgetText = (TextView) view.findViewById(R.id.news_widget_text);
        mWidgetType = (ImageView) view.findViewById(R.id.widget_type_indicator);
    }

    @Override
    public void loadWidgetData() {
        loadImage();
        loadTextData();
        setWidgetIcon();
    }

    protected void loadImage() {
        //  mWidgetImage.setDefaultImageResId(R.drawable.place_holder_on_the_show_white);
        mWidgetImage.setDefaultImageResId(R.drawable.place_holder);
        if (!TextUtils.isEmpty(mWidgetData.image)) {
            mWidgetImage.setImageUrl(mWidgetData.image, NdtvApplication.getApplication().getmImageLoader());
        }
    }

    protected void loadTextData() {
        if (!TextUtils.isEmpty(mWidgetData.title))
            mWidgetText.setText(Html.fromHtml(mWidgetData.getTitle()));
    }

    public void setWidgetIcon() {

    }
}
