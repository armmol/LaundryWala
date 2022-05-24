package com.example.laundry2;

import static com.example.laundry2.TestUtil.dummyCourier;
import static com.example.laundry2.TestUtil.dummyCourierId;
import static com.example.laundry2.TestUtil.dummyCustomer;
import static com.example.laundry2.TestUtil.dummyCustomerId;
import static com.example.laundry2.TestUtil.dummyLaundryHouseId;
import static com.example.laundry2.TestUtil.dummyOrder;
import static com.example.laundry2.TestUtil.dummyUnassignedOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.location.Location;
import android.location.LocationManager;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.Database.CurrentOrderCourierId;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.Database.OrderTracking;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ApplicationRepositoryTest {

    @Rule
    public InstantTaskExecutorRule testRule = new InstantTaskExecutorRule ();

    private LocationViewModel locationViewModel;
    private AuthenticationViewModel authenticationViewModel;
    private PaymentsViewModel paymentsViewModel;
    private LaundryBasketViewModel laundryBasketViewModel;
    private ApplicationDao applicationDao;
    private ApplicationDatabase db;
    private final Location temp = new Location (LocationManager.GPS_PROVIDER);


    @Before
    public void setUp () {
        authenticationViewModel = new AuthenticationViewModel (ApplicationProvider.getApplicationContext ());
        locationViewModel = new LocationViewModel (ApplicationProvider.getApplicationContext ());
        paymentsViewModel = new PaymentsViewModel (ApplicationProvider.getApplicationContext ());
        laundryBasketViewModel = new LaundryBasketViewModel (ApplicationProvider.getApplicationContext ());
        temp.setLatitude (54.9162135);
        temp.setLongitude (23.9546676);
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext (), ApplicationDatabase.class).build();
        applicationDao = db.appDao ();
    }

    @After
    public void tearDown () {
        db.close ();
        authenticationViewModel.signOut ();
    }

    @Test
    public void loginWithEmailSuccess () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData
                (authenticationViewModel.getCurrentSignInUser ()).getEmail (), dummyCustomer.getEmail ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Successfully logged in as " + dummyCustomer.getAuthType ());
        authenticationViewModel.signOut ();
    }

    @Test
    public void loginWithEmailFailureDueToWrongEmailPasswordCombination () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "1234567", dummyCustomer.getAuthType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Login Failure, Email or Password is incorrect.\n" + "Are you registered?");
    }

    @Test
    public void registerWithEmailFailedUserExists () throws InterruptedException {
        authenticationViewModel.signupEmail ("test@gmail.com", "123456", "123456", "Courier");
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Registration failed. Account might exist, Check Email");
    }

    @Test
    public void getOrder () throws InterruptedException {
        locationViewModel.getCustomerOrder (dummyOrder.getOrderId ());
        Order result = LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getOrder ());
        assertEquals (result.getOrderId (), dummyOrder.getOrderId ());
        assertEquals (result.getCustomerDrop (), dummyOrder.getCustomerDrop ());
        assertEquals (result.getLaundryHouseDrop (), dummyOrder.getLaundryHouseDrop ());
        assertEquals (result.getCustomerPickUp (), dummyOrder.getCustomerPickUp ());
        assertEquals (result.getLaundryHousePickUp (), dummyOrder.getLaundryHousePickUp ());
        assertEquals (result.getDateTime (), dummyOrder.getDateTime ());
        assertEquals (result.getLaundryHouseDrop (), dummyOrder.getLaundryHouseDrop ());
        assertEquals (result.isDrying (), dummyOrder.isDrying ());
        assertEquals (result.getTotalCost (), dummyOrder.getTotalCost (), 0);
        assertEquals (result.getLaundryHouseDeliveryLocationLatitude (), dummyOrder.getLaundryHouseDeliveryLocationLatitude (), 0);
        assertEquals (result.getLaundryHouseDeliveryLocationLongitude (), dummyOrder.getLaundryHouseDeliveryLocationLongitude (), 0);
        assertEquals (result.getCustomerDeliveryLocationLongitude (), dummyOrder.getCustomerDeliveryLocationLongitude (), 0);
        assertEquals (result.getCustomerDeliveryLocationLatitude (), dummyOrder.getCustomerDeliveryLocationLatitude (), 0);
    }

    @Test
    public void getCourierLocationChangeFail () throws InterruptedException {
        locationViewModel.getCourierLocation (dummyCourierId);
        //Test does not observe a location change
        assertNull (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getCurrentLocationMutableLiveData ()));
    }

    @Test
    public void startLocationService () throws InterruptedException {
        locationViewModel.startLiveLocation ();
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getServiceStateMutableLiveData ()), true);
    }

    @Test
    public void stopLocationService () throws InterruptedException {
        locationViewModel.stopLiveLocation ();
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getServiceStateMutableLiveData ()), false);
    }

    @Test
    public void updateCourierLocation () throws InterruptedException {
        Location newLocation = new Location (LocationManager.GPS_PROVIDER);
        newLocation.setLongitude (0.0);
        newLocation.setLatitude (0.0);
        locationViewModel.updateLiveLocation (dummyCourierId, newLocation);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getAuthStateMutableLiveData ()).getType (),
                "Updated successfully");
        //setting location back to old location for further tests
        locationViewModel.updateLiveLocation (dummyCourierId, temp);
    }

    @Test
    public void getLocation () {
        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient (InstrumentationRegistry.getInstrumentation ().getContext ());
        locationViewModel.getCurrentLocation ();
        fusedLocationProviderClient.getLastLocation ().addOnSuccessListener (location -> {
            try {
                assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData
                        (locationViewModel.getCurrentLocationMutableLiveData ()), location);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        });
    }

    @Test
    public void assignOrder () throws InterruptedException {
        authenticationViewModel.assignOrder (dummyCourierId, dummyOrder.getOrderId ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Order already assigned to Courier");
    }

    @Test
    public void unassignOrder () throws InterruptedException {
        authenticationViewModel.unassignOrder (dummyUnassignedOrder.getOrderId ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Order already unassigned from Courier");
    }

    @Test
    public void getCustomerEmail () throws InterruptedException {
        locationViewModel.getCustomerEmail ("umGxpLl3vzWQWSDCMuuvxkt9Mzs1_0_m3mPFtTFO7ZGnWE75BdLv1Cf5y52");
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getCustomerEmailMutableLiveData ()),
                "armaan552@gmail.com");
    }

    @Test
    public void enterIntoDatabase () throws InterruptedException {
        authenticationViewModel.enterIntoDB (dummyCustomerId,dummyCustomer.getEmail (),dummyCustomer.getAuthType (),
                dummyCustomer.getName (),dummyCustomer.getAddress (), dummyCustomer.getArea (), 0.0,0.0);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType ()
                , "Updated Successfully");
    }

    @Test
    public void createOrder () throws InterruptedException {
        applicationDao.insertLaundryItem (new LaundryItemCache ("Shirt"));
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData (applicationDao.getAllItems ()).get (0).getType (),"Shirt");
        laundryBasketViewModel.createOrder (dummyCustomerId,dummyLaundryHouseId,2.5,true);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (laundryBasketViewModel.orderPlacementStatus ()).getType ()
                , "Order was placed");
    }

    @Test
    public void updateOrderStatus () throws InterruptedException {
        authenticationViewModel.updateOrderStatus ("Order Not Started", dummyOrder.getOrderId ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Order Status changed successfully");
    }

    @Test
    public void getNewDeliveryCost () {
        authenticationViewModel.getNewDeliveryCost ();

    }

    @Test
    public void changeOrderPickDropStatus () throws InterruptedException {
        //Case Customer
        authenticationViewModel.changeOrderPickDropStatus (dummyOrder.getOrderId (), dummyOrder.getCourierId (),
                "Customer", "", false);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType ()
                , "Please wait for Courier! Courier is close to you, we will update you");

        //Laundry House
        authenticationViewModel.changeOrderPickDropStatus (dummyOrder.getOrderId (), dummyOrder.getCourierId (),
                "Laundry House", "", false);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType ()
                , "Please wait for Courier! Courier is close to you, we will update you");
    }

    @Test
    public void notifyOfArrival () throws InterruptedException {
        authenticationViewModel.notifyOfArrival (dummyOrder.getOrderId (),TestUtil.dummyCustomerId,"","");
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Notified successfully");
    }

    @Test
    public void getNotifiedReturnsNullBecauseOfNoNotificationSent () throws InterruptedException {
        authenticationViewModel.getNotified (dummyOrder.getOrderId ());
        assertTrue (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getCourierArrivalMutableLiveData ()));
    }

    @Test
    public void changeActiveStatus () throws InterruptedException {
        authenticationViewModel.changeActiveStatus (true, dummyCourier.getAuthType (), dummyCourierId);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getState ()).getType (),
                "Active Status changed successfully");
    }

    @Test
    public void insertOrderTracking () throws InterruptedException {
        applicationDao.insertIsOrderTracking (new OrderTracking ("test"));
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData
                (applicationDao.getIsOrderTracking ()).isOrderTracking,"test");
        applicationDao.deleteIsOrderTracking ();
    }

    @Test
    public void removeOrderTracking () throws InterruptedException {
        applicationDao.deleteIsOrderTracking ();
        assertNull (LiveDataUtil.getOrAwaitValueForLiveData (applicationDao.getIsOrderTracking ()));
    }

    @Test
    public void insertCurrentOrderCourierId () throws InterruptedException {
        applicationDao.insertCurrentOrderCourierId (new CurrentOrderCourierId ("test",""));
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData
                (applicationDao.getCurrentOrderCourierId ()).courierId,"test");
        applicationDao.deleteCurrentOrderCourierId ();
    }

    @Test
    public void removeCurrentOrderCourierId () throws InterruptedException {
        applicationDao.deleteCurrentOrderCourierId ();
        assertNull (LiveDataUtil.getOrAwaitValueForLiveData
                (applicationDao.getCurrentOrderCourierId ()));
    }

    @Test
    public void clearLaundryItemCache () throws InterruptedException {
        applicationDao.deleteAll ();
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData
                (applicationDao.getAllItems ()).size (),0);
    }

    @Test
    public void addItemToCache() throws InterruptedException {
        applicationDao.insertLaundryItem (new LaundryItemCache ("Shirt"));
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData
                (applicationDao.getAllItems ()).get (0).getType (),"Shirt");
        applicationDao.deleteAll ();
    }

    @Test
    public void removeItemFromCache() throws InterruptedException {
        applicationDao.deleteLaundryItem (new LaundryItemCache ("Shirt"));
        assertEquals (LiveDataUtil.getOrAwaitValueForLiveData
                (applicationDao.getAllItems ()).size (),0);
        applicationDao.deleteAll ();
    }

    @Test
    public void addItem () throws InterruptedException {
        laundryBasketViewModel.addItem ("Suit/Blazer/Coat");
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (laundryBasketViewModel.getLaundryItems ()).get (0).getType (),
                TestUtil.dummyLaundryItems.get (0).getType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (laundryBasketViewModel.getLaundryItems ()).get (0).getCost (),
                TestUtil. dummyLaundryItems.get (0).getCost (), 0);
    }

    @Test
    public void removeItem () throws InterruptedException {
        laundryBasketViewModel.addItem ("Suit/Blazer/Coat");
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (laundryBasketViewModel.getLaundryItems ()).get (0).getType (),
                TestUtil. dummyLaundryItems.get (0).getType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (laundryBasketViewModel.getLaundryItems ()).get (0).getCost (),
                TestUtil.dummyLaundryItems.get (0).getCost (), 0);
        laundryBasketViewModel.removeItem (new LaundryItemCache ("Suit/Blazer/Coat"));
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (laundryBasketViewModel.getLaundryItems ()).size (),
                0, 0);
    }

    @Test
    public void getApplicationUserData () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        authenticationViewModel.loadApplicationUserData (dummyCustomer.getAuthType (), dummyCustomerId);
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getAddress (),
                dummyCustomer.getAddress ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getAuthType (),
                dummyCustomer.getAuthType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getOrderId ().get (0),
                dummyCustomer.getOrderId ().get (0));
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getEmail (),
                dummyCustomer.getEmail ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getArea (),
                dummyCustomer.getArea ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getApplicationUserData ()).getName (),
                dummyCustomer.getName ());
        authenticationViewModel.signOut ();
    }

    @Test
    public void loadAllLaundryHouses () throws InterruptedException {
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        authenticationViewModel.loadAllLaundryHouses (TestUtil.dummyCustomerId);
        List<LaundryHouse> result = LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getLaundryHouses ());
        assertEquals (result.get (0).getName (), "Laundry House Kestucio");
        authenticationViewModel.signOut ();
    }

    @Test
    public void loadAllOrders () throws InterruptedException {
        //For Order History
        authenticationViewModel.loginEmail (dummyCustomer.getEmail (), "123456", dummyCustomer.getAuthType ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getCurrentSignInUser ()).getEmail (), dummyCustomer.getEmail ());
        authenticationViewModel.loadAllOrders (dummyCustomer.getAuthType (),dummyCustomerId, true);
        Order result = LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getOrders ()).get (1);
        assertEquals (result.getOrderId (), dummyOrder.getOrderId ());
        assertEquals (result.getCustomerDrop (), dummyOrder.getCustomerDrop ());
        assertEquals (result.getLaundryHouseDrop (), dummyOrder.getLaundryHouseDrop ());
        assertEquals (result.getCustomerPickUp (), dummyOrder.getCustomerPickUp ());
        assertEquals (result.getLaundryHousePickUp (), dummyOrder.getLaundryHousePickUp ());
        assertEquals (result.getDateTime (), dummyOrder.getDateTime ());
        assertEquals (result.getLaundryHouseDrop (), dummyOrder.getLaundryHouseDrop ());
        assertEquals (result.isDrying (), dummyOrder.isDrying ());
        assertEquals (result.getTotalCost (), dummyOrder.getTotalCost (), 0);
        assertEquals (result.getLaundryHouseDeliveryLocationLatitude (), dummyOrder.getLaundryHouseDeliveryLocationLatitude (), 0);
        assertEquals (result.getLaundryHouseDeliveryLocationLongitude (), dummyOrder.getLaundryHouseDeliveryLocationLongitude (), 0);
        assertEquals (result.getCustomerDeliveryLocationLongitude (), dummyOrder.getCustomerDeliveryLocationLongitude (), 0);
        assertEquals (result.getCustomerDeliveryLocationLatitude (), dummyOrder.getCustomerDeliveryLocationLatitude (), 0);
        authenticationViewModel.signOut ();
    }

    @Test
    public void loadAllCouriers () throws InterruptedException {
        authenticationViewModel.loadAllCouriers (dummyOrder.getOrderId ());
        List<Courier> result = LiveDataUtil.getOrAwaitValueForMutableLiveData (authenticationViewModel.getCouriers ());
        for (Courier courier : result) {
            if (courier.getUid ().equals (dummyCourierId)) {
                assertEquals (courier.getName (), dummyCourier.getName ());
            }
        }
    }

    @Test
    public void getUserAndLaundryHouseLatLng () throws InterruptedException {
        authenticationViewModel.assignOrder (dummyCourierId, dummyOrder.getOrderId ());
        locationViewModel.getUserAndLaundryHouseMarkerLocation (dummyOrder.getOrderId ());
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getLatLngMutableLiveData ()).get (0),
                new LatLng (dummyOrder.getCustomerDeliveryLocationLatitude (), dummyOrder.getCustomerDeliveryLocationLongitude ()));
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (locationViewModel.getLatLngMutableLiveData ()).get (1),
                new LatLng (dummyOrder.getLaundryHouseDeliveryLocationLatitude (), dummyOrder.getLaundryHouseDeliveryLocationLongitude ()));
    }

    @Test
    public void fetchCanUseGooglePay () throws InterruptedException {
        paymentsViewModel.canPayWithGooglePay ();
        assertEquals (LiveDataUtil.getOrAwaitValueForMutableLiveData (paymentsViewModel.get_canUseGooglePay ()),true);
    }

    @Test
    public void getLoadPaymentDataTaskFails () throws InterruptedException {
        paymentsViewModel.loadPaymentDataForGPay (100);
        assertFalse (LiveDataUtil.getOrAwaitValueForMutableLiveData (paymentsViewModel.getPaymentDataTaskMutableLiveData ()).isSuccessful ());
    }

}