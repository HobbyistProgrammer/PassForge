package com.benton.passforge.model;

public class Passwords {

    private String name;
    private String password;
    private int password_length;

    public Passwords(String name, String password,int password_length) {
        this.name = name;
        this.password = password;
        this.password_length = password_length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPassword_length() {
        return password_length;
    }

    public void setPassword_length(int password_length) {
        this.password_length = password_length;
    }
}
