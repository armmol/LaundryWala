package com.example.laundry2.Services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.laundry2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationSender {
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey = "AAAAaq8hpT0:APA91bGKqTKAGgbRAVO1_R34M3UkreGzwgEwNQxZ-xUg0DPURspqSWvr-ExRvyLTbtnUWGW7lm2tsFiEUn88L9GWC2PopHG86kiCKqg_lvR0EZQ3YDotiaFmsyIAbtg1WI4IE7yc6GB7";
    String userFcmToken;
    String title;
    String body;
    Context mContext;
    private RequestQueue requestQueue;

    public NotificationSender (String userFcmToken, String title, String body, Context mContext) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
    }

    public void SendNotifications () {
        requestQueue = Volley.newRequestQueue (mContext);
        JSONObject mainObj = new JSONObject ();
        try {
            mainObj.put ("to", userFcmToken);
            JSONObject notiObject = new JSONObject ();
            notiObject.put ("title", title);
            notiObject.put ("body", body);
            notiObject.put ("icon", R.drawable.customer);
            mainObj.put ("notification", notiObject);

            JsonObjectRequest request = new JsonObjectRequest (Request.Method.POST, postUrl, mainObj,
                    response -> {
                        // code run is got response
                    }, error -> {
                        // code run is got error
            }) {
                @Override
                public Map<String, String> getHeaders () {
                    Map<String, String> header = new HashMap<> ();
                    header.put ("content-type", "application/json");
                    header.put ("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            requestQueue.add (request);
        } catch (JSONException e) {
            e.printStackTrace ();
        }
    }
}
