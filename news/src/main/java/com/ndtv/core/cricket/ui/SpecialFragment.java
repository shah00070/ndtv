package com.ndtv.core.cricket.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.ndtv.core.R;
import com.ndtv.core.common.util.GsonObjectRequest;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.config.model.TabItem;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.cricket.dto.CricketContentDetail;
import com.ndtv.core.io.VolleyRequestQueue;
import com.ndtv.core.ui.widgets.BaseFragment;

import java.util.List;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Chandan kumar on 2/16/2015.
 */
public class SpecialFragment extends BaseFragment implements ApplicationConstants.NavigationType, ApplicationConstants.BundleKeys, Response.Listener<CricketContentDetail> {

    private static final String TAG = makeLogTag(SpecialFragment.class);
    public static final String DISCLAIMER_URL = "disclaimer_url";

    private int FIRST_POS;
    protected String title;
    private static final int DEFAULT_TAB_POS = 999;
    protected int mSectionPosiiton;
    protected int mNavigationPosition, mSubTabPos;
    private RadioGroup tabsGroup;
    HorizontalScrollView mScrollView;
    private String mNavigationTitle, mTabName;
    protected int mCheckedId;
    protected List<TabItem> tabList;
    protected TextView mDesclaimerTop;
    protected TextView mDesclaimerBottom;
    private CricketContentDetail mCricketContentDetail;
    private boolean isCricketPage;
    private String navigation;

