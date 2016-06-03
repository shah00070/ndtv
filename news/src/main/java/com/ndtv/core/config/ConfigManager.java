package com.ndtv.core.config;


import android.content.Context;
import android.text.TextUtils;

import com.ndtv.core.NdtvApplication;
import com.ndtv.core.common.util.PreferencesManager;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.config.model.Api;
import com.ndtv.core.config.model.Configuration;
import com.ndtv.core.config.model.Navigation;
import com.ndtv.core.config.model.Section;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.newswidget.Constants;
import com.ndtv.core.newswidget.dto.NewsWidget;
import com.ndtv.core.ui.NavigationItem;
import com.ndtv.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ndtv.core.util.LogUtils.LOGE;
import static com.ndtv.core.util.LogUtils.makeLogTag;

/**
 * Created by Srihari S Reddy on 12/11/14.
 */
public class ConfigManager {

    private static final String TAG = makeLogTag(ConfigManager.class);

    private static ConfigManager mInstance;
    private Configuration mConfiguration;
    private Map<String, String> configProps;
    private String mPushMessageText;
    private String mDefaultNav;
    public static int pageSize = ApplicationConstants.DEFAULT_PAGE_SIZE;
    public static int timeStampCap = ApplicationConstants.TIMESTAMP_CAP;
    public static int reviewCap = 0;

    private ConfigManager() {

    }

    public void setConfiguration(Configuration configuration) {
        mConfiguration = configuration;
        configProps = new ConcurrentHashMap<String, String>();
        BuildPropMap();

        pageSize = mInstance.getPageSize();
        timeStampCap = mInstance.getTimestampCap();
        reviewCap = mInstance.getProp("review_frequency", 0);
    }

    public static synchronized ConfigManager getInstance() {
        if (mInstance == null)
            mInstance = new ConfigManager();
        return mInstance;
    }

    public Configuration getConfiguration() {
        if(mConfiguration==null)
        {
            mConfiguration= PreferencesManager.getInstance(NdtvApplication.getApplication().getApplicationContext()).getConfig();
        }
        return mConfiguration;
    }

    public List<NavigationItem> getNavigationItems() {
        List<NavigationItem> menuItems = new ArrayList<NavigationItem>();

/*        Navigation navigation = new Navigation();
        navigation.title = "newsdigest";
        navigation.type = "newsdigest";
        navigation.url = "http://api.videos.ndtv.com/apis/search/mjson/client_key/apps-news-iphone-a5bb00ce1321c490ae0e0f621c83f20d/?type=featured&slug=news-in-60-secs&extra_params=filepath,category,socialshare,fullimage&private_video=1&pageSize=10&pageNumber=1&video_format=mp4&source=16";
        navigation.section = new ArrayList<Section>();
        Section section = new Section();
        section.type = "newsdigest";
        section.title = "News in 60";
        section.url = "http://api.videos.ndtv.com/apis/search/mjson/client_key/apps-news-iphone-a5bb00ce1321c490ae0e0f621c83f20d/?type=featured&slug=news-in-60-secs&extra_params=filepath,category,socialshare,fullimage&private_video=1&pageSize=10&pageNumber=1&video_format=mp4&source=16";
        navigation.section.add(section);
        navigation.menu_icon = "http://drop.ndtv.com/ndtv/apps/config/images/icons/android/niss_android.png";
        mConfiguration.navigation.add(navigation);*/

            for (Navigation n : getConfiguration().navigation) {
                menuItems.add(new NavigationItem(n.getTitle(), n.menu_icon, n.showDivider()));
            }
            return menuItems;

    }

    public Navigation getNavigation(int position) {
        /*if (position >= 0)*/
        if (null != getConfiguration() && getConfiguration().navigation.size() > position)
            return getConfiguration().navigation.get(position);
        return null;
    }

    public int getNavigationPosition(final Context context, final String navigationType) {

        if (getConfiguration() != null) {
            List<Navigation> navigationList = getConfiguration().getNavigationList();
            if (null != navigationList)
                for (int i = 0; i < navigationList.size(); i++) {
                    if (navigationList.get(i).type != null
                            && navigationList.get(i).type.equalsIgnoreCase(navigationType)) {
                        return i;
                    }
                }
        }
        return 0;
    }

    private void BuildPropMap() {
        for (Api prop : getConfiguration().customapis.api) {
            if (prop.type != null && prop.url != null)
                configProps.put(prop.type, prop.url);
        }
    }

    public String getProp(String key, String defaultValue) {
        try {
            return configProps.get(key);
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            return defaultValue;
        }
    }

