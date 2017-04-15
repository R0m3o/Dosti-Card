package com.zeezo.dosticard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import java.util.Random;

/**
 * Created by masho on 24-Jan-17.
 */
public class NewStore extends Fragment {

    EditText storeName, storeAddress, maxPoints, paymetToGetOnePoint, discount;
    Button createStoreBtn;
    Spinner dropdown;

    SessionManager sessionManager;
    HashMap<String, String> merchant;
    ValidateField validate = new ValidateField();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View newStoreView = inflater.inflate(R.layout.new_store, container, false);
        return newStoreView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());
        merchant = sessionManager.getMerchantInfo();
        initializeComponents(view);

        createStoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate.isValidStoreName(storeName.getText().toString().trim()))
                    Toast.makeText(getContext(), "Invalid store name!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidAddress(storeAddress.getText().toString().trim()))
                    Toast.makeText(getContext(), "Invalid store address!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidNumbers(maxPoints.getText().toString().trim()))
                    Toast.makeText(getContext(), "Points should be between 1 to 100!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidNumbers(discount.getText().toString().trim()))
                    Toast.makeText(getContext(), "Discount should be between 1 to 100!", Toast.LENGTH_LONG).show();
                else {
                    if (isInternetConnected()) {
                        //createStore();
                        isStoreAlreadyExist(storeName.getText().toString().trim(), storeAddress.getText().toString().trim());
                    }else {
                        final Snackbar internetAccess = Snackbar.make(view.findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                        internetAccess.setAction("Retry", new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                startActivity(getActivity().getIntent());
                                internetAccess.dismiss();
                            }
                        }).show();
                    }
                }
            }
        });
    }

    private void initializeComponents(View v) {
        storeName = (EditText) v.findViewById(R.id.idNewStoreName);
        dropdown = (Spinner) v.findViewById(R.id.idNewStoreCity);
        storeAddress = (EditText) v.findViewById(R.id.idNewStoreAddress);
        maxPoints = (EditText) v.findViewById(R.id.idNewStoreSetPoints);
        paymetToGetOnePoint = (EditText) v.findViewById(R.id.idNewStorePaymentToGetOnePoint);
        discount = (EditText) v.findViewById(R.id.idNewStoreSetDiscount);
        createStoreBtn = (Button) v.findViewById(R.id.idBtnCreateStore);

        String[] cities = new String[]{"Lahore", "Islamabad", "Karachi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, cities);
        dropdown.setAdapter(adapter);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTING)
            return true;
        else
            return false;
    }

    private void isStoreAlreadyExist(String name, String address) {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "Checking user exist or not!", false, false);

        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store/isStoreAlreadyExist/" + name +"/"+ address.replace(" ", "%20");


        StringRequest request = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        if (response.equals("true")) {
                            Toast.makeText(getContext(), "Store already exist! please put different store name or address.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            createStore();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Error: "+error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                            internetAccess.setAction("Retry", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    startActivity(getActivity().getIntent());
                                    internetAccess.dismiss();
                                }
                            }).show();
                        }
                    }
                }
        );
        Volley.newRequestQueue(getContext()).add(request);
    }

    private void createStore() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "Creating...", false, false);

        final String store_name = storeName.getText().toString().trim();
        final String store_city = dropdown.getSelectedItem().toString();
        final String store_address = storeAddress.getText().toString().trim();
        final String store_maxPoints = maxPoints.getText().toString().trim();
        final String store_paymentToGetOnepoint = paymetToGetOnePoint.getText().toString().trim();
        final String store_discount = discount.getText().toString().trim();

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Snackbar.make(getView().findViewById(R.id.idNewStoreLayout), "Successfully Created", Snackbar.LENGTH_LONG).show();
                        getStoreID(store_address);
                        progressDialog.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Server Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                            internetAccess.setAction("Retry", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    startActivity(getActivity().getIntent());
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
                params.put("PaymentToGetOnePoint", store_paymentToGetOnepoint);
                params.put("PercentageDiscount", store_discount);

                return params;
            }
        };

        Volley.newRequestQueue(getContext()).add(postRequest);
    }

    private void getStoreID(String store_address) {
        String storeAddress = store_address.replace(" ", "%20");
        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store/getStoreByAddress/"+storeAddress;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int storeID = Integer.valueOf(response.getString("ID"));
                            //Toast.makeText(getContext(), String.valueOf(storeID), Toast.LENGTH_LONG).show();
                            updateStoreIdAndDesignationToMerchant(storeID);
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        Volley.newRequestQueue(getContext()).add(objectRequest);
    }

    private void updateStoreIdAndDesignationToMerchant(final int storeID) {

        final int merchantId = Integer.valueOf(merchant.get("Merchant_Id"));
        final String designation = "Admin";

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant/updateStoreIdAndDesignation/"+merchantId+"/"+storeID+"/"+designation;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Snackbar.make(getView().findViewById(R.id.idNewStoreLayout), "Successfully Added", Snackbar.LENGTH_LONG).show();
                        sessionManager.createLoginSession(Integer.parseInt(merchant.get("Merchant_Id")), merchant.get("First_name"), merchant.get("Last_name"),
                                merchant.get("Password"), merchant.get("Contact_No"), merchant.get("Address"), storeID, designation);
                        Intent startMerchantProfile = new Intent(getContext(), MerchantProfile.class);
                        startActivity(startMerchantProfile);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Server Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idNewStoreLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                            internetAccess.setAction("Retry", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    startActivity(getActivity().getIntent());
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
        Volley.newRequestQueue(getContext()).add(request);
    }

}
