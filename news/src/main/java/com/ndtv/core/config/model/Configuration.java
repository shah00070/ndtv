package com.ndtv.core.config.model;

import android.content.Context;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.ndtv.core.R;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.ConfigManager;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.settings.ui.SettingsFragment;

import java.util.List;

/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public class Configuration {

    public List<Navigation> navigation;
    public CustomApis customapis;


    public List<Navigation> getNavigationList() {
        return navigation;
    }

    public List<Api> getCustomApiList() {
        return customapis.api;
    }

    public void cleanNavigation(Context context) {
        String customPushStatus = getCustomApi(ApplicationConstants.CustomApiType.ENABLE_CUSTOMIZED_PUSH);
        if (!TextUtils.isEmpty(customPushStatus) && ApplicationConstants.Status.ENABLED.equalsIgnoreCase(customPushStatus)) {
            enableCustomizedPush(context);
        }
    }


    public String getCustomApi(String type) {
        List<Api> customApiList = getCustomApiList();
        for (Api api : customApiList)

            if (type.equals(api.type)) {
                return api.url;
            }
        return null;

    }

    public int getNavIndex(String navTitle) {
        int i = 0;

        try {
            for (Navigation n : navigation) {
                if (n.title.equalsIgnoreCase(navTitle))
                    return i;
                else if(Utility.decodeString(n.title).equalsIgnoreCase(navTitle))
                    return i;
                i++;
            }

        } catch (Exception E) {
            Crashlytics.log(E.getMessage());
        } catch (StackOverflowError E) {
            return 0;
        }
        return getDefaultNavPosEx();
    }

    public int getDefaultNavPosEx() {
        String defStr = ConfigManager.getInstance().getDefaultNav();
        String[] defNav = defStr.split("/", 2);
        return ConfigManager.getInstance().getConfiguration().getNavIndex(defNav[0]);
    }


    public String getNavTilte(int pos) {
        if (navigation == null) return "";
        return navigation.get(pos).getTitle();
    }

    public int getSec(String navTitle, String secTitle) {

        Navigation currentNav = navigation.get(getNavIndex(navTitle));
        int i = 0;
        try {
            for (Section s : currentNav.section) {
                if (s.title.equalsIgnoreCase(secTitle))
                    return i;
                i++;
            }
        } catch (Exception E) {
            Crashlytics.log(E.getMessage());
        }
        return 0;
    }


    private void enableCustomizedPush(Context context) {
        if (navigation != null) {
            for (int i = 0; i < navigation.size(); i++) {
                Navigation navigationList = navigation.get(i);

                if (ApplicationConstants.NavigationType.SETTINGS.equalsIgnoreCase(navigationList.type)) {

                    Section section = new Section();
                    section.type = SettingsFragment.SettingsConstants.NOTIFICATIONS;
                    section.title = context.getString(R.string.customized_push_title);
                    navigationList.section.add(0, section);
                    break;
                }
            }
        }
    }
}