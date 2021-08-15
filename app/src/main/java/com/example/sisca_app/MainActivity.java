package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sisca_app.fragments.HomeFragment;
import com.firebase.ui.auth.data.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String userId;
    private Integer patientAge, bpmInt, hrMax;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private Dialog dialog;
    private Vibrator vibrator;
    private double hardThres;
    private DatabaseReference sensorRef, patientParamsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new Dialog(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final long[] pattern = {500, 1000}; // 0.5s sleep, 1s vibrate

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String age = value.getString("age");
                patientAge = Integer.valueOf(age);
                DatabaseReference patientParamsRef = FirebaseDatabase.getInstance().getReference().child("PatientParams");
                patientParamsRef.child("age").setValue(patientAge);
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
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);

                // Checking the BPM value
                if (bpmInt < 60) {
                    // Alert heart beat too slow
                    Toast.makeText(MainActivity.this, "Patient have bradycardia threat", Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(pattern, 0);
                    bradycardiaAlert();
                } else if (bpmInt > 100 && bpmInt < hrMax) {
                    // Alert heart beat too fast
                    Toast.makeText(MainActivity.this, "Patient have tachycardia threat", Toast.LENGTH_SHORT).show();
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


    }

    private void tachycardiaAlert() {
        dialog.setContentView(R.layout.tachycardia_alert);
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
        dialog.setContentView(R.layout.bradycardia_alert);
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
        final DocumentReference reference = fStore.collection("users").document(userId);

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