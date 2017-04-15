package com.zeezo.dosticard;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * Created by Mashood Murtaza on 01-Apr-17.
 */
public class UpdateMerchant extends Activity implements View.OnClickListener{

    EditText fName, lName, pass, confirmPass, contact, m_address;
    Button edit, update;

    SessionManager session;
    HashMap<String, String> merchant;

    ValidateField validate;
    int verifyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_merchant);

        session = new SessionManager(getApplicationContext());
        merchant = session.getMerchantInfo();
        validate = new ValidateField();

        initialize();
        setUpView();

        edit.setOnClickListener(this);
        update.setOnClickListener(this);
    }

    private void initialize() {
        fName = (EditText) findViewById(R.id.idEditMerchantFirstName);
        lName = (EditText) findViewById(R.id.idEditMerchantLastName);
        pass = (EditText) findViewById(R.id.idEditMerchantPassword);
        confirmPass = (EditText) findViewById(R.id.idEditMerchantConfirmPass);
        contact = (EditText) findViewById(R.id.idEditMerchantPhoneNum);
        m_address = (EditText) findViewById(R.id.idEditMerchantAddress);

        edit = (Button) findViewById(R.id.idUpdateMerchantEdit);
        update = (Button) findViewById(R.id.idUpdateMerchant);
    }

    private void setUpView() {
        fName.setText(merchant.get("First_name"));
        lName.setText(merchant.get("Last_name"));
        pass.setText(merchant.get("Password"));
        confirmPass.setText(merchant.get("Password"));
        contact.setText(merchant.get("Contact_No"));
        m_address.setText(merchant.get("Address"));

        fName.setEnabled(false);
        lName.setEnabled(false);
        pass.setEnabled(false);
        confirmPass.setEnabled(false);
        contact.setEnabled(false);
        m_address.setEnabled(false);

        edit.setEnabled(true);
        edit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.idUpdateMerchantEdit:
                fName.setEnabled(true);
                lName.setEnabled(true);
                pass.setEnabled(true);
                confirmPass.setEnabled(true);
                contact.setEnabled(true);
                m_address.setEnabled(true);

                edit.setEnabled(false);
                edit.setVisibility(View.GONE);
                break;

            case R.id.idUpdateMerchant:
                if (!validate.isValidName(fName.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid First Name!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidName(lName.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid Last Name!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidPassword(pass.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid password!", Toast.LENGTH_LONG).show();
                else if (!confirmPass.getText().toString().trim().equals(pass.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid confirm password!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidContactNum(contact.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid contact number!", Toast.LENGTH_LONG).show();
                else if (!validate.isValidAddress(m_address.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid address!", Toast.LENGTH_LONG).show();
                else {
                    if (contact.getText().toString().trim().equals(merchant.get("Contact_No"))) {
                        updateMerchant(merchant.get("Merchant_Id"));
                    }else {
                        sendSMS();
                        openDialogBox();
                    }
                }
                break;
        }
    }

    protected void sendSMS() {
        String phoneNo = contact.getText().toString();
        Random random = new Random();
        verifyCode = random.nextInt(5000) + 1000;
        String msg = "Welcome to Dosti Card! Your Verification Code is: " + verifyCode;

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, msg, null, null);
    }

    Dialog dialog;
    public void openDialogBox() {
        dialog = new Dialog(this);
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
                else if (!enterCode.getText().toString().trim().equals(String.valueOf(verifyCode)))
                    Toast.makeText(getApplication(), "Error: Wrong code entered!", Toast.LENGTH_LONG).show();
                else if (enterCode.getText().toString().trim().equals(String.valueOf(verifyCode))) {
                    updateMerchant(merchant.get("Merchant_Id"));
                    dialog.dismiss();
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

    private void updateMerchant(String merchant_id) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Updating...", false, false);

        final String first_name = fName.getText().toString().trim();
        final String last_name = lName.getText().toString().trim();
        final String password = pass.getText().toString().trim();
        final String contact_num = contact.getText().toString().trim();
        final String address = m_address.getText().toString().trim();

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Merchant/updateMerchant/"+merchant_id;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        session.createLoginSession(Integer.parseInt(merchant.get("Merchant_Id")), first_name, last_name, password, contact_num, address,
                                Integer.parseInt(merchant.get("StoreID")), merchant.get("Designation"));
                        progressDialog.dismiss();
                        Intent startMerchantProfile = new Intent(UpdateMerchant.this, MerchantProfile.class);
                        UpdateMerchant.this.startActivity(startMerchantProfile);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error: "+error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Fname", first_name);
                params.put("Lname", last_name);
                params.put("Password", password);
                params.put("ContactNo", contact_num);
                params.put("Address", address);
                return params;
            }
        };
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
