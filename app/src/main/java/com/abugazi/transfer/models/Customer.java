package com.abugazi.transfer.models;

public class Customer {
    public int id;
    public String name;
    public String phone;
    public double balance;
    public String createdAt;

    public Customer() {}

    @Override
    public String toString() {
        return name;
    }
}
