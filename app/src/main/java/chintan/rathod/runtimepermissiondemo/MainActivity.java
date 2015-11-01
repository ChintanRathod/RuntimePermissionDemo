package chintan.rathod.runtimepermissiondemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chintan Rathod
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int READ_CONTACT_REQUEST_CODE = 100;
    private static final int READ_CALL_LOG_REQUEST_CODE = 101;
    private Button btnContacts;
    private TextView txtContactList;
    private Button btnReadPhoneCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnContacts = (Button) findViewById(R.id.btnContacts);
        btnReadPhoneCalls = (Button) findViewById(R.id.btnReadPhoneCalls);
        txtContactList = (TextView) findViewById(R.id.txtContactList);

        btnContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Check build version
                 */
                if(Build.VERSION.SDK_INT < 23){
                    displayContacts();
                }else {
                    requestContactPermission();
                }
            }
        });

        btnReadPhoneCalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Check build version
                 */
                if(Build.VERSION.SDK_INT < 23){
                    displayCallLogs();
                }else {
                    requestCameraPermission();
                }
            }
        });

    }

    private void requestContactPermission() {

        int hasContactPermission = ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_CONTACTS);

        if(hasContactPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACT_REQUEST_CODE);
        }else {
            Toast.makeText(MainActivity.this, "Contact Permission is already granted", Toast.LENGTH_LONG).show();
            displayContacts();
        }
    }

    private void requestCameraPermission() {
        int hasCameraPermission = ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_CALL_LOG);

        if(hasCameraPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALL_LOG}, READ_CALL_LOG_REQUEST_CODE);
        }else {
            Toast.makeText(MainActivity.this, "Read Phone State Permission is already granted", Toast.LENGTH_LONG).show();
            displayCallLogs();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case READ_CONTACT_REQUEST_CODE:

                // Check if the only required permission has been granted
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission has been granted, preview can be displayed
                    Log.i("Permission", "Contact permission has now been granted. Showing result.");
                    Toast.makeText(MainActivity.this,"Contact Permission is Granted",Toast.LENGTH_SHORT).show();

                    displayContacts();

                } else {
                    Log.i("Permission", "Contact permission was NOT granted.");
                    Toast.makeText(MainActivity.this,"Permission is not Granted",Toast.LENGTH_SHORT).show();
                }

                break;

            case READ_CALL_LOG_REQUEST_CODE:

                // Permission has not been granted and must be requested.
                // Check if the only required permission has been granted
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"Read call log Permission is Granted",Toast.LENGTH_SHORT).show();
                    displayCallLogs();
                }else {
                    Toast.makeText(MainActivity.this,"Read call log Permission is not Granted",Toast.LENGTH_SHORT).show();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_CALL_LOG)) {
                        // Provide an additional rationale to the user if the permission was not granted
                        // and the user would benefit from additional context for the use of the permission.
                        // Display a SnackBar with a button to request the missing permission.
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Required Attention")
                                .setMessage("Phone state permission is require to check your phone status")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALL_LOG},
                                                READ_CALL_LOG_REQUEST_CODE);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }

                break;
        }
    }

    private void displayContacts() {
        txtContactList.setText("");
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        txtContactList.append(name + " : "+phoneNo + "\n");
                    }
                    pCur.close();
                }
            }
        }
    }

    private void displayCallLogs(){

        String[] projection = new String[] {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
        };

        Cursor c = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, null,
                null, CallLog.Calls.DATE + " DESC");

        txtContactList.setText("");

        if (c.getCount() > 0)
        {
            while(c.moveToNext()){
                String callerID = c.getString(c.getColumnIndex(CallLog.Calls._ID));
                String callerNumber = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
                long callDateandTime = c.getLong(c.getColumnIndex(CallLog.Calls.DATE));
                long callDuration = c.getLong(c.getColumnIndex(CallLog.Calls.DURATION));

                txtContactList.append(callerNumber + " : "+callDuration + "\n");
            }
            c.close();
        }
    }

}
