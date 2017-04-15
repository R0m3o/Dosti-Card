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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by masho on 06-Mar-17.
 */
public class NewStoreBranch extends Fragment {

    ListView storeListView;
    TextView noStore, noOfStore;
    ArrayAdapter adapter;
    ArrayList<String> storeList, storeAddresses;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_store_branch, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        storeListView = (ListView) view.findViewById(R.id.idStoresBranchListView);
        noStore = (TextView) view.findViewById(R.id.idStoresBranchNoStore);
        noOfStore = (TextView) view.findViewById(R.id.idStoresBranchNoOfStore);
        storeList = new ArrayList<String>();
        storeAddresses = new ArrayList<String>();
        //storeList.add("a");
        if (isInternetConnected()) {
            getListOfStores();
        }else {
            final Snackbar internetAccess = Snackbar.make(view.findViewById(R.id.idNewStoreBranchLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
            internetAccess.setAction("Retry", new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(getActivity().getIntent());
                    internetAccess.dismiss();
                }
            }).show();
        }

        storeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(), storeAddresses.get(position), Toast.LENGTH_LONG).show();
                if (isInternetConnected()) {
                    getStore(position);
                }else {
                    final Snackbar internetAccess = Snackbar.make(view.findViewById(R.id.idNewStoreBranchLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                    internetAccess.setAction("Retry", new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            startActivity(getActivity().getIntent());
                            internetAccess.dismiss();
                        }
                    }).show();
                }
            }
        });

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

    private void getListOfStores() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "", false, false);

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store";

        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() == 0) {
                            storeListView.setVisibility(View.GONE);
                            noStore.setVisibility(View.VISIBLE);
                        }else {
                            storeListView.setVisibility(View.VISIBLE);
                            noStore.setVisibility(View.GONE);

                            int noOfStores = 0;
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);
                                    String storeName = obj.getString("StoreName");
                                    String storeAddress = obj.getString("Address");

                                    String nameWithAdd = storeName + " (" + storeAddress + ")";

                                    storeList.add(nameWithAdd);
                                    storeAddresses.add(storeAddress);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                noOfStores++;
                                noOfStore.setText(String.valueOf(noOfStores));
                            }

                            adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, storeList);
                            storeListView.setAdapter(adapter);
                        }
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Server Error:"+ error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idNewStoreBranchLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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

    private void getStore(int pos) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "", false, false);

        String address = storeAddresses.get(pos);
        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Store/getStoreByAddress/"+address.replace(" ", "%20");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        int store_ID = 0, points_limit = 0, paymentToGetOnePoint = 0, discount = 0;
                        String store_name = "", store_city = "", store_address = "";
                        try {
                            store_ID = response.getInt("ID");
                            store_name = response.getString("StoreName");
                            store_city = response.getString("City");
                            store_address = response.getString("Address");
                            points_limit = response.getInt("PointsLimit");
                            paymentToGetOnePoint = response.getInt("PaymentToGetOnePoint");
                            discount = response.getInt("PercentageDiscount");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Bundle bundle = new Bundle();
                        bundle.putInt("storeID", store_ID);
                        bundle.putString("storeName", store_name);
                        bundle.putString("storeCity", store_city);
                        bundle.putString("storeAddress", store_address);
                        bundle.putInt("storePointsLimit", points_limit);
                        bundle.putInt("storePaymentToGetOnePoint", paymentToGetOnePoint);
                        bundle.putInt("storeDiscount", discount);

                        //Toast.makeText(getContext(), bundle.getString("storeName"), Toast.LENGTH_LONG).show();

                        Intent startCreateNewStoreBranch = new Intent(getContext(), CreateNewStoreBranch.class);
                        startCreateNewStoreBranch.putExtras(bundle);
                        NewStoreBranch.this.startActivity(startCreateNewStoreBranch);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Server Error: "+ error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idNewStoreBranchLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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

}
