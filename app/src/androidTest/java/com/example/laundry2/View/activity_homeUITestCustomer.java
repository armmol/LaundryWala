package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.laundry2.TestUtil.dummyCustomer;
import static com.example.laundry2.TestUtil.dummyCustomerId;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.Database.AuthType;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.LiveDataUtil;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class activity_homeUITestCustomer {

    @Rule
    public ActivityScenarioRule<activity_home> scenario = new ActivityScenarioRule<> (activity_home.class);
    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();

    private AuthenticationViewModel authenticationViewModel;
    private ApplicationDatabase db;
    private ApplicationDao applicationDao;

    @Before
    public void setUp () {
        db = Room.inMemoryDatabaseBuilder (ApplicationProvider.getApplicationContext (), ApplicationDatabase.class).build ();
        applicationDao = db.appDao ();
        authenticationViewModel = new AuthenticationViewModel (ApplicationProvider.getApplicationContext ());
        authenticationViewModel.signOut ();
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        applicationDao.insertAuthtype (new AuthType (dummyCustomer.getAuthType ()));
    }

    @After
    public void tearDown () {
        authenticationViewModel.signOut ();
        db.close ();
    }

    @Test
    public void uiLaunch () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        String authtype = LiveDataUtil.getOrAwaitValueForLiveData (applicationDao.getAuthType ()).authtype;
        onView (withId (R.id.swiperefreshlayout_home)).check (matches (isDisplayed ()));
        onView (withId (R.id.imageButton_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.imageButton_map)).check (matches (isDisplayed ()));
        onView (withId (R.id.imageButton_orderhistory)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_userGreeting)).check (matches (withEffectiveVisibility (ViewMatchers.Visibility.VISIBLE)));
        onView (withId (R.id.recyclerView_userhome)).check (matches (isDisplayed ()));
        onView (withId (R.id.switch_activestatus)).check (matches (withEffectiveVisibility (ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void checkLaundryHousesDisplayForCustomer () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        authenticationViewModel.loadAllLaundryHouses (dummyCustomerId);
        List<LaundryHouse> laundryHouseArrayList = LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getLaundryHouses ());
        assertEquals(laundryHouseArrayList.size (),4);
    }
}