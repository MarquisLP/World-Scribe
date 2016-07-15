package com.averi.worldscribe;

/**
 * Created by mark on 14/07/16.
 * A class containing data on a Person's Membership within a certain Group.
 */
public class Membership {
    /**
     * The name of the Group this Membership is for.
     */
    public String groupName;
    /**
     * The role or rank the Person has within the Group.
     * This field is optional; null signifies no role or rank.
     */
    public String memberRole = null;
}
