package net.xerael.beam;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.Charset;

public class BeamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beam);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_beam, container, false);
        }
    }


    //Button method
    public void setText(View v) {
        EditText ed = (EditText) findViewById(R.id.edtext);
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(ed.getText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //set Ndef message to send by beam
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        assert nfcAdapter != null;
        nfcAdapter.setNdefPushMessageCallback(
                new NfcAdapter.CreateNdefMessageCallback() {
                    public NdefMessage createNdefMessage(NfcEvent event) {
                        return createMessage();
                    }
                }, this);

        //See if app got called by AndroidBeam intent.
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            extractPayload(getIntent());
        }
    }

    /**
     * Creates a new NdefMessage with payload of text field.
     * @return NFC Data Exchange Format
     */
    private NdefMessage createMessage() {
        String mimeType = "application/net.xerael.beam";
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));

        //GENERATE PAYLOAD
        TextView text = (TextView) findViewById(R.id.text);
        byte[] payLoad = text.getText().toString().getBytes();

        //GENERATE NFC MESSAGE
        return new NdefMessage(
                new NdefRecord[]{
                new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                        mimeBytes,
                        null,
                        payLoad),
                NdefRecord.createApplicationRecord("net.xerael.beam")
        });
    }

    private void extractPayload(Intent beamIntent) {
        Parcelable[] messages = beamIntent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage message = (NdefMessage) messages[0];
        NdefRecord record = message.getRecords()[0];
        String payload = new String(record.getPayload());
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(payload);
    }

}
