package com.example.laundry2.View;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.laundry2.TestUtil.dummyCustomer;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.LaundryBasketViewModel;
import com.example.laundry2.LiveDataUtil;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.R;
import com.example.laundry2.RecyclerViewMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class activity_createBasketUITest {

    private LaundryBasketViewModel laundryBasketViewModel;
    private AuthenticationViewModel authenticationViewModel;
    private ApplicationDatabase db;
    private ApplicationDao applicationDao;

    @Rule
    public ActivityScenarioRule<activity_createBasket> scenario = new ActivityScenarioRule<> (activity_createBasket.class);
    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();


    @Before
    public void setUp () {
        db = Room.inMemoryDatabaseBuilder (ApplicationProvider.getApplicationContext (), ApplicationDatabase.class).build ();
        applicationDao = db.appDao ();
        laundryBasketViewModel = new LaundryBasketViewModel (ApplicationProvider.getApplicationContext ());
        authenticationViewModel = new AuthenticationViewModel (ApplicationProvider.getApplicationContext ());
        authenticationViewModel.insertLaundryHouseCacheData ("ROAax2Y8kNX12d7ftY5QSCe71he2", "2.5");
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (),"123456",dummyCustomer.getAuthType ());
    }

    @After
    public void tearDown () {
        db.close ();
        authenticationViewModel.removeLaundryHouseCacheData ();
    }

    @Test
    public void uiLaunch () {
        onView (withId (R.id.create_basket)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_shirts_add)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_bedsheets)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_pants)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_jackets)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_suits)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_kgs)).check (matches (isDisplayed ()));
        onView (withId (R.id.imgbtn_carpets)).check (matches (isDisplayed ()));
        onView (withId (R.id.btn_confrimandpay_createbasket)).check (matches (isDisplayed ()));
        onView (withId (R.id.checkBox_iNeedACourier)).check (matches (isDisplayed ()));
        onView (withId (R.id.checkBox_iNeedDrying)).check (matches (isDisplayed ()));
        onView (withId (R.id.card_laundrybasket)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_laundrybasketcost)).check (matches (isDisplayed ()));
        onView (withId (R.id.txt_laundrybasketcounter)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView_kgcounter)).check (matches (withEffectiveVisibility (ViewMatchers.Visibility.VISIBLE)));
        onView (withId (R.id.textView3)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView4)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView5)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView6)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView7)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView8)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView9)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView13)).check (matches (isDisplayed ()));
        onView (withId (R.id.textView17)).check (matches (isDisplayed ()));
    }

    @Test
    public void addItemsToBasketAndConfirmOrder () throws InterruptedException {
        laundryBasketViewModel.clearLaundryItemCache ();
        onView (withId (R.id.imgbtn_shirts_add)).perform (click ());
        onView (withId (R.id.imgbtn_carpets)).perform (click ());
        onView (withId (R.id.imgbtn_kgs)).perform (click ());
        onView (withId (R.id.imgbtn_jackets)).perform (click ());
        onView (withId (R.id.imgbtn_suits)).perform (click ());
        onView (withId (R.id.imgbtn_pants)).perform (click ());
        onView (withId (R.id.imgbtn_bedsheets)).perform (click ());
        onView (withId(R.id.card_laundrybasket)).perform (click ());
        onView (withId (R.id.layout_itemsview)).check (matches(isDisplayed ()));
        pressBack ();
        assertEquals(LiveDataUtil.getOrAwaitValueForLiveData
                (laundryBasketViewModel.getCachedItems ()).size (),7);
        onView (withId (R.id.txt_laundrybasketcounter)).check (matches ((ViewMatchers.withText ("7"))));
        onView (withId (R.id.txt_laundrybasketcost)).check (matches (not (ViewMatchers.withText (R.string._0))));
        onView (withId (R.id.btn_confrimandpay_createbasket)).perform (click ());
        onView (withId (R.id.layout_confrim_address)).check (matches (isDisplayed ()));
        onView (withId (R.id.button_confirmaddress_confirm)).perform (click ());
        onView (withId (R.id.layout_payments)).check (matches (isDisplayed ()));
        onView (withId (R.id.paypalbtn)).perform (click ());
    }

    @Test
    public void clearBasket() throws InterruptedException {
        laundryBasketViewModel.clearLaundryItemCache ();
        onView (withId (R.id.imgbtn_shirts_add)).perform (click ());
        onView (withId (R.id.imgbtn_carpets)).perform (click ());
        onView (withId (R.id.checkBox_iNeedACourier)).perform (click ());
        onView (withId (R.id.checkBox_iNeedACourier)).check (matches (not(isChecked ())));
        onView (withId (R.id.checkBox_iNeedDrying)).perform (click ());
        onView (withId (R.id.checkBox_iNeedDrying)).check (matches (isChecked ()));
        onView (withId (R.id.txt_laundrybasketcounter)).check (matches (withText ("2")));
        onView (withId(R.id.card_laundrybasket)).perform (click ());
        onView (withId (R.id.layout_itemsview)).check (matches(isDisplayed ()));
        onView (withId (R.id.textView_clearBasket)).perform (click ());
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData (applicationDao.getAllItems ()).size (),0);
    }

    @Test
    public void removeItemFromBasket() throws InterruptedException {
        laundryBasketViewModel.clearLaundryItemCache ();
        onView (withId (R.id.imgbtn_shirts_add)).perform (click ());
        onView (withId (R.id.imgbtn_carpets)).perform (click ());
        onView (withId (R.id.txt_laundrybasketcounter)).check (matches (withText ("2")));
        onView (withId(R.id.card_laundrybasket)).perform (click ());
        onView (withId (R.id.layout_itemsview)).check (matches(isDisplayed ()));
        onView (withId (R.id.recyclerView_confirmorder)).perform (RecyclerViewActions.scrollToPosition (1));
        onView (RecyclerViewMatcher.atPositionOnView (R.id.recyclerView_confirmorder,1,R.id.imgbtn_card_laundrybasket_delete)).perform (click ());
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData (laundryBasketViewModel.getCachedItems ()).size (),1);
        pressBack ();
        onView (withId (R.id.txt_laundrybasketcounter)).check (matches (withText ("1")));
    }
}