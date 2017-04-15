package com.zeezo.dosticard;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Created by masho on 31-Jan-17.
 */
public class CustomerRegistration extends Activity implements View.OnClickListener {

    EditText firstName, lastName, contactNumber, address;
    Button createCustomer;
    LinearLayout customerRegistrationLayout;

    int verifyCode;

    ValidateField validateField;
    SessionManager session;
    HashMap<String, String> merchant, store;

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_registration);

        session = new SessionManager(getApplicationContext());
        merchant = session.getMerchantInfo();
        store = session.getStoreInfo();

        validateField = new ValidateField();
        initialize();
    }

    private void initialize() {
        customerRegistrationLayout = (LinearLayout) findViewById(R.id.idCustomerRegistrationLayout);
        firstName = (EditText) findViewById(R.id.idCreateCustomerFirstName);
        lastName = (EditText) findViewById(R.id.idCreateCustomerLastName);
        contactNumber = (EditText) findViewById(R.id.idCreateCustomerContactNum);
        address = (EditText) findViewById(R.id.idCreateCustomerAddress);
        createCustomer = (Button) findViewById(R.id.idBtnCreateCustomer);

        createCustomer.setOnClickListener(this);

        nfcAdapter = nfcAdapter.getDefaultAdapter(this);
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
        int id = view.getId();
        switch (id) {
            case R.id.idBtnCreateCustomer:

                if (!validateField.isValidName(firstName.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid First Name!", Toast.LENGTH_LONG).show();
                else if (!validateField.isValidName(lastName.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid Last Name!", Toast.LENGTH_LONG).show();
                else if (!validateField.isValidContactNum(contactNumber.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid contact number!", Toast.LENGTH_LONG).show();
                else if (!validateField.isValidAddress(address.getText().toString().trim()))
                    Toast.makeText(getApplicationContext(), "Invalid address!", Toast.LENGTH_LONG).show();
                else {
                    if (isInternetConnected()) {
                        isCustomerAlreadyExist();
                    }else {
                        final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idCustomerRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
                        internetAccess.setAction("Retry", new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                startActivity(getIntent());
                                internetAccess.dismiss();
                            }
                        }).show();
                    }
                }
                break;
        }

    }

    private void isCustomerAlreadyExist() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Checking user exist or not!", false, false);

        String customerContact = contactNumber.getText().toString().trim();
        String URL = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer/isCustomerExist/" + customerContact;

        StringRequest request = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        if (response.equals("true")) {
                            Toast.makeText(getApplicationContext(), "Contact number already exist! please put another contact number.", Toast.LENGTH_LONG).show();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                                    String[] permissions = {android.Manifest.permission.SEND_SMS};
                                    requestPermissions(permissions, 0);
                                } else {
                                    sendSMS();
                                    openDialogBox();
                                }
                            } else {
                                sendSMS();
                                openDialogBox();
                            }
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
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idCustomerRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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

    protected void sendSMS() {
        String phoneNo = contactNumber.getText().toString();
        Random random = new Random();
        verifyCode = random.nextInt(5000) + 1000;
        String msg = "Welcome to Dosti Card! Your Verification Code is: " + verifyCode;

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, msg, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();
            sendSMS();
            openDialogBox();
        } else {
            Toast.makeText(getApplicationContext(), "Sending SMS permission denied!", Toast.LENGTH_LONG).show();
            finishActivity();
        }

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
                else if (!enterCode.getText().toString().equals(String.valueOf(verifyCode)))
                    Toast.makeText(getApplication(), "Error: Wrong code entered!", Toast.LENGTH_LONG).show();
                else if (enterCode.getText().toString().equals(String.valueOf(verifyCode))) {

                    if (nfcAdapter == null) {
                        Toast.makeText(getApplicationContext(), "Device does not support NFC", Toast.LENGTH_LONG).show();
                    } else if (!nfcAdapter.isEnabled()) {
                        Toast.makeText(getApplicationContext(), "Please enable NFC", Toast.LENGTH_LONG).show();
                    } else {
                        enableForegroundDispatch();
                        Toast.makeText(getApplicationContext(), "Please tap the card to write!", Toast.LENGTH_LONG).show();
                    }

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

    private NdefMessage createMessage(String message) {
        NdefRecord record = createRecord(message);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{record});

        return ndefMessage;
    }

    private NdefRecord createRecord(String message) {
        try {
            byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            byte[] text = message.getBytes("UTF-8");

            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) langSize);
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());

            return record;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean writeTag(Tag tag, NdefMessage message) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(getApplicationContext(), "Error: tag not writable", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(getApplicationContext(), "Error: tag too small", Toast.LENGTH_SHORT).show();
                    return false;
                }
                ndef.writeNdefMessage(message);
                ndef.close();
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        format.close();
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void registerCustomer() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Adding Customer...", false, false);

        final String first_name = firstName.getText().toString().trim();
        final String last_name = lastName.getText().toString().trim();
        final String contact_num = contactNumber.getText().toString().trim();
        final String c_address = address.getText().toString().trim();

        final int store_id = Integer.valueOf(merchant.get("StoreID"));

        String url = "http://dosticardapi.us-west-2.elasticbeanstalk.com/api/Customer";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Successfully Created!", Toast.LENGTH_LONG).show();

                        welcomeMessage(contact_num);
                        Intent startCustomerProfile = new Intent(CustomerRegistration.this, CustomerProfile.class);
                        startCustomerProfile.putExtra("Contact_Number", contact_num);
                        startActivity(startCustomerProfile);

                        finishActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (isInternetConnected()) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            final Snackbar internetAccess = Snackbar.make(findViewById(R.id.idCustomerRegistrationLayout), "No Internet Access!", Snackbar.LENGTH_INDEFINITE);
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
                params.put("ContactNo", contact_num);
                params.put("Address", c_address);
                params.put("StoreID", String.valueOf(store_id));
                params.put("Points", String.valueOf(0));
                params.put("Load", String.valueOf(0));
                params.put("ReservedAmount", String.valueOf(0));
                params.put("Rewards", String.valueOf(0));

                return params;
            }
        };
        Volley.newRequestQueue(CustomerRegistration.this).add(stringRequest);
    }

    private void welcomeMessage(String phoneNumber) {
        String message = "Welcome to DOSTI CARD. You can use your point card as well as gift card anytime. You can earn points every payment just rupees Rs:"+
                store.get("Store_paymentToGetOnePoint")+".";

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private void finishActivity() {
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            customerRegistrationLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background_landscape));
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            customerRegistrationLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage message = createMessage(contactNumber.getText().toString().trim());

            if (writeTag(tag, message)) {
                Toast.makeText(getApplicationContext(), "Successfully write the tag!", Toast.LENGTH_LONG).show();
                //disableForegroundDispatch();
                dialog.dismiss();
                registerCustomer();
            } else
                Toast.makeText(getApplicationContext(), "Error: Tag can't write. Something Wrong!", Toast.LENGTH_LONG).show();
        }
    }

    private void enableForegroundDispatch() {

        Intent startIntent = new Intent(CustomerRegistration.this, CustomerRegistration.class);
        startIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, startIntent, 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] intentFilter = new IntentFilter[]{tagDetected};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
    }

    private void disableForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disableForegroundDispatch();
        finishActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
    }
}
