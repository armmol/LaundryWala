package com.example.laundry2.DataClasses;

public class AuthState {
    String type;
    boolean isValid;


    public AuthState (String type, boolean isvalid) {
        this.type = type;
        this.isValid = isvalid;
    }

    public boolean isValid () {
        return isValid;
    }

    public void setIsValid (boolean isvalid) {
        this.isValid = isvalid;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }
}
