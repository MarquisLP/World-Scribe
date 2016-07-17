package com.averi.worldscribe;

/**
 * Created by mark on 14/07/16.
 * A class containing data on a Member within a Group.
 */
public class Member {
    /**
     * The name of the Person who is a Member of the Group.
     */
    public String memberName;
    /**
     * The role or rank the Person has within the Group.
     * This field is optional; null signifies no role or rank.
     */
    public String memberRole = null;
}
