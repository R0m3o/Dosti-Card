package com.zeezo.dosticard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.view.View;
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
import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by masho on 26-Jan-17.
 */
public class CustomerProfile extends FragmentActivity {

    TabLayout menu;
    ViewPager viewPager;

    LinearLayout customerProfileLayout;
    TextView customerName, gainedPoints, haveBalance;

    SessionManager session;
    HashMap<String, String> merchant, store;
    String customerContact;

    Bundle customerInfoBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_profile);

        session = new SessionManager(getApplicationContext());
        merchant = session.getMerchantInfo();
        store = session.getStoreInfo();

        customerProfileLayout = (LinearLayout) findViewById(R.id.idCustomerProfileLayout);

        menu = (TabLayout) findViewById(R.id.idCustomerMenu);
        viewPager = (ViewPager) findViewById(R.id.idCustomerViewPager);
        customerName = (TextView) findViewById(R.id.idCustomerProfileName);
        gainedPoints = (TextView) findViewById(R.id.idCustomerProfileGainedPoints);
        haveBalance = (TextView) findViewById(R.id.idCustomerProfileBalance);

        customerInfoBundle = new Bundle();

        customerContact = getIntent().getExtras().getString("Contact_Number");

        if (isInternetConnected()) {
            getCustomerInfo(customerContact);
        }else {
            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idCustomerProfileLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTING)
            return true;
        else
            return false;
    }

    private void getCustomerInfo(String contactNo) {
        final ProgressDialog progressDialog = ProgressDialog.show(getApplicationContext(), "","please wait..." ,false, false);

        final int store_id = Integer.valueOf(merchant.get("StoreID"));

        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/"+store_id+"/"+contactNo;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int ID = 0;
                        String name = "";
                        String contact_no = "";
                        String address = "";
                        int storeID = 0;
                        int points = 0;
                        int balance = 0;
                        int reservedAmount = 0;
                        int rewards = 0;

                        try {
                            ID = response.getInt("ID");
                            name = response.getString("Fname") +" "+response.getString("Lname");
                            contact_no = response.getString("ContactNo");
                            address = response.getString("Address");
                            storeID = response.getInt("StoreID");
                            points = response.getInt("Points");
                            balance = response.getInt("Load");
                            reservedAmount = response.getInt("ReservedAmount");
                            rewards = response.getInt("Rewards");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        customerInfoBundle.putInt("CustomerID", ID);
                        customerInfoBundle.putString("CustomerName", name);
                        customerInfoBundle.putString("CustomerContact", contact_no);
                        customerInfoBundle.putString("CustomerAddress", address);
                        customerInfoBundle.putInt("CustomerStoreID", storeID);
                        customerInfoBundle.putInt("CustomerPoints", points);
                        customerInfoBundle.putInt("CustomerBalance", balance);
                        customerInfoBundle.putInt("CustomerReservedAmount", reservedAmount);
                        customerInfoBundle.putInt("CustomerReward", rewards);

                        setUpProfile();
                        setUpAdapter();

                        //Toast.makeText(getApplicationContext(), getName(), Toast.LENGTH_LONG).show();

                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), "You are not a member of that store.", Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idCustomerProfileLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                            internetAccess.setAction("Retry", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    startActivity(getIntent());
                                    internetAccess.dismiss();
                                }
                            }).show();
                        }
                        finish();
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void setUpProfile() {
        customerName.setText(customerInfoBundle.getString("CustomerName"));
        gainedPoints.setText("Points: "+String.valueOf(customerInfoBundle.getInt("CustomerPoints")));
        haveBalance.setText("Balance: "+String.valueOf(customerInfoBundle.getInt("CustomerBalance")));
    }

    private void setUpAdapter() {
        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager() , getApplicationContext()));
        menu.setupWithViewPager(viewPager);

        menu.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private String fragments[] = {"Point Card", "Gift Card"};

        public CustomAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0:
                    PointCard pointCard = new PointCard();
                    pointCard.setArguments(customerInfoBundle);
                    return pointCard;
                case 1:
                    GiftCard giftCard = new GiftCard();
                    giftCard.setArguments(customerInfoBundle);
                    return giftCard;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            customerProfileLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable .dark_background_landscape));
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            customerProfileLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }

}
