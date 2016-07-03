package com.averi.worldscribe.model;

import java.util.ArrayList;

/**
 * Created by mark on 02/06/16.
 */
public class World {
    private String name;
    private ArrayList<Person> people;
    private ArrayList groups;
    private ArrayList places;
    private ArrayList items;
    private ArrayList concepts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
