package com.averi.worldscribe.views;

import com.averi.worldscribe.Category;

/**
 * Created by mark on 23/08/16.
 *
 * Interface for Activities that contain a {@link BottomBar} View.
 */
public interface BottomBarActivity {
    /**
     * Responds to the user pressing a Category button on the BottomBar.
     * @param category The Category represented by the button that was pressed.
     */
    void respondToBottomBarButton(Category category);
}
