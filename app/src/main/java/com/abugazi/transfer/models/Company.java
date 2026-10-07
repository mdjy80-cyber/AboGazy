package com.abugazi.transfer.models;

public class Company {
    public int id;
    public String name;
    public String color;
    public int active;

    public Company() {}

    @Override
    public String toString() {
        return name;
    }
}