    public static SpecialFragment newInstance(List<TabItem> tabList, int position, String title, int navigationPos) {
        SpecialFragment specialFragment = new SpecialFragment();
        specialFragment.mSectionPosiiton = position;
        specialFragment.title = title;
        specialFragment.isCricketPage = false;
        specialFragment.tabList = tabList;
        specialFragment.mNavigationPosition = navigationPos;
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPos);
        specialFragment.setArguments(bundle);
        return specialFragment;
    }

    public static SpecialFragment newInstance(List<TabItem> tabList, int position, String title, int navigationPos, String desclaimerUrl) {
        SpecialFragment specialFragment = new SpecialFragment();
        specialFragment.mSectionPosiiton = position;
        specialFragment.title = title;
        specialFragment.isCricketPage = true;
        specialFragment.tabList = tabList;
        specialFragment.mNavigationPosition = navigationPos;
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("section", title);
        bundle.putInt(ApplicationConstants.BundleKeys.NAVIGATION_POS, navigationPos);
        bundle.putString(DISCLAIMER_URL, desclaimerUrl);
        specialFragment.setArguments(bundle);
        return specialFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setScreenName() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.special_tab_fragment, null, false);
        initViews(view);
        //createTabs(view, inflater);
        // addWebViewFragment(FIRST_POS);
        return view;
    }

    protected void initViews(View view) {
        mDesclaimerTop = (TextView) view.findViewById(R.id.disclaimer_top);
        mDesclaimerBottom = (TextView) view.findViewById(R.id.disclaimer_bottom);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isCricketPage) {
            downloadDisclaimer();
        } else {
            createTabsAndAddWebView();
        }
    }

    protected void downloadDisclaimer() {
        Bundle bundle = getArguments();
        String url = bundle.getString(DISCLAIMER_URL);
        if (TextUtils.isEmpty(url)) {
            createTabsAndAddWebView();
        } else {
            GsonObjectRequest request = new GsonObjectRequest(Request.Method.GET, url, CricketContentDetail.class, this, null);
            VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);
        }
    }

    @Override
    public void onResponse(CricketContentDetail cricketContentDetail) {
        mCricketContentDetail = cricketContentDetail;
        createTabsAndAddWebView();
    }

    public void createTabsAndAddWebView() {
        try {
            createTabs(getView(), getActivity().getLayoutInflater());
            addWebViewFragment(FIRST_POS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void createTabs(View view, LayoutInflater inflater) {
        mScrollView = (HorizontalScrollView) view.findViewById(R.id.tab_scroll);

        int tabCount = 0;
        if (getActivity() != null) {
            tabsGroup = (RadioGroup) view.findViewById(R.id.tabs);
            tabsGroup.setOnCheckedChangeListener(mTabChangeListener);
            Section section = ConfigManager.getInstance().getSection(mSectionPosiiton, mNavigationPosition);

            if (section != null && section.getTabList() != null)
                tabCount = section.getTabList().size();
            for (int i = 0; i < tabCount; i++) {
                // Radiobutton in xml is inflated because there is no way to
                // make button drawable null if defined programmaticaly.
                RadioButton button = (RadioButton) inflater.inflate(R.layout.radio_button, null, false);
                button.setTextColor(getActivity().getResources().getColor(R.color.black));
                button.setText(ConfigManager.getInstance().getSection(mSectionPosiiton, mNavigationPosition).getTabList().get(i).getTitle());

                button.setId(i);
                button.setBackgroundResource(R.drawable.tab_btn_selector);
                tabsGroup.addView(button);
                if (i == FIRST_POS) {
                    button.setChecked(true);
                }
            }
        }
    }

    /**
     * @param checkedId
     */
    protected void addWebViewFragment(int checkedId) {
        String typeStr = null;
        Section section = ConfigManager.getInstance().getSection(mSectionPosiiton, mNavigationPosition);
        if (section != null && section.getTabList() != null
                && section.getTabList().size() > 0)
            typeStr = section.getTabList().get(checkedId).type;

        if (PAGE_TYPE.equalsIgnoreCase(typeStr)) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            String navigationUrl = section.getTabList().get(checkedId).getUrl();
            WebViewFragment pageFragment = WebViewFragment.newInstance(checkedId, navigationUrl, mSectionPosiiton, title, mNavigationPosition);
//            Bundle bundle = new Bundle();
//            bundle.putInt(SECTION_POSITION, mSectionPosiiton);
//            bundle.putInt(NAVIGATION_POS, mNavigationPosition);
//            bundle.putString(URL_STRING, section.getTabList().get(checkedId).getUrl());
//            pageFragment.setArguments(bundle);
            transaction.replace(R.id.sub_body, pageFragment).commitAllowingStateLoss();
        }
    }


    RadioGroup.OnCheckedChangeListener mTabChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            final RadioButton tabView = (RadioButton) group.getChildAt(checkedId);
            if (tabView != null) {
                //  mTabName = tabView.getText().toString();
                onTabChecked(tabView.getText().toString());
                addWebViewFragment(checkedId);
                mCheckedId = checkedId;
                final int scrollPos = tabView.getLeft() - (Utility.getScreenWidth(getActivity()) - tabView.getWidth())
                        / 2;
                mScrollView.scrollTo(scrollPos, 0);
            }
        }
    };

    public void onTabChecked(String tabText) {
        mTabName = tabText;
        setDisclaimerText(tabText);
    }

    public void setDisclaimerText(String tabName) {
        if (mCricketContentDetail != null && mCricketContentDetail.disclaimer != null) {
            String desclaimerString = null;
            String desclaimerPosition = null;
            for (CricketContentDetail.Disclaimer disclaimer : mCricketContentDetail.disclaimer) {
                if (!TextUtils.isEmpty(disclaimer.getSubsection()) && disclaimer.getSubsection().equalsIgnoreCase(tabName)) {
                    desclaimerString = disclaimer.getMessage();
                    desclaimerPosition = disclaimer.getPosition();
                    break;
                }
            }

            //if desclaimer is empty fetch it from section name
            if (TextUtils.isEmpty(desclaimerString)) {
                Section section = ConfigManager.getInstance().getSection(mSectionPosiiton, mNavigationPosition);
                if (section != null)
                    for (CricketContentDetail.Disclaimer disclaimer : mCricketContentDetail.disclaimer) {
                        if (!TextUtils.isEmpty(disclaimer.getName()) && disclaimer.getName().equalsIgnoreCase(section.title)) {
                            desclaimerString = disclaimer.getMessage();
                            desclaimerPosition = disclaimer.getPosition();
                            break;
                        }
                    }
            }

            if (TextUtils.isEmpty(desclaimerString)) {
                mDesclaimerTop.setVisibility(View.GONE);
                mDesclaimerBottom.setVisibility(View.GONE);
            } else {

                if (TextUtils.isEmpty(desclaimerPosition) || desclaimerPosition.equalsIgnoreCase("top")) {
                    mDesclaimerTop.setText(desclaimerString);
                    mDesclaimerTop.setVisibility(View.VISIBLE);
                    mDesclaimerBottom.setVisibility(View.GONE);
                } else {
                    mDesclaimerBottom.setText(desclaimerString);
                    mDesclaimerTop.setVisibility(View.GONE);
                    mDesclaimerBottom.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void refresh() {
        if (getActivity() != null)
            addWebViewFragment(mCheckedId);
    }

}
