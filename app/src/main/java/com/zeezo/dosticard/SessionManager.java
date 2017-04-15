package com.zeezo.dosticard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by masho on 19-Feb-17.
 */
public class SessionManager {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context getContext;

    public SessionManager(Context context){
        this.getContext = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        editor.apply();
    }

    public void createLoginSession(int id, String firstName, String lastName, String pass, String contact_no, String address, int storeID, String designation) {

        editor.putBoolean("isLoggedIn", true);
        editor.putInt("Merchant_Id", id);
        editor.putString("First_name", firstName);
        editor.putString("Last_name", lastName);
        editor.putString("Password", pass);
        editor.putString("Contact_No", contact_no);
        editor.putString("Address", address);
        editor.putInt("StoreID", storeID);
        editor.putString("Designation", designation);
        editor.apply();

        //Toast.makeText(this.getContext, String.valueOf(preferences.getInt("StoreID", 0)), Toast.LENGTH_LONG).show();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean("isLoggedIn", false);
    }

    public HashMap<String, String> getMerchantInfo(){
        HashMap<String, String> merchant = new HashMap<String, String>();
        merchant.put("Merchant_Id", String.valueOf(preferences.getInt("Merchant_Id", 0)));
        merchant.put("First_name", preferences.getString("First_name", null));
        merchant.put("Last_name", preferences.getString("Last_name", null));
        merchant.put("Password", preferences.getString("Password", null));
        merchant.put("Contact_No", preferences.getString("Contact_No", null));
        merchant.put("Address", preferences.getString("Address", null));
        merchant.put("StoreID", String.valueOf(preferences.getInt("StoreID", 0)));
        merchant.put("Designation", preferences.getString("Designation", null));

        return merchant;
    }

    public void createStoreSession(int store_id, String store_name, String store_city, String store_address, int store_pointsLimit, int store_paymentToGetOnePoint, int store_discount) {
        editor.putInt("StoreID", store_id);
        editor.putString("StoreName", store_name);
        editor.putString("StoreCity", store_city);
        editor.putString("StoreAddress", store_address);
        editor.putInt("StorePointsLimit", store_pointsLimit);
        editor.putInt("StorePaymentToGetOnePoint", store_paymentToGetOnePoint);
        editor.putInt("StoreDiscount", store_discount);

        editor.apply();
    }

    public HashMap<String, String> getStoreInfo(){
        HashMap<String, String> store = new HashMap<String, String>();
        store.put("Store_ID", String.valueOf(preferences.getInt("StoreID", 0)));
        store.put("Store_Name", preferences.getString("StoreName", null));
        store.put("Store_City", preferences.getString("StoreCity", null));
        store.put("Store_Address", preferences.getString("StoreAddress", null));
        store.put("Store_pointsLimit", String.valueOf(preferences.getInt("StorePointsLimit", 0)));
        store.put("Store_paymentToGetOnePoint", String.valueOf(preferences.getInt("StorePaymentToGetOnePoint", 0)));
        store.put("Store_Discount", String.valueOf(preferences.getInt("StoreDiscount", 0)));

        return store;
    }
}