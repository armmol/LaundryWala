package com.example.laundry2;

import androidx.test.espresso.idling.CountingIdlingResource;

public class ExpressoIdlingResource {

    private static final String RESOURCE = "Global";

    public static final CountingIdlingResource countingIdlingResource = new CountingIdlingResource (RESOURCE);

    public static void increment () {
        countingIdlingResource.increment ();
    }

    public static void decrement () {
        if (countingIdlingResource.isIdleNow ())
            countingIdlingResource.decrement ();
    }
}