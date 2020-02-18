package com.averi.worldscribe;

import java.util.Comparator;

/**
 * Created by mark on 05/08/16.
 * A class containing data on a Person's residence within a certain Place.
 */
public class Residence {
    /**
     * The name of the World containing the Place and resident.
     */
    public String worldName;
    /**
     * The name of the Place of residence.
     */
    public String placeName;
    /**
     * The name of the Person taking residence.
     */
    public String residentName;

    /**
     * A Comparator that compares the place names between two Residences.
     */
    public static class ByPlaceNameComparator implements Comparator<Residence> {
        @Override
        public int compare(Residence residence, Residence t1) {
            return residence.placeName.compareTo(t1.placeName);
        }
    }

    /**
     * A Comparator that compares the resident names between two Residences.
     */
    public static class ByResidentNameComparator implements Comparator<Residence> {
        @Override
        public int compare(Residence residence, Residence t1) {
            return residence.residentName.compareTo(t1.residentName);
        }
    }
}
