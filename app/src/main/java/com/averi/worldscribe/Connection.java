package com.averi.worldscribe;

import java.io.Serializable;

/**
 * Created by mark on 02/07/16.
 */
public class Connection implements Serializable {
    public String worldName;
    public Category articleCategory;
    public String articleName;
    public String articleRelation;
    public Category connectedArticleCategory;
    public String connectedArticleName;
    public String connectedArticleRelation;
}
