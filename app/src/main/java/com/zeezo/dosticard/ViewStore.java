package com.zeezo.dosticard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
 * Created by masho on 27-Feb-17.
 */
public class ViewStore extends Activity {

    LinearLayout storeViewLayout;
    TextView store_name;
    TextView store_city;
    TextView store_Address;
    TextView store_pointLimit;
    TextView store_paymentToGetOnePoiint;
    TextView storeDiscount;

    EditText storeName, storeAddress, storePointsLimit, storePaymentToGetOnePoiint, store_discount;
    Spinner storeCity;

    Button joinStore, editStore, update;

    SessionManager session;
    HashMap<String, String> storeInfo, merchant;

    ValidateField validate = new ValidateField();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_view);

        initialization();
        updateView();
    }

    private void initialization() {
        storeViewLayout = (LinearLayout) findViewById(R.id.idStoreViewLayout);
        store_name = (TextView) findViewById(R.id.idStoreViewName);
        store_city = (TextView) findViewById(R.id.idStoreViewCity);
        store_Address = (TextView) findViewById(R.id.idStoreViewAddress);
        store_pointLimit = (TextView) findViewById(R.id.idStoreViewPointsLimit);
        store_paymentToGetOnePoiint = (TextView) findViewById(R.id.idStoreViewPaymentToGetOnePoint);
        storeDiscount = (TextView) findViewById(R.id.idStoreViewDiscount);

        editStore = (Button) findViewById(R.id.idStoreViewEditBtn);
        joinStore = (Button) findViewById(R.id.idJoinExistingStoreJoinBtn);
        joinStore.setVisibility(View.GONE);
        update = (Button) findViewById(R.id.idUpdateStore);
        update.setVisibility(View.GONE);

        session = new SessionManager(getApplicationContext());
        storeInfo = session.getStoreInfo();
        merchant = session.getMerchantInfo();

        storeName = (EditText) findViewById(R.id.idUpdateStoreName);
        storeCity = (Spinner) findViewById(R.id.idUpdateStoreCity);
        storeAddress = (EditText) findViewById(R.id.idUpdateStoreAddress);
        storePointsLimit = (EditText) findViewById(R.id.idUpdateStorePointsLimit);
        storePaymentToGetOnePoiint = (EditText) findViewById(R.id.idUpdateStorePaymentToGetOnePoint);
        store_discount = (EditText) findViewById(R.id.idUpdateStoreDiscount);

        editStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEditView();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate.isValidStoreName(storeName.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid store name!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidAddress(storeAddress.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid store address!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidNumbers(storePointsLimit.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Points should be between 1 to 100!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidNumbers(store_discount.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Discount should be between 1 to 100!", Toast.LENGTH_LONG).show();
                else {
                    updateStore(storeInfo.get("Store_Name"), storeInfo.get("Store_Address"));
                }
            }
        });

        if (merchant.get("Designation").equals("Admin"))
            editStore.setVisibility(View.VISIBLE);
        else
            editStore.setVisibility(View.GONE);

    }

    private void updateView(){
        store_name.setText(storeInfo.get("Store_Name"));
        store_city.setText(storeInfo.get("Store_City"));
        store_Address.setText(storeInfo.get("Store_Address"));
        store_pointLimit.setText(storeInfo.get("Store_pointsLimit"));
        store_paymentToGetOnePoiint.setText(storeInfo.get("Store_paymentToGetOnePoint"));
        storeDiscount.setText(storeInfo.get("Store_Discount")+"%");
    }

    private void updateEditView(){
        editStore.setVisibility(View.GONE);
        joinStore.setVisibility(View.GONE);
        store_name.setVisibility(View.GONE);
        store_city.setVisibility(View.GONE);
        store_Address.setVisibility(View.GONE);
        store_pointLimit.setVisibility(View.GONE);
        store_paymentToGetOnePoiint.setVisibility(View.GONE);
        storeDiscount.setVisibility(View.GONE);

        update.setVisibility(View.VISIBLE);
        storeName.setVisibility(View.VISIBLE);
        storeCity.setVisibility(View.VISIBLE);
        storeAddress.setVisibility(View.VISIBLE);
        storePointsLimit.setVisibility(View.VISIBLE);
        storePaymentToGetOnePoiint.setVisibility(View.VISIBLE);
        store_discount.setVisibility(View.VISIBLE);

        storeName.setText(storeInfo.get("Store_Name"));
        storeAddress.setText(storeInfo.get("Store_Address"));
        storePointsLimit.setText(storeInfo.get("Store_pointsLimit"));
        storePaymentToGetOnePoiint.setText(storeInfo.get("Store_paymentToGetOnePoint"));
        store_discount.setText(storeInfo.get("Store_Discount"));
        String[] cities = new String[]{"Lahore", "Islamabad", "Karachi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cities);
        storeCity.setAdapter(adapter);
    }

    private void updateStore(String store_name, String store_address) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Updating...", false, false);

        final String name = storeName.getText().toString().trim();
        final String city = storeCity.getSelectedItem().toString();
        final String address = storeAddress.getText().toString().trim();
        final String pointsLimit = storePointsLimit.getText().toString().trim();
        final String paymentToGetOnePount = storePaymentToGetOnePoiint.getText().toString().trim();
        final String discount = store_discount.getText().toString().trim();

        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store/updateStore/"+store_name+"/"+store_address.replace(" ","%20");

        StringRequest request = new StringRequest(Request.Method.PUT, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        session.createStoreSession(Integer.parseInt(storeInfo.get("Store_ID")), name, city, address, Integer.parseInt(pointsLimit),
                                Integer.parseInt(paymentToGetOnePount), Integer.parseInt(discount));
                        progressDialog.dismiss();
                        startActivity(getIntent());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Error:"+ error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("StoreName", name);
                params.put("City", city);
                params.put("Address", address);
                params.put("PointsLimit", pointsLimit);
                params.put("PaymentToGetOnePoint", paymentToGetOnePount);
                params.put("PercentageDiscount", discount);

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

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
