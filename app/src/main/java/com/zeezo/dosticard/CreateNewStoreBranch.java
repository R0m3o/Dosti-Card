package com.zeezo.dosticard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by masho on 08-Mar-17.
 */
public class CreateNewStoreBranch extends Activity {

    EditText storeName, storeAddress, maxPoints, paymentToGetOnePoint, discount;
    Button createStoreBtn;
    Spinner dropdown;

    SessionManager sessionManager;
    HashMap<String, String> merchant;
    ValidateField validate = new ValidateField();

    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_store);

        sessionManager = new SessionManager(getApplicationContext());
        merchant = sessionManager.getMerchantInfo();
        initializeComponents();

        createStoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate.isValidAddress(storeAddress.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid store address!", Toast.LENGTH_LONG).show();
                else {
                    if (isInternetConnected()) {
                        if (!storeAddress.getText().toString().trim().equals(bundle.getString("storeAddress"))) {
                            createStore();
                        }else {
                            Toast.makeText(getApplicationContext(), "Alert: Address should be different with parent store.", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
        });
    }

    private void initializeComponents() {
        storeName = (EditText) findViewById(R.id.idNewStoreName);
        dropdown = (Spinner) findViewById(R.id.idNewStoreCity);
        storeAddress = (EditText) findViewById(R.id.idNewStoreAddress);
        maxPoints = (EditText) findViewById(R.id.idNewStoreSetPoints);
        paymentToGetOnePoint = (EditText) findViewById(R.id.idNewStorePaymentToGetOnePoint);
        discount = (EditText) findViewById(R.id.idNewStoreSetDiscount);
        createStoreBtn = (Button) findViewById(R.id.idBtnCreateStore);

        bundle = getIntent().getExtras();


        storeName.setText(bundle.getString("storeName"));
        maxPoints.setText(String.valueOf(bundle.getInt("storePointsLimit")));
        paymentToGetOnePoint.setText(String.valueOf(bundle.getInt("storePaymentToGetOnePoint")));
        discount.setText(String.valueOf(bundle.getInt("storeDiscount")));

        String[] cities = new String[]{"Lahore", "Islamabad", "Karachi"};
        //Toast.makeText(getApplicationContext(), city, Toast.LENGTH_LONG).show();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cities);
        dropdown.setAdapter(adapter);

        String city = bundle.getString("storeCity");
        int cityPosition = adapter.getPosition(city);
        dropdown.setSelection(cityPosition);

        storeName.setEnabled(false);
        maxPoints.setEnabled(false);
        paymentToGetOnePoint.setEnabled(false);
        discount.setEnabled(false);

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

    private void createStore() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "", false, false);

        final String store_name = storeName.getText().toString().trim();
        final String store_city = dropdown.getSelectedItem().toString();
        final String store_address = storeAddress.getText().toString().trim();
        final String store_maxPoints = maxPoints.getText().toString().trim();
        final String store_paymentToGetOnePoint = paymentToGetOnePoint.getText().toString().trim();
        final String store_discount = discount.getText().toString().trim();

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Successfully create a store", Toast.LENGTH_LONG).show();
                        getStoreID(store_address);
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), "Server Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
                params.put("StoreName", store_name);
                params.put("City", store_city);
                params.put("Address", store_address);
                params.put("PointsLimit", store_maxPoints);
                params.put("PaymentToGetOnePoint", store_paymentToGetOnePoint);
                params.put("PercentageDiscount", store_discount);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(postRequest);
    }

    private void getStoreID(String store_address) {
        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store/getStoreByAddress/"+store_address.replace(" ", "%20");

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int storeID = Integer.valueOf(response.getString("ID"));
                            //Toast.makeText(getContext(), String.valueOf(storeID), Toast.LENGTH_LONG).show();
                            updateStoreIdAndDesignationToMerchant(storeID);
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        Volley.newRequestQueue(this).add(objectRequest);
    }

    private void updateStoreIdAndDesignationToMerchant(final int storeID) {

        final int merchantId = Integer.valueOf(merchant.get("Merchant_Id"));
        final String designation = "Admin";

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant/updateStoreIdAndDesignation/"+merchantId+"/"+storeID+"/"+designation;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_LONG).show();
                        sessionManager.createLoginSession(Integer.parseInt(merchant.get("Merchant_Id")), merchant.get("First_name"), merchant.get("Last_name"),
                                merchant.get("Password"), merchant.get("Contact_No"), merchant.get("Address"), storeID, designation);
                        Intent startMerchantProfile = new Intent(getApplicationContext(), MerchantProfile.class);
                        startActivity(startMerchantProfile);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
