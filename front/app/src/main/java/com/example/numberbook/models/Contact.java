package com.example.numberbook.models;


public class Contact {
    private String name;
    private String number;

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    // Obligatoire pour Retrofit/Gson
    public Contact() {}

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
