package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.laundry2.TestUtil.dummyCustomer;
import static com.example.laundry2.TestUtil.dummyCustomerId;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.LiveDataUtil;
import com.example.laundry2.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class activity_profileUITest {

    private AuthenticationViewModel authenticationViewModel;

    @Rule
    public ActivityScenarioRule<activity_profile> activityScenarioRule = new ActivityScenarioRule<activity_profile> (activity_profile.class);
    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();

    @Before
    public void setUp () {
        authenticationViewModel = new AuthenticationViewModel (ApplicationProvider.getApplicationContext ());
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
    }

    @After
    public void tearDown () {
    }

    @Test
    public void uiLaunch () {
        onView (withId (R.id.layout_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.btn_save_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.btn_logout_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.edtxt_address_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.edtxt_email_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.edtxt_name_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.btn_logout_profile)).check (matches (isDisplayed ()));
    }

    @Test
    public void Logout () throws InterruptedException {
        authenticationViewModel.signOut ();
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getLogoutMutableLiveData ()), true);
    }

    @Test
    public void saveData () throws InterruptedException {
        authenticationViewModel.enterIntoDB (dummyCustomerId, dummyCustomer.getEmail (), dummyCustomer.getAuthType (),
                dummyCustomer.getName (), dummyCustomer.getAddress (), dummyCustomer.getArea (), 54.8953516, 23.9282175);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (), "Updated Successfully");
    }
}