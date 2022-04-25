package com.example.laundry2.Contract;

import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.AuthState;

public interface PaymentsContract {
    void canPayWithGooglePay ();

    void loadPaymentDataForGPay(long cents);

    MutableLiveData<AuthState> getAuthStateMutableLiveData ();
}
