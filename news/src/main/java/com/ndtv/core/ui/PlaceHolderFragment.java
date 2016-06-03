package com.ndtv.core.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ndtv.core.R;

/**
 * Created by Srihari S Reddy on 23/12/14.
 */
public class PlaceHolderFragment extends Fragment {

    public static PlaceHolderFragment newInstance(String title) {

        PlaceHolderFragment f = new PlaceHolderFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.placeholder_fragment, container, false);

        TextView title = (TextView) view.findViewById(R.id.item_title);
        title.setText(getArguments().getString("title"));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
