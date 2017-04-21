package com.example.myapplication;

/**
 * Created by austin on 4/13/2017.
 */

public class contact {
    private String name, number;
    public contact(String n, String num){
        this.name = n;
        this.number = num;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getNumber(){
        return this.number;
    }

    public void setNumber(String num){
        this.number = num;
    }
}
