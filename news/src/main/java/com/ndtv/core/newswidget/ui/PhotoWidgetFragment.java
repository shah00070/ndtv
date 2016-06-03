package com.ndtv.core.newswidget.ui;

import com.ndtv.core.R;
import com.ndtv.core.newswidget.ui.custom.TextWidget;

/**
 * Created by Harisha B on 23/2/15.
 */
public class PhotoWidgetFragment extends TextWidget {
    @Override
    public void setWidgetIcon() {
        mWidgetType.setImageResource(R.drawable.widget_photo);
    }
}
