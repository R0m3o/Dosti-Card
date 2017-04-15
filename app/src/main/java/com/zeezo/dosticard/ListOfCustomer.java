package com.zeezo.dosticard;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by masho on 26-Feb-17.
 */
public class ListOfCustomer extends Activity {

    private CustomerAdapter customerAdapter;
    ListView customerList;
    TextView noOfCustomer;
    TextView noCustomer;

    SessionManager session;
    HashMap<String, String> merchant;
    Dialog customerViewDialog;
    ArrayList<String> customerContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_list);

        customerList = (ListView) findViewById(R.id.idCustomerListView);
        noOfCustomer = (TextView) findViewById(R.id.idCustomerListNoOfCustomer);
        noCustomer = (TextView) findViewById(R.id.idCustomerListNoCustomer);
        session = new SessionManager(getApplicationContext());
        merchant = session.getMerchantInfo();
        customerContacts = new ArrayList<String>();

        customerAdapter = new CustomerAdapter(getApplicationContext(), R.layout.customer_listview);
        customerList.setAdapter(customerAdapter);

        listOfCustomer();

        customerViewDialog = new Dialog(this);
        customerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                customerViewDialog.setContentView(R.layout.customer_view);
                customerViewDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                getCustomerInfo(position);

                ImageView cancelDialog = (ImageView) customerViewDialog.findViewById(R.id.idCancelDialog);
                cancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customerViewDialog.dismiss();
                    }
                });

                final Button removeCustomer = (Button) customerViewDialog.findViewById(R.id.idCustomerRemoveBtn);
                removeCustomer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getApplicationContext(), contactNoOfDesiredCustomer, Toast.LENGTH_LONG).show();

                        removeCustomer(customerContacts.get(position));
                    }
                });

                if (merchant.get("Designation").equals("Admin"))
                    removeCustomer.setVisibility(View.VISIBLE);
                else
                    removeCustomer.setVisibility(View.GONE);

                customerViewDialog.show();
            }
        });

        //Toast.makeText(getApplicationContext(), String.valueOf(customerData.getPoints()), Toast.LENGTH_LONG).show();
    }

    private void listOfCustomer() {

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "");
        int store_id = Integer.parseInt(merchant.get("StoreID"));
        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/customer_store/" + store_id;

        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.dismiss();

                        if (response.length() == 0) {
                            customerList.setVisibility(View.GONE);
                            noCustomer.setVisibility(View.VISIBLE);
                        } else {
                            customerList.setVisibility(View.VISIBLE);
                            noCustomer.setVisibility(View.GONE);
                            int noOfCustomers = 0;
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);
                                    String name = obj.getString("Fname") + " " + obj.getString("Lname");
                                    CustomerData customerData = new CustomerData(name, obj.getInt("Points"), obj.getInt("Load"));

                                    customerAdapter.add(customerData);
                                    customerContacts.add(obj.getString("ContactNo"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                noOfCustomers++;
                                noOfCustomer.setText(String.valueOf(noOfCustomers));

                            }
                        }
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
        Volley.newRequestQueue(ListOfCustomer.this).add(request);
    }


    private void getCustomerInfo(int position) {
        final ProgressDialog progressDialog = ProgressDialog.show(getApplicationContext(), "", "Getting info...", false, false);

        final int store_id = Integer.valueOf(merchant.get("StoreID"));
        String contactOfDesiredCustomer = customerContacts.get(position);

        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/" + store_id + "/" + contactOfDesiredCustomer;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TextView customer_name = (TextView) customerViewDialog.findViewById(R.id.nameOfCustomer);
                        TextView customer_contact = (TextView) customerViewDialog.findViewById(R.id.contactOfCustomer);
                        TextView customer_address = (TextView) customerViewDialog.findViewById(R.id.addressOfCustomer);
                        TextView customer_points = (TextView) customerViewDialog.findViewById(R.id.pointsOfCustomer);
                        TextView customer_balance = (TextView) customerViewDialog.findViewById(R.id.balanceOfCustomer);
                        TextView customer_rewards = (TextView) customerViewDialog.findViewById(R.id.rewardsOfCustomer);

                        try {
                            customer_name.setText(response.getString("Fname") + " " + response.getString("Lname"));
                            customer_contact.setText(response.getString("ContactNo"));
                            customer_address.setText(response.getString("Address"));
                            customer_points.setText(String.valueOf(response.getInt("Points")));
                            customer_balance.setText("Rs: " + String.valueOf(response.getInt("Load")));
                            customer_rewards.setText(String.valueOf(response.getInt("Rewards")) + " rewards");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();

                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void removeCustomer(final String contact) {
        final ProgressDialog progressDialog = ProgressDialog.show(getApplicationContext(), "", "Deleting...", false, false);

        final int store_id = Integer.valueOf(merchant.get("StoreID"));

        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/deletingCustomer/" + store_id + "/" + contact;

        StringRequest request = new StringRequest(Request.Method.DELETE, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        customerViewDialog.dismiss();
                        progressDialog.dismiss();

                        startActivity(getIntent());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
