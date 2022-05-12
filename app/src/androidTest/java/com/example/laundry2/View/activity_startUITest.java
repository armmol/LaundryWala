package com.example.laundry2.View;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.laundry2.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public class activity_startUITest {
    @Rule
    public ActivityScenarioRule<activity_start> activityScenarioRule = new ActivityScenarioRule<> (activity_start.class);
    @Test
    public void uiLaunch () {
        onView(withId(R.id.start)).check (matches(isDisplayed ()));
        onView(withId(R.id.start_welcomeimage)).check (matches(isDisplayed ()));
        onView(withId(R.id.start_welcome)).check (matches(isDisplayed ()));
    }
}