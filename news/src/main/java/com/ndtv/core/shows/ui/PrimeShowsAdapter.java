/**
 * @author Chandan kumar
 *
 */

package com.ndtv.core.shows.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ndtv.core.R;
import com.ndtv.core.common.util.BaseArrayAdapter;
import com.ndtv.core.common.util.Utility;
import com.ndtv.core.constants.ApplicationConstants;
import com.ndtv.core.shows.dto.PrimeShowItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * @author Chandan kumar
 */
public class PrimeShowsAdapter extends BaseArrayAdapter<PrimeShowItem> implements SectionIndexer {

    private LayoutInflater mInflater;
    private ArrayList<String> mSections;
    private List<PrimeShowItem> showsList;
    private ArrayList<Integer> headerTextPosList;
    private HashMap<String, Integer> alphaIndexer;
    private String[] sectionsArray;

    public PrimeShowsAdapter(Context context, List<PrimeShowItem> showsList) {
        super(context, 0, showsList);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showsList = showsList;
        mSections = new ArrayList<String>();
        headerTextPosList = new ArrayList<>();
        for (int i = 0; i < showsList.size(); i++) {
            char firstChar = Utility.decodeString(showsList.get(i).name).charAt(0);
            if (i == 0 && Character.isDigit(firstChar)) {
                headerTextPosList.add(i);
            }
            if (!Character.isDigit(firstChar))
                if (!mSections.contains(Character.toString(firstChar))) {
                    mSections.add(Character.toString(firstChar));
                    headerTextPosList.add(i);
                }
        }

        alphaIndexer = new HashMap<String, Integer>();
        int size = showsList.size();

        for (int x = 0; x < size; x++) {
            String s = showsList.get(x).name;
            // get the first letter of the store
            String ch = s.substring(0, 1);
            // convert to uppercase otherwise lowercase a -z will be sorted
            // after upper A-Z
            ch = ch.toUpperCase();
            // put only if the key does not exist
            if (!alphaIndexer.containsKey(ch))
                alphaIndexer.put(ch, x);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(
                sectionLetters);
        Collections.sort(sectionList);
        sectionsArray = new String[sectionList.size()];
        sectionsArray = sectionList.toArray(sectionsArray);
    }


    class Holder {
        TextView title;
        ImageView imageview;
        TextView headerText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.prime_show_list_item, parent, false);
            holder.headerText = (TextView) convertView.findViewById(R.id.header_text);
            holder.title = (TextView) convertView.findViewById(R.id.shows_title);
            holder.imageview = (ImageView) convertView.findViewById(R.id.shows_image);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        PrimeShowItem item = showsList.get(position);
        if (item != null)
            setItemData(holder, item);

        char charFirst = item.name.toCharArray()[0];
        if (headerTextPosList.contains(position)) {
            if (Character.isDigit(charFirst))
                holder.headerText.setText(ApplicationConstants.HASH_SYMBOL);
            else
                holder.headerText.setText(Character.toString(charFirst));
        } else {
            holder.headerText.setText("");
        }

        return convertView;
    }

    /**
     * @param holder
     * @param item
     */
    private void setItemData(Holder holder, PrimeShowItem item) {

        holder.title.setText(Html.fromHtml((Utility.decodeString(item.name))).toString());
        if (item.image != null && holder.imageview != null) {
            NetworkImageView imageView = NetworkImageView.class.cast(holder.imageview);
            imageView.setDefaultImageResId(R.drawable.place_holder_on_the_show_white);
            imageView.setImageUrl(item.image, imageLoader);
        }
    }

    @Override
    public int getPositionForSection(int section) {
        return alphaIndexer.get(sectionsArray[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return sectionsArray;
    }
}