    public int getProp(String key, int defaultValue) {
        try {
            return Integer.parseInt(configProps.get(key));
        } catch (Exception e) {
            LOGE(TAG, e.getMessage());
            return defaultValue;
        }
    }

    public int getPageSize() {
        return getProp("size", ApplicationConstants.DEFAULT_PAGE_SIZE);
    }

    public int getTimestampCap() {
        return getProp("timestamp_cap", ApplicationConstants.TIMESTAMP_CAP);
    }

    public String getDefaultNav() {
        return getProp("default_section", ApplicationConstants.DEFAULT_LAUNCH);
    }

    public int getDefaultSec() {
        return 0;
    }

    public String getCustomApiUrl(String type) {
        if (getConfiguration() != null && getConfiguration().customapis != null) {
            List<Api> customApiList = getConfiguration().customapis.api;
            for (Api api : customApiList)
                if (type.equals(api.type)) {
                    return api.url;
                }
        }
        return null;
    }

    public String getCustomApiStatus(String type){
        if (mConfiguration != null && mConfiguration.customapis != null) {
            List<Api> customApiList = mConfiguration.customapis.api;
            for (Api api : customApiList)
                if (type.equals(api.type)) {
                    return api.status;
                }
        }
        return null;
    }

    public String getCustomApiFrequency(String type){
        if (mConfiguration != null && mConfiguration.customapis != null) {
            List<Api> customApiList = mConfiguration.customapis.api;
            for (Api api : customApiList)
                if (type.equals(api.type)) {
                    return api.frequency;
                }
        }
        return null;
    }

    //Splash Ad Related
    public String getSplashAdLocation(String type) {
        if (getConfiguration() != null && getConfiguration().customapis != null) {
            List<Api> customApiList = getConfiguration().customapis.api;
            for (Api api : customApiList)
                if (type.equals(api.type)) {
                    return api.location;
                }
        }
        return null;
    }

    public Navigation getNavigation(int navigationPos, Context ctx) {
        if (getConfiguration() != null) {
            List<Navigation> navigationList = getConfiguration().getNavigationList();
            if (null != navigationList && navigationList.size() > navigationPos)
                return navigationList.get(navigationPos);
        }
        return null;
    }

    public Section getSection(int sectionPos, int navigationPos) {
        if (null != getConfiguration()) {
            List<Navigation> navigationList = getConfiguration().getNavigationList();
            if (navigationList != null && navigationList.size() > navigationPos) {
                Navigation navigation = navigationList.get(navigationPos);
                if (navigation != null && navigation.getSectionList() != null
                        && navigation.getSectionList().size() > sectionPos)
                    return navigationList.get(navigationPos).getSectionList().get(sectionPos);
            }
        }
        return null;
    }


