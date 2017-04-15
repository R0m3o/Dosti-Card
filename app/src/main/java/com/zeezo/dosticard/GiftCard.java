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
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
 * Created by masho on 26-Jan-17.
 */
public class GiftCard extends Fragment implements View.OnClickListener {

    EditText amount;
    TextView yourBalance;
    Button load, collectGift;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gift_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initialize(view);
        yourBalance.setText("Your Balance: "+String.valueOf(getArguments().getInt("CustomerBalance")));
    }

    private void initialize(View view) {
        amount = (EditText) view.findViewById(R.id.idGiftCardAmount);
        yourBalance = (TextView) view.findViewById(R.id.idGiftCardBalance);
        load = (Button) view.findViewById(R.id.idGiftCardLoadBalance);
        collectGift = (Button) view.findViewById(R.id.idGiftCardCollectGift);

        load.setOnClickListener(GiftCard.this);
        collectGift.setOnClickListener(GiftCard.this);

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

    @Override
    public void onClick(View view) {
        if (isInternetConnected()) {
            int id = view.getId();
            switch (id) {
                case R.id.idGiftCardLoadBalance:
                    loadBalance();
                    break;
                case R.id.idGiftCardCollectGift:
                    collectGift();
                    break;
            }
        }else {
            final Snackbar internetAccess = Snackbar.make(view.findViewById(R.id.idGiftCardLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
            internetAccess.setAction("Retry", new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(getActivity().getIntent());
                    internetAccess.dismiss();
                }
            }).show();
        }
    }

    ValidateField validate = new ValidateField();
    private void loadBalance() {
        if (validate.isValidPayment(amount.getText().toString().trim())){
            String contactNumber = getArguments().getString("CustomerContact");
            addLoad(contactNumber);

        }else
            Toast.makeText(getContext(), "Please put valid amount to load!", Toast.LENGTH_LONG).show();
    }


    private void addLoad(String customerContactNumber) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "", false, false);

        final int amountToLoad = Integer.parseInt(amount.getText().toString().trim());
        final int balance = getArguments().getInt("CustomerBalance") + amountToLoad;
        String URl = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/addLoad/"+customerContactNumber+"/"+balance;
        //Toast.makeText(getContext(), URl, Toast.LENGTH_LONG).show();
        StringRequest request = new StringRequest(Request.Method.PUT, URl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String msg = "Thank you for using DOSTI CARD. You have successfully loaded Rs:"+String.valueOf(amountToLoad)+
                                ". Now you have Rs:"+String.valueOf(balance)+" and u can purchase any gift any time.";
                        sendSMS(msg);
                        amount.setText("");
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG).show();
                        startActivity(getActivity().getIntent());
                        dialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Error:" + error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idGiftCardLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
                params.put("Load", String.valueOf(balance));
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }

    private void collectGift() {
        if (validate.isValidPayment(amount.getText().toString().trim())){
            if (Integer.parseInt(amount.getText().toString().trim()) <= getArguments().getInt("CustomerBalance")) {
                String contactNumber = getArguments().getString("CustomerContact");
                updateBalance(contactNumber);
            }else
                Toast.makeText(getContext(), "You have insufficient balance!", Toast.LENGTH_LONG).show();

        }else
            Toast.makeText(getContext(), "Please put valid amount to load!", Toast.LENGTH_LONG).show();
    }

    private void updateBalance(String contactNumber) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "", false, false);

        final int priceOfGift = Integer.parseInt(amount.getText().toString().trim());
        final int balance = getArguments().getInt("CustomerBalance") - priceOfGift;
        String URl = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/addLoad/"+contactNumber+"/"+balance;
        //Toast.makeText(getContext(), URl, Toast.LENGTH_LONG).show();
        StringRequest request = new StringRequest(Request.Method.PUT, URl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String msg = "Thank you for using DOSTI CARD. You have successfully receive a gift for Rs:"+String.valueOf(priceOfGift)+
                                ". Now you have Rs:"+String.valueOf(balance);
                        sendSMS(msg);
                        dialog.dismiss();
                        amount.setText("");
                        //Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG).show();
                        startActivity(getActivity().getIntent());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), "Error:" + error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idGiftCardLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
                params.put("Load", String.valueOf(balance));
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }

    protected void sendSMS(String msg) {
        String phoneNo = getArguments().getString("CustomerContact");

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, msg, null, null);
    }
}
