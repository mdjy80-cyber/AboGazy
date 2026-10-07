package com.abugazi.transfer.models;

public class Category {
    public int id;
    public int companyId;
    public String name;
    public double value;
    public double price;
    public int active;

    public Category() {}

    @Override
    public String toString() {
        return name + " - " + price;
    }
}
