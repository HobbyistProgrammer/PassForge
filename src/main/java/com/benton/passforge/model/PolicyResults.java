package com.benton.passforge.model;

import java.util.List;

public class PolicyResults {

    public boolean isValid;
    public String strength;
    public List<String> errors;

    public PolicyResults(boolean isValid, String strength, List<String> errors) {
        this.isValid = isValid;
        this.strength = strength;
        this.errors = errors;
    }
}
