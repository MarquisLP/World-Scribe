package com.averi.worldscribe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.averi.worldscribe.R;
import com.averi.worldscribe.utilities.AttributeGetter;

/**
 * Created by mark on 15/08/16.
 * Adapter for the Select App Theme spinner.
 */
public class AppThemeArrayAdapter extends ArrayAdapter<Integer> {
    private LayoutInflater inflater;
    private Integer[] themes;

    /**
     * Initialize a new AppThemeArrayAdapter.
     * @param context The Context using this Adapter.
     * @param themes The IDs of the theme resources this Adapter will contain.
     */
    public AppThemeArrayAdapter(Context context, Integer[] themes) {
        super(context, R.layout.theme_list_item, themes);

        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.themes = themes;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getItemViewForTheme(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItemViewForTheme(position);
    }

    /**
     * Creates a spinner item View for a specific app theme.
     * @param position The list index of the app theme to create the View for.
     * @return A View representing the specified theme.
     */
    private View getItemViewForTheme(int position) {
        int themeID = themes[position];

        View itemLayout = inflater.inflate(R.layout.theme_list_item, null);
        TextView itemView = (TextView) itemLayout.findViewById(R.id.text);
        itemView.setBackgroundColor(AttributeGetter.getColorAttribute(getContext(), themeID,
                R.attr.colorPrimary));
        itemView.setText(AttributeGetter.getStyleName(getContext(), themeID));

        return itemLayout;
    }

}
