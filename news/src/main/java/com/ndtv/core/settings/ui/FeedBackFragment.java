package com.ndtv.core.settings.ui;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ndtv.core.R;
import com.ndtv.core.ads.ui.BannerAdFragment;
import com.ndtv.core.common.util.SplashAdManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.ui.widgets.BaseFragment;

import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by sangeetha on 23/2/15.
 */
public class FeedBackFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "SettingsFragment " + makeLogTag(FeedBackFragment.class);
    private EditText mFeedbackTxt;
    private Button mSubmitBtn;
    private final String SHARE_INTENT_TYPE = "text/message";
    private final String NO_NETWORK_DIALOG = "noNetworkDlg";
    private String mSectionTitle;
    private int mNavigationPosition;
    private String navigation;
    private BannerAdFragment.AdListener mAdUpdateListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAdUpdateListener = (BannerAdFragment.AdListener) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractArguments();
        if(mAdUpdateListener != null)
            mAdUpdateListener.hideIMBannerAd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.feedback_layout, container, false);
        initViews(view);

        initListeners();
        return view;
    }

    private void initViews(View view) {
        mSubmitBtn = (Button) view.findViewById(R.id.submit_feedback_btn);
        mFeedbackTxt = (EditText) view.findViewById(R.id.feedback_txt);
    }

    private void extractArguments() {
        if (null != getArguments()) {
            mSectionTitle = getArguments().getString(SettingsFragment.SettingsConstants.SECTION_TITLE);
            mNavigationPosition = getArguments().getInt(ApplicationConstants.BundleKeys.NAVIGATION_POS);
        }
        navigation = ConfigManager.getInstance().getConfiguration().getNavTilte(mNavigationPosition);
    }

    private void initListeners() {
        mSubmitBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submit_feedback_btn && null != mFeedbackTxt) {

            String feedback = mFeedbackTxt.getText().toString();
            if (!TextUtils.isEmpty(feedback)) {
                //to avoid splash ads
                if (SplashAdManager.getSplashAdMngrInstance(getActivity()) != null) {
                    SplashAdManager.getSplashAdMngrInstance(getActivity()).signInBtnClicked(true);
                }
                if (Utility.isInternetOn(getActivity())) {
                    postFeedback(feedback);
                } else
                    showNoNetworkDialog();
            }
        }
    }

    public void postFeedback(String feedBack) {
        String feedbackEmail = ConfigManager.getInstance().getCustomApiUrl(ApplicationConstants.CustomApiType.FEEDBACK_EMAIL_API);
        if (!TextUtils.isEmpty(feedbackEmail)) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType(SHARE_INTENT_TYPE);
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{feedbackEmail});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
            if (null != getActivity())
                intent.putExtra(android.content.Intent.EXTRA_TEXT,
                        Utility.getFeedbackExtraSubject(getActivity(), feedBack));
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.sending_mail)));
                //Fix for the feedback text still shows in the box.
                mFeedbackTxt.setText("");
            } catch (ActivityNotFoundException e) {
                if (getActivity() != null)
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.feed_back_alert_msg),
                            Toast.LENGTH_LONG).show();
                e.printStackTrace();


            }
        }
    }

    private void showNoNetworkDialog() {
        NoNetworkDialog dialog = (NoNetworkDialog) getActivity().getSupportFragmentManager().findFragmentByTag(NO_NETWORK_DIALOG);

        if (dialog != null) {
            dialog.dismissAllowingStateLoss();
        }

        dialog = new NoNetworkDialog();
        if (dialog != null && !dialog.isVisible()) {
            dialog.show(getActivity().getSupportFragmentManager(), NO_NETWORK_DIALOG);
        }
    }

    @Override
    public void setScreenName() {
        setScreenName(navigation + " - " + mSectionTitle);
    }
}
