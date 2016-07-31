package com.averi.worldscribe;

import java.io.Serializable;
import java.util.HashSet;

/**
 * <p>
 * Created by mark on 31/07/16.
 * </p>
 *
 * <p>
 * Contains a list of all of the Articles that are linked to a certain Article through one
 * of the three types of links (Connection, Membership, or Residence).
 * </p>
 */
public class LinkedArticleList implements Serializable {

    private HashSet<String> personNames = new HashSet<>();
    private HashSet<String> groupNames = new HashSet<>();
    private HashSet<String> placeNames = new HashSet<>();
    private HashSet<String> itemNames = new HashSet<>();
    private HashSet<String> conceptNames = new HashSet<>();

    /**
     * Adds an Article to this list.
     * @param category The {@link Category} of the Article.
     * @param name The name of the Article.
     */
    public void addArticle(Category category, String name) {
        switch (category) {
            case Person:
                personNames.add(name);
                break;
            case Group:
                groupNames.add(name);
                break;
            case Place:
                placeNames.add(name);
                break;
            case Item:
                itemNames.add(name);
                break;
            case Concept:
            default:
                conceptNames.add(name);
                break;
        }
    }

    /**
     * Gets all Article links within a certain Category.
     * @param category The {@link Category} of Articles to return.
     * @return A HashSet containing all Article links of the specified Category.
     */
    public HashSet<String> getAllLinksInCategory(Category category) {
        switch (category) {
            case Person:
                return personNames;
            case Group:
                return groupNames;
            case Place:
                return placeNames;
            case Item:
                return itemNames;
            case Concept:
            default:
                return conceptNames;
        }
    }

}
