package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sisca_app.fragments.DoctorHomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DoctorMainActivity extends AppCompatActivity {

    private String imuStatus, rrStatus;
    private ImageView startDefib, cancelDefib;
    private Integer bpmInt, patientAge, hrMax;
    private Dialog dialog;
    private String relayState = "OFF";
    private Vibrator vibrator;
    private double hardThres;
    private DatabaseReference sensorRef, patientParamsRef;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private String userId;

    private static final String TAG = "bluetoothTAG";
    private final String btMac = "9C:9C:1F:E3:7E:CA";
    private int mMaxChars = 50000;
    private BluetoothSocket mBTSocket;
    private UUID mDeviceUUID;
    private ReadInput mReadThread = null;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;
    private BluetoothDevice mDevice;
    private ProgressDialog progressDialog;
    private boolean receiveText = true;
    Bundle btData;
    DoctorHomeFragment doctorHomeFragment;
    FragmentTransaction fragmentTransaction;
    String strInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mDevice = bundle.getParcelable(BluetoothActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(bundle.getString(BluetoothActivity.DEVICE_UUID));
        mMaxChars = bundle.getInt(BluetoothActivity.BUFFER_SIZE);
        Log.d(TAG,"Ready");

//        doctorHomeFragment = new DoctorHomeFragment();
//        fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        btData = new Bundle();


        dialog = new Dialog(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final long[] pattern = {500, 1000}; // 0.5s sleep, 1s vibrate
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();


        DatabaseReference relayRef = FirebaseDatabase.getInstance().getReference().child("Relay");
        relayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relayState = snapshot.child("relayState").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        patientParamsRef = FirebaseDatabase.getInstance().getReference().child("PatientParams");
        patientParamsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientAge = snapshot.child("age").getValue(Integer.class);
                hrMax = 220 - patientAge;
                hardThres = hrMax * 0.93;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sensorRef = FirebaseDatabase.getInstance().getReference().child("Sensor");
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imuStatus = snapshot.child("imuSensorStatus").getValue().toString();
                rrStatus = snapshot.child("rrDetection").getValue().toString();
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);


                // Checking the IMU sensor status
                if (imuStatus.trim().equals("FALL")) {
                    Toast.makeText(DoctorMainActivity.this, "Patient Falling", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern, 0);
                    openAlertDialog();
                }

                // Check the RR arythmia detection
                if (rrStatus.trim().equals("ABNORMAL")) {
                    Toast.makeText(DoctorMainActivity.this, "Arythmia Detected", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern, 0);
                    openAlertDialog();
                }

                // Checking the BPM value
                if (bpmInt < 60) {
                    // Alert heart beat too slow
                    Toast.makeText(DoctorMainActivity.this, "Patient have bradycardia threat", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern, 0);
                    bradycardiaAlert();
                } else if (bpmInt > 100 && bpmInt < hrMax) {
                    // Alert heart beat too fast
                    Toast.makeText(DoctorMainActivity.this, "Patient have tachycardia threat", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern, 0);
                    tachycardiaAlert();
                } else if (bpmInt > hrMax) {
                    // Alert heart beat max

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.doctor_navigation);
        NavController navController = Navigation.findNavController(this, R.id.doctor_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void openAlertDialog() {
        dialog.setContentView(R.layout.alert_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        startDefib = dialog.findViewById(R.id.defib_start_button);
        cancelDefib = dialog.findViewById(R.id.defib_cancel_button);

        startDefib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorMainActivity.this, "Defibrilator triggered", Toast.LENGTH_SHORT).show();
                if (relayState.trim().equals("OFF")) {
                    relayState = "ON";
                    sendRelayCommand(relayState);
                    dialog.dismiss();
                    vibrator.cancel();
                }
            }
        });

        cancelDefib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relayState = "OFF";
                sendRelayCommand(relayState);
                dialog.dismiss();
                Toast.makeText(DoctorMainActivity.this, "Patient is safe", Toast.LENGTH_SHORT).show();
                vibrator.cancel();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                vibrator.cancel();
            }
        });

        dialog.show();
    }


    private void sendRelayCommand(String relayState) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }

    private void tachycardiaAlert() {
        dialog.setContentView(R.layout.doctor_tachycardia_alert);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                vibrator.cancel();
            }
        });

        dialog.show();
    }

    private void bradycardiaAlert() {
        dialog.setContentView(R.layout.doctor_bradycardia_alert);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                vibrator.cancel();
            }
        });

        dialog.show();
    }

    private void Status(String status)
    {
        final DocumentReference reference = fStore.collection("doctors").document(userId);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.update(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Status("online");

        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Status("offline");

        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisconnectBT().execute();
        }
        Log.d(TAG, "Paused");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    // -------------------------------------- BLUETOOTH --------------------------------------------

    private class ReadInput implements Runnable{
        private boolean bStop = false;
        private Thread t;
        Handler handler = new Handler();

        public ReadInput(){
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning(){
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;
            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop)
                {

                    byte[] buffer = new byte[256];
                    if(inputStream.available() > 0){

                        inputStream.read(buffer);
                        int i = 0;

                        for(i = 0; i < buffer.length && buffer[i] != 0; i++){
                        }
                        strInput = new String(buffer,0,i);

                        if(receiveText)
                        {

                           final Runnable r = new Runnable() {

                               @Override
                               public void run() {
                                   Log.d(TAG,strInput);
//                                   btData = new Bundle();
//                                   btData.putString("btData",strInput);
//                                   doctorHomeFragment = new DoctorHomeFragment();
//                                   doctorHomeFragment.setArguments(btData);
                               }
                           };

                           handler.postDelayed(r,0); //handler delay
                        }
                    }
                    Thread.sleep(0); // Thread delay
                }
            } catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        public void stop(){
            bStop = true;
        }
    }

    public String passBtData()
    {
        return strInput;
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DoctorMainActivity.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }
    }

    private class DisconnectBT extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mReadThread != null){
                mReadThread.stop();
                while (mReadThread.isRunning());
                mReadThread = null;
            }

            try {
                mBTSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mIsBluetoothConnected = false;
            if(mIsUserInitiatedDisconnect){
                finish();
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }





}