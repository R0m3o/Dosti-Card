package com.zeezo.dosticard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by masho on 08-Mar-17.
 */
public class JoinExistingStore extends Activity {

    LinearLayout storeViewLayout;
    TextView store_name, store_city, store_Address, store_pointLimit, store_paymentToGetOnePoint, storeDiscount;

    Button joinExistingStore, updateBtn, editBtn;

    Bundle getBundle;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_view);

        sessionManager = new SessionManager(getApplicationContext());

        initialization();
        updateView();

        joinExistingStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int storeID = getBundle.getInt("storeID");
                if (isInternetConnected()) {
                    updateStoreIdAndDesignationToMerchant(storeID);
                }else {
                    final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idStoreViewLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                    internetAccess.setAction("Retry", new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            startActivity(getIntent());
                            internetAccess.dismiss();
                        }
                    }).show();
                }
            }
        });
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTING)
            return true;
        else
            return false;
    }

    private void initialization() {
        storeViewLayout = (LinearLayout) findViewById(R.id.idStoreViewLayout);
        store_name = (TextView) findViewById(R.id.idStoreViewName);
        store_city = (TextView) findViewById(R.id.idStoreViewCity);
        store_Address = (TextView) findViewById(R.id.idStoreViewAddress);
        store_pointLimit = (TextView) findViewById(R.id.idStoreViewPointsLimit);
        store_paymentToGetOnePoint = (TextView) findViewById(R.id.idStoreViewPaymentToGetOnePoint);
        storeDiscount = (TextView) findViewById(R.id.idStoreViewDiscount);
        joinExistingStore = (Button) findViewById(R.id.idJoinExistingStoreJoinBtn);
        joinExistingStore.setVisibility(View.VISIBLE);

        updateBtn = (Button) findViewById(R.id.idUpdateStore);
        updateBtn.setVisibility(View.GONE);
        editBtn = (Button)findViewById(R.id.idStoreViewEditBtn);
        editBtn.setVisibility(View.GONE);
        getBundle = getIntent().getExtras();

    }

    private void updateView(){
        store_name.setText(getBundle.getString("storeName"));
        store_city.setText(getBundle.getString("storeCity"));
        store_Address.setText(getBundle.getString("storeAddress"));
        store_pointLimit.setText(String.valueOf(getBundle.getInt("storePointsLimit")));
        store_paymentToGetOnePoint.setText(String.valueOf(getBundle.getInt("storePaymentToGetOnePoint")));
        storeDiscount.setText(String.valueOf(getBundle.getInt("storeDiscount"))+"%");
    }

    private void updateStoreIdAndDesignationToMerchant(final int storeID) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Sending Request", false, false);

        final HashMap<String, String> merchant = sessionManager.getMerchantInfo();
        final int merchantId = Integer.valueOf(merchant.get("Merchant_Id"));
        final String designation = "Moderator";

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant/updateStoreIdAndDesignation/"+merchantId+"/"+storeID+"/"+designation;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        //Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_LONG).show();
                        sessionManager.createLoginSession(Integer.parseInt(merchant.get("Merchant_Id")), merchant.get("First_name"), merchant.get("Last_name"),
                                merchant.get("Password"), merchant.get("Contact_No"), merchant.get("Address"), storeID, designation);
                        Intent startMerchantProfile = new Intent(JoinExistingStore.this, MerchantProfile.class);
                        JoinExistingStore.this.startActivity(startMerchantProfile);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), "Server Error: "+ error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idStoreViewLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                            internetAccess.setAction("Retry", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    startActivity(getIntent());
                                    internetAccess.dismiss();
                                }
                            }).show();
                        }
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("StoreID", String.valueOf(storeID));
                params.put("Designation", designation);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            storeViewLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background_landscape));
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            storeViewLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }

}
