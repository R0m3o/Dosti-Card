package com.zeezo.dosticard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by Mashood Murtaza on 28-Mar-17.
 */
public class ReadTag extends Activity{

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.use_nfc_card);

        nfcAdapter = nfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Toast.makeText(getApplicationContext(), "new Intent", Toast.LENGTH_LONG).show();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (parcelables != null && parcelables.length > 0){
            readTextFromMessage((NdefMessage) parcelables[0]);
        }
        else {
            Toast.makeText(getApplicationContext(), "Card is empty!", Toast.LENGTH_LONG).show();
        }
    }

    private void readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecord = ndefMessage.getRecords();

        if (ndefRecord != null && ndefRecord.length > 0){
            NdefRecord record = ndefRecord[0];
            String tagContent = getTextFromNdefRecord(record);
            //Toast.makeText(getApplicationContext(), tagContent, Toast.LENGTH_LONG).show();
            Intent startCustomerProfile = new Intent(this, CustomerProfile.class);
            startCustomerProfile.putExtra("Contact_Number", tagContent);
            startActivity(startCustomerProfile);
        }else
            Toast.makeText(getApplicationContext(), "Record not fount", Toast.LENGTH_LONG).show();

    }

    private String getTextFromNdefRecord(NdefRecord ndefRecord) {

        String text = "";
        byte[] payload = ndefRecord.getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageSize = payload[0] & 0063;

        try {
            text = new String(payload, languageSize + 1, payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "Unsupported Encoding: "+ e.toString(), Toast.LENGTH_LONG).show();
        }

        return text;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device does not support NFC", Toast.LENGTH_LONG).show();
            finish();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Please enable NFC", Toast.LENGTH_LONG).show();
            finish();
        } else {
            enableForegroundDispatch();
        }
    }

    private void enableForegroundDispatch() {

        Intent startIntent = new Intent(ReadTag.this, ReadTag.class);
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
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
        finish();
    }
}
