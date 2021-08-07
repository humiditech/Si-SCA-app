package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DoctorMainActivity extends AppCompatActivity {

    private String imuStatus,rrStatus;
    private ImageView startDefib, cancelDefib;
    private Dialog dialog;
    private String relayState = "OFF";
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);
        dialog = new Dialog(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final long[] pattern = {500,1000}; // 2s sleep, 1s vibrate

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

        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference().child("Sensor");
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imuStatus = snapshot.child("imuSensorStatus").getValue().toString();
                rrStatus = snapshot.child("rrDetection").getValue().toString();
                if(imuStatus.trim().equals("FALL"))
                {
                    Toast.makeText(DoctorMainActivity.this, "Patient Falling", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern,0);
                    openAlertDialog();
                }

                if(rrStatus.trim().equals("ABNORMAL"))
                {
                    Toast.makeText(DoctorMainActivity.this, "Arythmia Detected", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern,0);
                    openAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.doctor_navigation);
        NavController navController = Navigation.findNavController(this,R.id.doctor_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }

    private void openAlertDialog() {
        dialog.setContentView(R.layout.alert_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        startDefib = dialog.findViewById(R.id.defib_start_button);
        cancelDefib = dialog.findViewById(R.id.defib_cancel_button);

        startDefib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorMainActivity.this, "Defibrilator triggered", Toast.LENGTH_SHORT).show();
                if(relayState.trim().equals("OFF"))
                {
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

        dialog.show();
    }

    private void sendRelayCommand(String relayState) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }
}