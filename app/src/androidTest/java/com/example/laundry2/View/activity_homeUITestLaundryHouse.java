package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.laundry2.TestUtil.dummyLaundryHouse;
import static com.example.laundry2.TestUtil.dummyLaundryHouseId;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.Database.AuthType;
import com.example.laundry2.EspressoIdlingResource;
import com.example.laundry2.LiveDataUtil;
import com.example.laundry2.R;
import com.example.laundry2.RecyclerViewMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class activity_homeUITestLaundryHouse {

    @Rule
    public ActivityScenarioRule<activity_home> scenario = new ActivityScenarioRule<> (activity_home.class);
    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();

    private AuthenticationViewModel authenticationViewModel;
    private ApplicationDatabase db;
    private ApplicationDao applicationDao;

    @Before
    public void setUp () {
        IdlingRegistry.getInstance ().register (EspressoIdlingResource.countingIdlingResource);
        db = Room.inMemoryDatabaseBuilder (ApplicationProvider.getApplicationContext (), ApplicationDatabase.class).build ();
        applicationDao = db.appDao ();
        authenticationViewModel = new AuthenticationViewModel (ApplicationProvider.getApplicationContext ());
        authenticationViewModel.loginEmail (dummyLaundryHouse.getEmail (), "123456", dummyLaundryHouse.getAuthType ());
        applicationDao.insertAuthtype (new AuthType (dummyLaundryHouse.getAuthType ()));
    }

    @After
    public void tearDown () {
        IdlingRegistry.getInstance ().unregister (EspressoIdlingResource.countingIdlingResource);
        authenticationViewModel.signOut ();
        db.close ();
    }

    @Test
    public void uiLaunch () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyLaundryHouse.getEmail (), "123456", dummyLaundryHouse.getAuthType ());
        String authtype = LiveDataUtil.getOrAwaitValueForLiveData (applicationDao.getAuthType ()).authtype;
        assertEquals (authtype, dummyLaundryHouse.getAuthType ());
        onView (withId (R.id.imageButton_profile)).check (matches (isDisplayed ()));
        onView (withId (R.id.imageButton_map)).check (matches (isDisplayed ()));
        onView (withId (R.id.imageButton_orderhistory)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_userGreeting)).check (matches (withEffectiveVisibility (ViewMatchers.Visibility.VISIBLE)));
        onView (withId (R.id.recyclerView_userhome)).check (matches (isDisplayed ()));
    }

    @Test
    public void checkOrdersDisplayForLaundryHouse () throws InterruptedException {
        authenticationViewModel.loadAllOrders (dummyLaundryHouse.getAuthType (), dummyLaundryHouseId, false);
        RecyclerViewMatcher.atPositionOnView (R.id.recyclerView_userhome,1,R.id.button_assign).matches (isDisplayed ());
        List<Order> arraylistOrder = LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getOrders ());
        authenticationViewModel.loginEmail (dummyLaundryHouse.getEmail (), "123456", dummyLaundryHouse.getAuthType ());
        authenticationViewModel.loadApplicationUserData (dummyLaundryHouse.getAuthType (), dummyLaundryHouseId);
        assertEquals (arraylistOrder.size (), LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getOrderId ().size ());
    }
}