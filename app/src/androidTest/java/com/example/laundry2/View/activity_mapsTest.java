package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.laundry2.TestUtil.dummyCourierId;
import static com.example.laundry2.TestUtil.dummyCustomer;
import static org.junit.Assert.assertEquals;

import android.Manifest;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.Database.AuthType;
import com.example.laundry2.LiveDataUtil;
import com.example.laundry2.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith (AndroidJUnit4.class)
public class activity_mapsTest {

    private ApplicationDatabase db;
    private ApplicationDao applicationDao;
    private AuthenticationViewModel authenticationViewModel;

    @Rule
    public ActivityScenarioRule<activity_maps> activityScenarioRule = new ActivityScenarioRule<> (activity_maps.class);
    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();
    @Rule
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant (Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setUp(){

        db = Room.inMemoryDatabaseBuilder (ApplicationProvider.getApplicationContext (), ApplicationDatabase.class).build ();
        applicationDao = db.appDao ();
        authenticationViewModel = new AuthenticationViewModel (ApplicationProvider.getApplicationContext ());
        authenticationViewModel.insertCurrentOrderCourierId (dummyCourierId);
        applicationDao.insertAuthtype (new AuthType (dummyCustomer.getAuthType ()));

    }

    @After
    public void tearDown(){
        authenticationViewModel.removeCurrentOrderCourierId ();
        authenticationViewModel.signOut ();
        applicationDao.deleteAuthType ();
        db.close ();
    }

    @Test
    public void uiLaunch () {
        onView (withId (R.id.map)).check (matches (isDisplayed ()));
        onView (withId (R.id.button_refreshMap)).check (matches (isDisplayed ()));
    }

    @Test
    public void forCustomerLoginCourierIdIsSentAcrossActivties () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (),"123456", dummyCustomer.getAuthType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData (authenticationViewModel.getCurrentOrderCourierId ()).courierId, dummyCourierId);
    }
}