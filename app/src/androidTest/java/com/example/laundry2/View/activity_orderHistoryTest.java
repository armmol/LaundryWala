package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.laundry2.TestUtil.dummyCourierId;
import static com.example.laundry2.TestUtil.dummyCustomer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.Database.AuthType;
import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.LiveDataUtil;
import com.example.laundry2.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public class activity_orderHistoryTest {
    private ApplicationDatabase db;
    private ApplicationDao applicationDao;
    private AuthenticationViewModel authenticationViewModel;

    @Rule
    public ActivityScenarioRule<activity_orderHistory> activityScenarioRule = new ActivityScenarioRule<> (activity_orderHistory.class);
    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();

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
        applicationDao.deleteIsOrderTracking ();
        db.close ();
    }

    @Test
    public void uiLaunch () {
        onView (withId (R.id.recyclerView_orderhistory)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView10)).check (matches (isDisplayed ()));
    }

    @Test
    public void isOrderHistory() throws InterruptedException {
        authenticationViewModel.insertIsOrderTrackingData (ApplicationProvider.getApplicationContext ().getString (R.string.isordertracking));
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData (authenticationViewModel.getOrderTracking ()).isOrderTracking, ApplicationProvider.getApplicationContext ().getString (R.string.isordertracking));
    }

    @Test
    public void isTracking() throws InterruptedException {
        authenticationViewModel.removeIsOrderTrackingData ();
        assertNull (LiveDataUtil.getOrAwaitValueForLiveData (authenticationViewModel.getOrderTracking ()));
    }
}