package com.zeezo.dosticard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by masho on 22-Jan-17.
 */
public class MerchantProfile extends Activity implements View.OnClickListener{

    TextView name, designation;
    Button joinStore, viewStore, logout, viewCustomer, profileSetting, readTag, writeTag;

    ImageView dp;

    LinearLayout merchantProfileLayout;

    SessionManager session;
    HashMap<String, String> merchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merchant_profile);

        session = new SessionManager(getApplicationContext());
        merchant = session.getMerchantInfo();

        initialization();
        setMerchantInfo();

        joinStore.setOnClickListener(this);
        viewStore.setOnClickListener(this);
        logout.setOnClickListener(this);
        viewCustomer.setOnClickListener(this);
        profileSetting.setOnClickListener(this);
        readTag.setOnClickListener(this);
        writeTag.setOnClickListener(this);
        dp.setOnClickListener(this);
    }

    private void initialization() {
        merchantProfileLayout = (LinearLayout) findViewById(R.id.idMerchantProfileLayout);
        name = (TextView) findViewById(R.id.idMerchantProfileName);
        designation = (TextView) findViewById(R.id.idMerchantProfileDesignation);
        joinStore = (Button) findViewById(R.id.idMerchantProfileJoinStore);
        viewStore = (Button) findViewById(R.id.idMerchantProfileViewStore);
        logout = (Button) findViewById(R.id.idMerchantProfileLogout);
        viewCustomer = (Button) findViewById(R.id.idMerchantProfileViewCustomer);
        profileSetting = (Button) findViewById(R.id.idMerchantProfileSetting);
        readTag = (Button) findViewById(R.id.idReadTag);
        writeTag = (Button) findViewById(R.id.idWriteTag);
        dp = (ImageView) findViewById(R.id.idMerchantProfilePhoto);

        if (Integer.valueOf(merchant.get("StoreID")) == 0){
            viewStore.setVisibility(View.GONE);
            joinStore.setVisibility(View.VISIBLE);
        }else {
            viewStore.setVisibility(View.VISIBLE);
            joinStore.setVisibility(View.GONE);

            getStoreInfo();
        }
    }

    private void setMerchantInfo() {

        String fName = merchant.get("First_name");
        String lName = merchant.get("Last_name");
        String fullName = fName+" "+lName;
        name.setText(fullName);
        if (merchant.get("Designation").equals("")){
            designation.setText("Designation(It will show when u join a store)");
        }else {
            designation.setText(merchant.get("Designation"));
        }
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

    @Override
    public void onClick(View view) {
        if (isInternetConnected()) {
            int id = view.getId();
            switch (id) {
                case R.id.idMerchantProfileJoinStore:
                    Intent startJoinStoreActivity = new Intent(MerchantProfile.this, JoinStore.class);
                    MerchantProfile.this.startActivity(startJoinStoreActivity);
                    break;

                case R.id.idMerchantProfileViewStore:
                    Intent startViewStore = new Intent(MerchantProfile.this, ViewStore.class);
                    MerchantProfile.this.startActivity(startViewStore);
                    break;

                case R.id.idMerchantProfileViewCustomer:
                    if (Integer.valueOf(merchant.get("StoreID")) == 0){
                        Snackbar.make(findViewById(R.id.idMerchantProfileLayout), "Please Join Store First...", Snackbar.LENGTH_LONG).
                                setAction("Join", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent startJoinStoreActivity = new Intent(MerchantProfile.this, JoinStore.class);
                                        MerchantProfile.this.startActivity(startJoinStoreActivity);
                                    }
                                }).show();
                    }else {
                        Intent startListOfCustomer = new Intent(MerchantProfile.this, ListOfCustomer.class);
                        MerchantProfile.this.startActivity(startListOfCustomer);
                    }
                    break;

                case R.id.idMerchantProfileSetting:
                    Intent startUpdateMerchant = new Intent(MerchantProfile.this, UpdateMerchant.class);
                    MerchantProfile.this.startActivity(startUpdateMerchant);
                    break;

                case R.id.idMerchantProfileLogout:
                    if (session.isLoggedIn()) {
                        session.editor.clear();
                        session.editor.apply();

                        Intent startMerchantLogin = new Intent(MerchantProfile.this, MerchantLogin.class);
                        startMerchantLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startMerchantLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MerchantProfile.this.startActivity(startMerchantLogin);
                    }
                    break;

                case R.id.idReadTag:
                    if (Integer.valueOf(merchant.get("StoreID")) == 0){
                        Snackbar.make(findViewById(R.id.idMerchantProfileLayout), "Please Join Store First...", Snackbar.LENGTH_LONG).
                                setAction("Join", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent startJoinStoreActivity = new Intent(MerchantProfile.this, JoinStore.class);
                                        MerchantProfile.this.startActivity(startJoinStoreActivity);
                                    }
                                }).show();
                    }else {
                        Intent startReadTag = new Intent(MerchantProfile.this, ReadTag.class);
                        MerchantProfile.this.startActivity(startReadTag);
                    }
                    break;

                case R.id.idWriteTag:
                    if (Integer.valueOf(merchant.get("StoreID")) == 0){
                        Snackbar.make(findViewById(R.id.idMerchantProfileLayout), "Please Join Store First...", Snackbar.LENGTH_LONG).
                                setAction("Join", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent startJoinStoreActivity = new Intent(MerchantProfile.this, JoinStore.class);
                                        MerchantProfile.this.startActivity(startJoinStoreActivity);
                                    }
                                }).show();
                    }else {
                        Intent startWriteTag = new Intent(MerchantProfile.this, CustomerRegistration.class);
                        MerchantProfile.this.startActivity(startWriteTag);
                    }
                    break;

                case R.id.idMerchantProfilePhoto:
                    Intent startCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(startCamera, 0);
                    break;

            }
        }else {
            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantProfileLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
            internetAccess.setAction("Retry", new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(getIntent());
                    internetAccess.dismiss();
                }
            }).show();
        }
    }

    private void getStoreInfo() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "", false, false);

        int storeID = Integer.valueOf(merchant.get("StoreID"));
        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store/"+storeID;

        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int store_id = 0;
                        String store_name = "";
                        String store_city = "";
                        String store_address = "";
                        int store_pointsLimit = 0;
                        int store_paymentToGetOnePoint = 0;
                        int store_discount = 0;

                        try {
                            store_id = response.getInt("ID");
                            store_name = response.getString("StoreName");
                            store_city = response.getString("City");
                            store_address = response.getString("Address");
                            store_pointsLimit = response.getInt("PointsLimit");
                            store_paymentToGetOnePoint = response.getInt("PaymentToGetOnePoint");
                            store_discount = response.getInt("PercentageDiscount");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        session.createStoreSession(store_id, store_name, store_city, store_address, store_pointsLimit, store_paymentToGetOnePoint, store_discount);
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        Volley.newRequestQueue(MerchantProfile.this).add(objRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if (requestCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            dp.setImageBitmap(bitmap);
        //}
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            merchantProfileLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background_landscape));
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            merchantProfileLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }


}
