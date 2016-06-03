package com.ndtv.core.video.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ndtv.core.R;
import com.ndtv.core.video.dto.VideoItem;
import com.ndtv.core.video.manager.VideoManager;

import java.util.List;

/**
 * Created by laveen on 11/3/15.
 */
public class VideoPagerFragment extends Fragment {

    protected ViewPager mPager;

    protected List<VideoItem> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundleData();
    }

    protected void getBundleData() {
        Bundle bundle = getArguments();
        final String section = bundle.getString(VideosListingFragment.VIDEOS_LISTING_SECTION);
        mData = VideoManager.getInstance().getData(section);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_pager, container, false);
        initViews(view);
        return view;
    }

    protected void initViews(View view) {
        mPager = (ViewPager) view.findViewById(R.id.video_pager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setPagerAdapter();
    }

    protected void setPagerAdapter() {
        if (mData != null) {
            mPager.setAdapter(new VideoPagerAdapter(getChildFragmentManager(), mData));
        }
    }

    static class VideoPagerAdapter extends FragmentPagerAdapter {

        protected List<VideoItem> data;

        public VideoPagerAdapter(FragmentManager fm, List<VideoItem> data) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return data.size();
        }
    }

}
