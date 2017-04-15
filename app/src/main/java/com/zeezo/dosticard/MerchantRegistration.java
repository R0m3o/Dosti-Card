package com.zeezo.dosticard;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by masho on 19-Jan-17.
 */
public class MerchantRegistration extends Activity implements View.OnClickListener {

    TextView signInTxtBack;
    ImageView signInImgVBack;

    EditText fName, lName, password, confirmPass, contactNum, address;

    Button createMerchant;

    LinearLayout merchantRegistrationLayout;

    ValidateField validate;
    int verifyCode;
    String phoneNo, msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merchant_registration);

        validate = new ValidateField();
        initialization();
        signInTxtBack.setOnClickListener(this);
        signInImgVBack.setOnClickListener(this);
        createMerchant.setOnClickListener(this);
    }

    private void initialization() {
        merchantRegistrationLayout = (LinearLayout) findViewById(R.id.idMerchantRegistrationLayout);

        signInTxtBack = (TextView) findViewById(R.id.idStartSignInActivityText);
        signInImgVBack = (ImageView) findViewById(R.id.idStartSignInActivityArrow);
        fName = (EditText) findViewById(R.id.idCreateMerchantFirstName);
        lName = (EditText) findViewById(R.id.idCreateMerchantLastName);
        password = (EditText) findViewById(R.id.idCreateMerchantPassword);
        confirmPass = (EditText) findViewById(R.id.idCreateMerchantConfirmPass);
        contactNum = (EditText) findViewById(R.id.idCreateMerchantPhoneNum);
        address = (EditText) findViewById(R.id.idCreateMerchantAddress);
        createMerchant = (Button) findViewById(R.id.idBtnCreateMerchant);
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
                case R.id.idStartSignInActivityText:
                    startLoginActivity();
                    break;
                case R.id.idStartSignInActivityArrow:
                    startLoginActivity();
                    break;
                case R.id.idBtnCreateMerchant:
                    if (!validate.isValidName(fName.getText().toString().trim()))
                        Toast.makeText(getApplicationContext(), "Invalid First Name!", Toast.LENGTH_LONG).show();
                    else if (!validate.isValidName(lName.getText().toString().trim()))
                        Toast.makeText(getApplicationContext(), "Invalid Last Name!", Toast.LENGTH_LONG).show();
                    else if (!validate.isValidPassword(password.getText().toString().trim()))
                        Toast.makeText(getApplicationContext(), "Invalid password!", Toast.LENGTH_LONG).show();
                    else if (!confirmPass.getText().toString().trim().equals(password.getText().toString().trim()))
                        Toast.makeText(getApplicationContext(), "Invalid confirm password!", Toast.LENGTH_LONG).show();
                    else if (!validate.isValidContactNum(contactNum.getText().toString().trim()))
                        Toast.makeText(getApplicationContext(), "Invalid contact number!", Toast.LENGTH_LONG).show();
                    else if (!validate.isValidAddress(address.getText().toString().trim()))
                        Toast.makeText(getApplicationContext(), "Invalid address!", Toast.LENGTH_LONG).show();
                    else {
                        isMerchantAlreadyExist();
                        //registerMerchant();
                    }
                    break;
            }
        }else {
            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
            internetAccess.setAction("Retry", new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startActivity(getIntent());
                    internetAccess.dismiss();
                }
            }).show();
        }
    }

    private void isMerchantAlreadyExist() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Checking user exist or not!", false, false);

        String merchantContact = contactNum.getText().toString().trim();
        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant/isMerchantExist/" + merchantContact;


        StringRequest request = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        if (response.equals("true")) {
                            Toast.makeText(getApplicationContext(), "Contact number already exist! please put another contact number.", Toast.LENGTH_LONG).show();
                        }
                        else {

                            phoneNo = contactNum.getText().toString();
                            Random random = new Random();
                            verifyCode = random.nextInt(5000) + 1000;
                            msg = "Your validation code is: " + verifyCode;
                            sendSMS(phoneNo, msg);
                            openDialogBox();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
        );
        Volley.newRequestQueue(this).add(request);
    }

    public void startLoginActivity() {
        Intent startSignInActivity = new Intent(MerchantRegistration.this, MerchantLogin.class);
        MerchantRegistration.this.startActivity(startSignInActivity);
    }

    public void openDialogBox() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.sms_verify_dialog_box);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setTitle("SMS Verification Code");
        final EditText enterCode = (EditText) dialog.findViewById(R.id.idSMSCode);
        Button OKCode = (Button) dialog.findViewById(R.id.idBtnVerifyCode);
        Button cancelDialogBox = (Button) dialog.findViewById(R.id.idCancelDialogBox);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        OKCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enterCode.getText().toString().isEmpty())
                    Toast.makeText(getApplication(), "Error: Please enter code first!", Toast.LENGTH_LONG).show();
                else if (!enterCode.getText().toString().equals(String.valueOf(verifyCode)))
                    Toast.makeText(getApplication(), "Error: Wrong code entered!", Toast.LENGTH_LONG).show();
                else if (enterCode.getText().toString().equals(String.valueOf(verifyCode))) {
                    dialog.dismiss();
                    registerMerchant();
                }
            }
        });

        cancelDialogBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected void sendSMS(String phoneNumber, String message) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {android.Manifest.permission.SEND_SMS};
                requestPermissions(permissions, 0);
            }
        }
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

    }

    private void registerMerchant() {

        final ProgressDialog loader = ProgressDialog.show(this, "", "Creating...", false, false);

        final String first_name = fName.getText().toString().trim();
        final String last_name = lName.getText().toString().trim();
        final String pass = password.getText().toString().trim();
        final String contact_num = contactNum.getText().toString().trim();
        final String m_address = address.getText().toString().trim();

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loader.dismiss();
                        Toast.makeText(getApplicationContext(), "Successfully Created", Toast.LENGTH_LONG).show();
                        Intent startMerchantLogin = new Intent(MerchantRegistration.this, MerchantLogin.class);
                        MerchantRegistration.this.startActivity(startMerchantLogin);
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loader.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idMerchantRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Fname", first_name);
                params.put("Lname", last_name);
                params.put("Password", pass);
                params.put("ContactNo", contact_num);
                params.put("Address", m_address);
                return params;
            }

        };

        Volley.newRequestQueue(MerchantRegistration.this).add(stringRequest);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            merchantRegistrationLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background_landscape));
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            merchantRegistrationLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }

}
