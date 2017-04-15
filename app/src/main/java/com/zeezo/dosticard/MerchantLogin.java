package com.zeezo.dosticard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class MerchantLogin extends AppCompatActivity implements View.OnClickListener {

    EditText contact, password;
    Button startMerchantRegister;
    Button btnLogin;

    SessionManager sessionManager;

    LinearLayout loginLayout;

    ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merchant_login);

        if (isInternetConnected()){
            sessionManager = new SessionManager(getApplicationContext());
            if (!sessionManager.isLoggedIn()) {
                initialization();

                startMerchantRegister.setOnClickListener(this);
                btnLogin.setOnClickListener(this);

            } else {
                Intent startMerchantProfileActivity = new Intent(MerchantLogin.this, com.zeezo.dosticard.MerchantProfile.class);
                MerchantLogin.this.startActivity(startMerchantProfileActivity);
            }
        }else {
            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantLoginLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
            internetAccess.setAction("Retry", new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(getIntent());
                    internetAccess.dismiss();
                }
            }).show();
        }
    }

    private boolean isInternetConnected() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTING)
            return true;
        else
            return false;
    }

    private void initialization() {
        contact = (EditText) findViewById(R.id.idLoginContact);
        password = (EditText) findViewById(R.id.idLoginPassword);
        startMerchantRegister = (Button) findViewById(R.id.idBtnStartRegistration);
        btnLogin = (Button) findViewById(R.id.idBtnLogin);

        loginLayout = (LinearLayout) findViewById(R.id.idMerchantLoginLayout);
    }

    @Override
    public void onClick(View view) {
        if (isInternetConnected()) {
            int id = view.getId();
            switch (id) {
                case R.id.idBtnStartRegistration:
                    startRegistrationActivity();
                    break;
                case R.id.idBtnLogin:
                    if (contact.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter contact number!", Toast.LENGTH_LONG).show();
                    else if (password.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
                    else {
                        validateMerchant();
                    }
                    break;
            }
        }else {
            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantLoginLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
            internetAccess.setAction("Retry", new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(getIntent());
                    internetAccess.dismiss();
                }
            }).show();
        }
    }

    public void startRegistrationActivity() {
        Intent startRegistrationActivity = new Intent(MerchantLogin.this, com.zeezo.dosticard.MerchantRegistration.class);
        MerchantLogin.this.startActivity(startRegistrationActivity);
    }

    private void validateMerchant() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Logging...", false, false);

        String contact_num = contact.getText().toString().trim();
        String pass = password.getText().toString().trim();

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant/" + contact_num + "/" + pass;
        //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int ID = 0;
                        String firstName = "";
                        String lastName = "";
                        String pass = "";
                        String contact_no = "";
                        String address = "";
                        int storeID = 0;
                        String designation = "";

                        try {
                            ID = response.getInt("ID");
                            firstName = response.getString("Fname");
                            lastName = response.getString("Lname");
                            pass = response.getString("Password");
                            contact_no = response.getString("ContactNo");
                            address = response.getString("Address");
                            storeID = response.getInt("StoreID");
                            designation = response.getString("Designation");
                        } catch (JSONException e) {
                            //Toast.makeText(getApplicationContext(), "Server Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        sessionManager.createLoginSession(ID, firstName, lastName, pass, contact_no, address, storeID, designation);

                        progressDialog.dismiss();
                        Intent startMerchantProfileActivity = new Intent(MerchantLogin.this, com.zeezo.dosticard.MerchantProfile.class);
                        MerchantLogin.this.startActivity(startMerchantProfileActivity);
                        //Toast.makeText(getApplicationContext(), String.valueOf(storeID), Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (!isInternetConnected()) {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantLoginLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                            internetAccess.setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(getIntent());
                                    internetAccess.dismiss();
                                }
                            }).show();
                        }else
                            Toast.makeText(getApplicationContext(), "Error: You have entered wrong contact number or password!", Toast.LENGTH_LONG).show();
                    }
                }
        );

        Volley.newRequestQueue(MerchantLogin.this).add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            loginLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background_landscape));
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            loginLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }
}
