package com.yili.unityandroidplugin.sdk;

import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.common.collect.ImmutableList;
import com.unity3d.player.UnityPlayer;
import com.yili.unityandroidplugin.MsgType;
import com.yili.unityandroidplugin.UnityBridge;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IAP {
    private static IAP _Instance;
    private List<ProductDetails> _productDetailsList;
    private Purchase _purchaseSuccess;
    private String _productId;

    public static IAP Instance() {
        if (_Instance == null) {
            _Instance = new IAP();
        }
        return _Instance;
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    // Process the purchase as described in the next section.
                    _purchaseSuccess = purchase;
                    NotifyGoogle();
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user canceling the purchase flow.
                try {
                    JSONObject json = new JSONObject();
                    json.put("msgType", MsgType.PurchaseFail);
                    UnityBridge.Java2Unity(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle any other error codes.
                try {
                    JSONObject json = new JSONObject();
                    json.put("msgType", MsgType.PurchaseFail);
                    UnityBridge.Java2Unity(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private BillingClient billingClient = BillingClient.newBuilder(UnityPlayer.currentActivity)
            .setListener(purchasesUpdatedListener)
            // Configure other settings.
            .build();

    public void ConnectionGooglePlay(String productId) {
        this._productId = productId;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Query(productId);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("iap", "Billing Service Disconnected.");
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            ConnectionGooglePlay(_productId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer.schedule(task, 5000);
            }
        });
    }

    public void Query(String productId) {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(productId)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build()))
                        .build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {
                        // check billingResult
                        // process returned productDetailsList
                        _productDetailsList = productDetailsList;
                        Buy(productId);
                    }
                }
        );
    }

    public void Buy(String productId) {
        ProductDetails willBuyProductDetails = null;
        for (ProductDetails productDetails : _productDetailsList) {
            if (productDetails.getProductId().equals(productId)) {
                willBuyProductDetails = productDetails;
            }
        }
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(willBuyProductDetails)
                                // For one-time products, "setOfferToken" method shouldn't be called.
                                // For subscriptions, to get an offer token, call
                                // ProductDetails.subscriptionOfferDetails() for a list of offers
                                // that are available to the user.
//                                .setOfferToken(selectedOfferToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

// Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(UnityPlayer.currentActivity, billingFlowParams);
    }

    public void NotifyGoogle() {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(_purchaseSuccess.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    try {
                        JSONObject json = new JSONObject();
                        json.put("msgType", MsgType.PurchaseEnd);
                        json.put("id", _productId);
                        UnityBridge.Java2Unity(json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("msgType", MsgType.PurchaseFail);
                        UnityBridge.Java2Unity(json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }
}
