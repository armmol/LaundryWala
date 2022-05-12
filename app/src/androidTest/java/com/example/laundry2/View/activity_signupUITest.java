package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.laundry2.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class activity_signupUITest {

    @Rule
    public ActivityScenarioRule<activity_signup> scenarioRule = new ActivityScenarioRule<> (activity_signup.class);

    @Test
    public void uiLaunch () {
        onView (withId (R.id.layout_signup)).check (matches (isDisplayed ()));
        onView (withId (R.id.btn_googlesignin_signup)).check (matches (isDisplayed ()));
        onView (withId (R.id.btn_signup)).check (matches (isDisplayed ()));
        onView (withId (R.id.edtxt_email_signup)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_gotologinfromsignup)).check (matches (isDisplayed ()));
        onView (withId (R.id.edtxt_password_signup)).check (matches (isDisplayed ()));
        onView (withId (R.id.edtxt_confirm_password_signup)).check (matches (isDisplayed ()));
    }

    @Test
    public void uiSignUpButtonClick () {
        String email = "mom1@gmail.com";
        String password = "123456";
        String authType = "Laundry House";
        onView (withId (R.id.edtxt_email_signup)).perform (typeText (email));
        onView (withId (R.id.edtxt_password_signup)).perform (typeText (password));
        onView (withId (R.id.edtxt_confirm_password_signup)).perform (typeText (password));
        closeSoftKeyboard ();
        onView (withId (R.id.spinner_signup)).perform (click ());
        onData (allOf (is (instanceOf (String.class)), is (authType))).perform (click ());
        onView (withId (R.id.spinner_signup)).check (matches (withSpinnerText (containsString (authType))));
        onView (withId (R.id.btn_signup)).perform (click ());
    }

    @Test
    public void uiGoogleSignIn () {
        String authType = "Laundry House";
        onView (withId (R.id.spinner_signup)).perform (click ());
        onData (allOf (is (instanceOf (String.class)), is (authType))).perform (click ());
        onView (withId (R.id.spinner_signup)).check (matches (withSpinnerText (containsString (authType))));
        onView (withId (R.id.btn_googlesignin_signup)).perform (click ());
    }

    @Test
    public void goToLogIn () {
        onView (withId (R.id.txt_gotologinfromsignup)).perform (click ());
        onView (withId (R.id.layout_login)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_gotosignupfromlogin)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_gotosignupfromlogin)).perform (click ());
        onView (withId (R.id.layout_signup)).check (matches (isDisplayed ()));
    }
}