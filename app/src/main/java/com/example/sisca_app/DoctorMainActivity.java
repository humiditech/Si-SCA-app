package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.util.HashMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);
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
//        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                HashMap<String,Object> hashMap = new HashMap<>();
//
//                hashMap.put("status",status);
//                Log.d("myTag",status);
//
//                reference.update(hashMap);
//            }
//        });
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.update(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Status("offline");
    }

}