package com.averi.worldscribe.adapters;

import com.averi.worldscribe.LinkedArticleList;

/**
 * <p>
 * Created by mark on 31/07/16.
 * </p>
 *
 * <p>
 * An Interface for Adapters containing links to Articles.
 * </p>
 */
public interface ArticleLinkAdapter {
    /**
     * @return A list of all linked Articles contained within this Adapter.
     */
    public LinkedArticleList getLinkedArticleList();
}
