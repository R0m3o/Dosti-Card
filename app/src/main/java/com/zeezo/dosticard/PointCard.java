package com.zeezo.dosticard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by masho on 26-Jan-17.
 */
public class PointCard extends Fragment implements View.OnClickListener {

    TextView txtGainPoints, txtReservedAmount, txtPointProgress, pointsToGetReward;
    EditText payment;
    ProgressBar pointProgress;
    Button btnRedeemReward, btnCollectPoints, rewardsYouHave;

    int noOfRewards;

    SessionManager session;
    HashMap<String, String> store, merchant;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.point_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(getContext());
        merchant = session.getMerchantInfo();
        store = session.getStoreInfo();

        initialize(view);

        //String name = getArguments().getString("CustomerName");
        //Toast.makeText(getContext(), name, Toast.LENGTH_LONG).show();
        setUpInfo();
    }

    private void initialize(View v) {

        txtGainPoints = (TextView) v.findViewById(R.id.idPointCardEarnedPoint);
        txtReservedAmount = (TextView) v.findViewById(R.id.idPointCardReservedAmount);
        txtPointProgress = (TextView) v.findViewById(R.id.idPointCardGainedPoints);
        pointsToGetReward = (TextView) v.findViewById(R.id.idPointCardLeftPoints);
        payment = (EditText) v.findViewById(R.id.idPointCardPayment);
        pointProgress = (ProgressBar) v.findViewById(R.id.idPointCardProgressBar);

        rewardsYouHave = (Button) v.findViewById(R.id.idNoOfRewards);
        btnRedeemReward = (Button) v.findViewById(R.id.idPointCardRedeem);
        btnCollectPoints = (Button) v.findViewById(R.id.idPointCardCollectPoint);

        btnRedeemReward.setOnClickListener(PointCard.this);
        btnCollectPoints.setOnClickListener(PointCard.this);
    }

    private void setUpInfo() {

        txtGainPoints.setText("Points You Gain: "+String.valueOf(getArguments().getInt("CustomerPoints")));
        txtPointProgress.setText(String.valueOf(getArguments().getInt("CustomerPoints"))+"/"+store.get("Store_pointsLimit"));
        pointProgress.setMax(Integer.parseInt(store.get("Store_pointsLimit")));
        pointProgress.setProgress(getArguments().getInt("CustomerPoints"));
        txtReservedAmount.setText("Reserved Amount Rs: "+String.valueOf(getArguments().getInt("CustomerReservedAmount")));
        int leftPoints = Integer.valueOf(store.get("Store_pointsLimit")) - getArguments().getInt("CustomerPoints");
        pointsToGetReward.setText(String.valueOf(leftPoints));
        rewardsYouHave.setText(String.valueOf(getArguments().getInt("CustomerReward")));

        noOfRewards = Integer.valueOf(rewardsYouHave.getText().toString());
        //Toast.makeText(getContext(), String.valueOf(customerINFO.getID()), Toast.LENGTH_LONG).show();

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
                case R.id.idPointCardRedeem:
                    redeemReward();
                    break;
                case R.id.idPointCardCollectPoint:
                    collectPoints();
                    break;
            }
        }else {
            final Snackbar internetAccess = Snackbar.make(view.findViewById(R.id.idPointCardLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
    private void redeemReward() {
        if (noOfRewards > 0){
            if (validate.isValidPayment(payment.getText().toString().trim())){
                noOfRewards--;
                float percentageDiscount = Float.valueOf(store.get("Store_Discount")) / 100;
                int discount = Math.round(Float.valueOf(payment.getText().toString().trim()) * percentageDiscount);
                int paymentAfterDiscount = Integer.parseInt(payment.getText().toString().trim()) - discount;

                updatePointCard(getArguments().getInt("CustomerPoints"), getArguments().getInt("CustomerReservedAmount") ,noOfRewards);
                String msg = "Congrats! You have got "+store.get("Store_Discount")+"% off as a reward. Just pay Rs:"+paymentAfterDiscount+
                        " and collect your reward. Now you have "+String.valueOf(noOfRewards)+" reward and " +
                        String.valueOf(getArguments().getInt("CustomerPoints"))+"/"+store.get("Store_pointsLimit")+" points.";
                sendSMS(getArguments().getString("CustomerContact"), msg);
                payment.setText("");
            }else
                Toast.makeText(getContext(), "Please put valid payment!", Toast.LENGTH_LONG).show();

        }else
            Toast.makeText(getContext(),"You have no rewards yet!", Toast.LENGTH_LONG).show();
    }

    protected void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private void collectPoints() {
        if (!validate.isValidPayment(payment.getText().toString().trim()))
            Toast.makeText(getContext(), "Please put valid payment!", Toast.LENGTH_LONG).show();
        else {
            int getPayment = Integer.valueOf(payment.getText().toString().trim()) + getArguments().getInt("CustomerReservedAmount");
            int pointsOnPayment = 0;
            int paymentToGetOnePoint = Integer.parseInt(store.get("Store_paymentToGetOnePoint"));
            if (getPayment > paymentToGetOnePoint) {
                pointsOnPayment = getPayment / paymentToGetOnePoint;
                getPayment = getPayment % paymentToGetOnePoint;
            }

            int totalPoints = getArguments().getInt("CustomerPoints") + pointsOnPayment;
            int pointsLimit = Integer.valueOf(store.get("Store_pointsLimit"));
            while (totalPoints >= pointsLimit){
                noOfRewards++;
                totalPoints = totalPoints % pointsLimit;
            }

            updatePointCard(totalPoints, getPayment, noOfRewards);

        }
    }

    private void updatePointCard(final int points, final int reservedAmount, final int rewards) {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", "", false, false);
        int customerID = getArguments().getInt("CustomerID");
        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com//api/Customer/"+customerID+"/"+points+"/"+reservedAmount+"/"+rewards;
        //Toast.makeText(getContext(), url, Toast.LENGTH_LONG).show();
        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //getCustomerInfo();
                        sendSMS(points, rewards);
                        payment.setText("");
                        startActivity(getActivity().getIntent());
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(getView().findViewById(R.id.idMerchantRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
                params.put("Points", String.valueOf(points));
                params.put("ReservedAmount", String.valueOf(reservedAmount));
                params.put("Rewards", String.valueOf(rewards));
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }

    protected void sendSMS(int points, int rewards) {
        String phoneNo = getArguments().getString("CustomerContact");
        String msg = "Thank you for using DOSTI CARD. You have purchased items only for Rs:"+payment.getText().toString().trim()+
                ". Now you have "+String.valueOf(points)+" points and "+String.valueOf(rewards)+" rewards.";

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, msg, null, null);
    }
}