    public String getSectionURL(String navigationTitle, String subSectionName) {
        if (getConfiguration() != null) {
            List<Navigation> navigationList = getConfiguration().getNavigationList();
            if (navigationList != null && navigationList.size() > 0) {
                for (Navigation navigation : navigationList) {
                    if (StringUtils.decodeString(navigation.title).equalsIgnoreCase(navigationTitle)) {
                        List<Section> sectionList = navigation.section;
                        for (Section section : sectionList) {
                            if (section.title.equalsIgnoreCase(subSectionName)) {
                                return section.getSectionUrl();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setPushNewsMessageText(String messageText) {
        mPushMessageText = messageText;
    }


    public String getPushNewsMessageText() {

        if (null != mPushMessageText)
            return mPushMessageText;
        else
            return "";
    }

    public int getSectionPositionBasedOntitle(final Context context, final String sectionTitle,
                                              final int navigationIndex) {
        Configuration configuration = getConfiguration();
        if (configuration != null) {
            List<Navigation> navigationList = configuration.getNavigationList();
            Navigation navigation = navigationList.get(navigationIndex);
            List<Section> sections = navigation.getSectionList();
            for (int index = 0; index < sections.size(); index++) {
                Section section = sections.get(index);
                if (!TextUtils.isEmpty(sectionTitle) && section.getTitle().equalsIgnoreCase(sectionTitle)) {
                    return index;
                }
            }

        }
        return -1;
    }

    public Api returnCustomApi(String type) {
        if (getConfiguration() != null && getConfiguration().customapis != null) {
            List<Api> customApiList = getConfiguration().customapis.api;
            for (Api api : customApiList)
                if (type.equals(api.type)) {
                    return api;
                }
        }
        return null;

    }

    /**
     * ********************NEWS WIDGET CODE*******************************
     */

    public int getWidgetHeight(int index, NewsWidget mWidget) {
        int height = 230;
        if (mWidget != null && index < mWidget.item.size()) {
            NewsWidget.NewsWidgetItem widget = mWidget.item.get(index);
            if (widget.extraData != null) {
                height = widget.extraData.height;
            }
        }
        return height == 0 ? 230 : height;
    }

    public String getWidgetAppLink(int index, NewsWidget mWidget) {
        if (mWidget != null && index < mWidget.item.size()) {
            NewsWidget.NewsWidgetItem widget = mWidget.item.get(index);
            if (widget.extraData != null)
                return widget.extraData.applink;
        }
        return null;
    }

    public NewsWidget.NewsWidgetItem getWidgetData(int widgetIndex, NewsWidget mWidget) {
        if (mWidget != null && widgetIndex < mWidget.item.size()) {
            return mWidget.item.get(widgetIndex);
        }
        return null;
    }

    public int getWidgetCount(NewsWidget mWidget) {
        if (mWidget != null && mWidget.item != null) {
            return mWidget.item.size();
        }
        return 0;
    }


    public static String getDeeplinkCategory(String deeplinkUrl) {
        if (!TextUtils.isEmpty(deeplinkUrl)) {
            //"ndtv://category=news&subcategory=world&id=641669"
            String[] array = deeplinkUrl.split("&subcategory=");
            array = array[0].split("category=");
            if (array.length > 1) {
                String category = array[1];
                if (!TextUtils.isEmpty(category))
                    return category;
            }
        }
        return null;
    }

    public static String getDeeplinkSubcategory(String deeplinkUrl) {
        if (!TextUtils.isEmpty(deeplinkUrl)) {
            String[] array = deeplinkUrl.split("&subcategory=");
            if (array.length > 1) {
                array = array[1].split("&id=");
                String category = array[0];
                if (!TextUtils.isEmpty(category))
                    return category;
            }
        }
        return null;
    }

    public static String getDeeplinkingId(String deeplinkUrl) {
        if (!TextUtils.isEmpty(deeplinkUrl)) {
            String[] array = deeplinkUrl.split("&id=");
            if (array.length > 1) {
                String category = array[1];
                if (!TextUtils.isEmpty(category))
                    return category;
            }
        }
        return null;
    }

    public int getNavigationIndexBasedOntitle(final String navigationTitle) {
        Configuration configuration = getConfiguration();
        if (configuration != null) {
            List<Navigation> navigationList = configuration.getNavigationList();
            if (null != navigationList)
                for (int i = 0; i < navigationList.size(); i++) {
                    if (navigationList.get(i).title != null
                            && Utility.decodeString(navigationList.get(i).title).equalsIgnoreCase(navigationTitle)) {
                        return i;
                    }
                }
        }
        return -1;
    }

    public boolean isWidgetAvilableForNavigationAndSection(int navigationIndex, int sectionIndex) {
        Api api = returnCustomApi(Constants.BREAKING_WIDGET_SECTIONS);
        if (api != null) {
            /*if (navigationIndex < mConfiguration.navigation.size()-1)
                return false;*/
            Navigation navigation = getConfiguration().navigation.get(navigationIndex);
            if (null != navigation) {
                if (navigation.section.size() > 0) {
                    Section section = navigation.section.get(sectionIndex);
                    if (widgetMap.isEmpty()) {
                        splitWidgetNavigationAndSection();
                    }
                    String sectionName = widgetMap.get(navigation.title);
                    if (sectionName != null && sectionName.equalsIgnoreCase(section.title))
                        return true;
                }
            }
        }
        return false;
    }
    public String getBreakingWidgetNavigationAndSectionName() {
        Api api = returnCustomApi(Constants.BREAKING_WIDGET_SECTIONS);
        if (api != null) {
            return api.getUrl().replaceFirst("/","");
        }
        return "";
    }
    private Map<String, String> widgetMap = new HashMap<String, String>();

    private void splitWidgetNavigationAndSection() {
        Api api = returnCustomApi(Constants.BREAKING_WIDGET_SECTIONS);
        if (api != null) {
            String widgetSections = api.url;
            String[] widgetSectionsArray = widgetSections.split("\\|");

            for (int index = 0; index < widgetSectionsArray.length; index++) {
                String subsections = widgetSectionsArray[index];
                String[] subsectionArray = subsections.split("/");
                String navigationName = subsectionArray[0];
                String sectionName = subsectionArray[1];
                widgetMap.put(navigationName, sectionName);
            }
        }
    }
    /***********************NEWS WIDGET CODE END********************************/


    /**
     * Settings code*
     */
    public boolean isEnglish(String title) {
        String pattern = "^[A-Za-z0-9. ]+$";
        if (title.matches(pattern))
            return true;
        else
            return false;
    }
}
