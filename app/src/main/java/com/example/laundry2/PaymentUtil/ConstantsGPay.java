package com.example.laundry2.PaymentUtil;


/*
 * Copyright 2020 Google Inc.
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

import com.google.android.gms.wallet.WalletConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ConstantsGPay {
    public static final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;
    public static final List<String> SUPPORTED_NETWORKS = Arrays.asList (
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA");
    public static final List<String> SUPPORTED_METHODS = Arrays.asList (
            "PAN_ONLY",
            "CRYPTOGRAM_3DS");
    public static final String COUNTRY_CODE = "LT";
    public static final String CURRENCY_CODE = "EUR";
    public static final List<String> SHIPPING_SUPPORTED_COUNTRIES = Collections.singletonList ("LT");
    public static final String DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME";
    public static final HashMap<String, String> DIRECT_TOKENIZATION_PARAMETERS =
            new HashMap<String, String> () {{
                put ("protocolVersion", "ECv2");
                put ("publicKey", DIRECT_TOKENIZATION_PUBLIC_KEY);
            }};
}


