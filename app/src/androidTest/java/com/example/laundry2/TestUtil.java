package com.example.laundry2;

import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;

import java.util.ArrayList;
import java.util.Collections;

public class TestUtil {
    public static ArrayList<String> dummyOrderIds = new ArrayList<> (Collections.singletonList ("qq3JQ6hzJFWTRPR1q6o2YaYqFMe2_0_ROAax2Y8kNX12d7ftY5QSCe71he2"));
    public static ArrayList<LaundryItem> dummyLaundryItems = new ArrayList<> (Collections.singletonList (new LaundryItem ("Suit/Blazer/Coat", 1)));
    public static String dummyCourierId = "mXWTJs7VWUWI82RP9cLsoad8R8n1";
    public static String dummyCustomerId = "qq3JQ6hzJFWTRPR1q6o2YaYqFMe2";
    public static String dummyLaundryHouseId = "ROAax2Y8kNX12d7ftY5QSCe71he2";
    public static ApplicationUser dummyCustomer = new ApplicationUser ("Trakų g. 12, Kaunas 44236, Lithuania", " Kaunas 44236",
            "Customer", "fake@gmail.com",
            54.8953516, 23.9282175, "Fake Customer",
            7, false, dummyOrderIds);
    public static ApplicationUser dummyCourier = new ApplicationUser ("Birželio 23-iosios g., Kaunas, Lithuania", "Kaunas",
            "Courier", "fakecourier@gmail.com",
            54.9162135, 23.9546676, "fake courier",
            0, true, dummyOrderIds);
    public static ApplicationUser dummyLaundryHouse = new ApplicationUser ("Birželio 23-iosios g., Kaunas, Lithuania", "Kaunas",
            "Laundry House", "fakelaundryhouse@gmail.com",
            54.9162135, 23.9546676, "fakelaundryhouser",
            0, true, dummyOrderIds);
    public static Order dummyOrder = new Order ("qq3JQ6hzJFWTRPR1q6o2YaYqFMe2_1_ROAax2Y8kNX12d7ftY5QSCe71he2", "mXWTJs7VWUWI82RP9cLsoad8R8n1", dummyLaundryItems,
            "Sun May 22 08:12:43 GMT+03:00 2022", "Order Not Started",
            54.8953516, 23.9282175, 54.9162135, 23.9546676,
            2.5, true, false, false, false, false, false);
    public static Order dummyUnassignedOrder = new Order ("qq3JQ6hzJFWTRPR1q6o2YaYqFMe2_0_ROAax2Y8kNX12d7ftY5QSCe71he2", "", dummyLaundryItems,
            "Thu May 19 08:54:07 GMT+03:00 2022", "Order Not Started",
            54.8953516, 23.9282175, 54.9162135, 23.9546676,
            2.5, true, false, false, false, false, false);
}
