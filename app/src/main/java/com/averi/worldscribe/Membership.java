package com.averi.worldscribe;

import java.io.Serializable;

/**
 * Created by mark on 14/07/16.
 * A class containing data on a Person's Membership within a certain Group.
 */
public class Membership implements Serializable {
    /**
     * The name of the World where the Group and its members reside.
     */
    public String worldName;
    /**
     * The name of the Group this Membership is for.
     */
    public String groupName;
    /**
     * The name of the Person involved in this Membership.
     */
    public String memberName;
    /**
     * The role or rank the Member has within the Group.
     * This field is optional; empty string signifies no role or rank.
     */
    public String memberRole = "";
}
