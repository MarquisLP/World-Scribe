package com.averi.worldscribe.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.averi.worldscribe.R;

/**
 * Created by mark on 28/08/16.
 *
 * A wrapper for Article section TextViews, allowing them to collapse and expand their
 * respective sections when clicked.
 */
public class ArticleSectionCollapser implements View.OnClickListener {

    private Context context;
    private String sectionName;
    private TextView sectionHeader;
    private ViewGroup sectionLayout;

    /**
     * Instantiates a new ArticleSectionCollapser.
     * @param context The Context the Article section belongs to.
     * @param sectionHeader The TextView displaying the section's name.
     * @param sectionLayout The layout containing the actual content of the section.
     */
    public ArticleSectionCollapser(Context context, TextView sectionHeader,
                                   ViewGroup sectionLayout) {
        this.context = context;
        this.sectionName = sectionHeader.getText().toString();
        this.sectionHeader = sectionHeader;
        this.sectionLayout = sectionLayout;

        sectionHeader.setOnClickListener(this);

        updateCollapseIcon();
    }

    @Override
    public void onClick(View view) {
        toggleSectionLayoutVisiblity();
        updateCollapseIcon();
    }

    /**
     * Toggles the visibility of the section's layout.
     */
    private void toggleSectionLayoutVisiblity() {
        if (sectionLayout.getVisibility() == View.VISIBLE) {
            sectionLayout.setVisibility(View.GONE);
        } else {
            sectionLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Changes the icon beside the header to show whether the section is currently
     * collapsed or expanded.
     */
    private void updateCollapseIcon() {
        if (sectionLayout.getVisibility() == View.VISIBLE) {
            sectionHeader.setText(context.getString(R.string.expandedSectionHeader,
                    sectionName));
        } else {
            sectionHeader.setText(context.getString(R.string.collapsedSectionHeader,
                    sectionName));
        }
    }

}
