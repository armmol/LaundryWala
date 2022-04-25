/*
 * Copyright 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.laundry2.PaymentUtil;

import android.content.Context;

import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * <p>Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
public class PaymentsUtil {

    public static final BigDecimal CENTS_IN_A_UNIT = new BigDecimal (100);
    private static JSONObject getBaseRequest () throws JSONException {
        return new JSONObject ().put ("apiVersion", 2).put ("apiVersionMinor", 0);
    }
    public static PaymentsClient createPaymentsClient (Context context) {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder ().setEnvironment (ConstantsGPay.PAYMENTS_ENVIRONMENT).build ();
        return Wallet.getPaymentsClient (context, walletOptions);
    }
    private static JSONObject getGatewayTokenizationSpecification () throws JSONException {
        return new JSONObject () {{
            put ("type", "PAYMENT_GATEWAY");
            put ("parameters", new JSONObject () {{
                put ("gateway", "example");
                put ("gatewayMerchantId", "exampleGatewayMerchantId");
            }});
        }};
    }
    private static JSONArray getAllowedCardNetworks () {
        return new JSONArray (ConstantsGPay.SUPPORTED_NETWORKS);
    }

    private static JSONArray getAllowedCardAuthMethods () {
        return new JSONArray (ConstantsGPay.SUPPORTED_METHODS);
    }

    private static JSONObject getBaseCardPaymentMethod () throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject ();
        cardPaymentMethod.put ("type", "CARD");
        JSONObject parameters = new JSONObject ();
        parameters.put ("allowedAuthMethods", getAllowedCardAuthMethods ());
        parameters.put ("allowedCardNetworks", getAllowedCardNetworks ());
        parameters.put ("billingAddressRequired", true);
        JSONObject billingAddressParameters = new JSONObject ();
        billingAddressParameters.put ("format", "FULL");
        parameters.put ("billingAddressParameters", billingAddressParameters);
        cardPaymentMethod.put ("parameters", parameters);
        return cardPaymentMethod;
    }

    private static JSONObject getCardPaymentMethod () throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod ();
        cardPaymentMethod.put ("tokenizationSpecification", getGatewayTokenizationSpecification ());

        return cardPaymentMethod;
    }

    public static JSONObject getIsReadyToPayRequest () {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest ();
            isReadyToPayRequest.put (
                    "allowedPaymentMethods", new JSONArray ().put (getBaseCardPaymentMethod ()));

            return isReadyToPayRequest;

        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject getTransactionInfo (String price) throws JSONException {
        JSONObject transactionInfo = new JSONObject ();
        transactionInfo.put ("totalPrice", price);
        transactionInfo.put ("totalPriceStatus", "FINAL");
        transactionInfo.put ("countryCode", ConstantsGPay.COUNTRY_CODE);
        transactionInfo.put ("currencyCode", ConstantsGPay.CURRENCY_CODE);
        transactionInfo.put ("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE");

        return transactionInfo;
    }

    private static JSONObject getMerchantInfo () throws JSONException {
        return new JSONObject ().put ("merchantName", "Example Merchant");
    }

    public static JSONObject getPaymentDataRequest (long priceCents) {

        final String price = PaymentsUtil.centsToString (priceCents);

        try {
            JSONObject paymentDataRequest = PaymentsUtil.getBaseRequest ();
            paymentDataRequest.put (
                    "allowedPaymentMethods", new JSONArray ().put (PaymentsUtil.getCardPaymentMethod ()));
            paymentDataRequest.put ("transactionInfo", PaymentsUtil.getTransactionInfo (price));
            paymentDataRequest.put ("merchantInfo", PaymentsUtil.getMerchantInfo ());
            paymentDataRequest.put ("shippingAddressRequired", true);
            JSONObject shippingAddressParameters = new JSONObject ();
            shippingAddressParameters.put ("phoneNumberRequired", false);
            JSONArray allowedCountryCodes = new JSONArray (ConstantsGPay.SHIPPING_SUPPORTED_COUNTRIES);
            shippingAddressParameters.put ("allowedCountryCodes", allowedCountryCodes);
            paymentDataRequest.put ("shippingAddressParameters", shippingAddressParameters);
            return paymentDataRequest;

        } catch (JSONException e) {
            return null;
        }
    }

    public static String centsToString (long cents) {
        return new BigDecimal (cents)
                .divide (CENTS_IN_A_UNIT, RoundingMode.HALF_EVEN)
                .setScale (2, RoundingMode.HALF_EVEN)
                .toString ();
    }
}
