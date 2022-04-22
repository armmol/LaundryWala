package com.example.laundry2;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.PaymentsContract;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.Repositories.ApplicationRepository;

public class PaymentsViewModel extends AndroidViewModel implements PaymentsContract {

    private final ApplicationRepository applicationRepository;
    private final MutableLiveData<AuthState> authStateMutableLiveData;

    public PaymentsViewModel (Application application) {
        super (application);
        this.applicationRepository = new ApplicationRepository (application);
        authStateMutableLiveData = applicationRepository.getAuthStateMutableLiveData ();
    }

    @Override
    public void payWithGooglePay () {

    }

    @Override
    public void payWithPaySera () {

    }

    @Override
    public MutableLiveData<AuthState> getAuthStateMutableLiveData () {
        return authStateMutableLiveData;
    }
}
