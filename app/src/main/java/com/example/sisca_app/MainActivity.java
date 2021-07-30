package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private String imuStatus;
    private ImageView startDefib, cancelDefib;
    private Dialog dialog;
    private String relayState = "OFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new Dialog(this);

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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Sensor");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imuStatus = snapshot.child("imuSensorStatus").getValue().toString();
                if(imuStatus.trim().equals("FALL"))
                {
                    Toast.makeText(MainActivity.this, "Patient Falling", Toast.LENGTH_SHORT).show();
                    openAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        NavController navController = Navigation.findNavController(this,R.id.fragment);
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
                Toast.makeText(MainActivity.this, "Defibrilator triggered", Toast.LENGTH_SHORT).show();
                if (relayState.trim().equals("OFF"))
                {
                    relayState = "ON";
                    sendRelayCommand(relayState);
                    dialog.dismiss();
                }
            }
        });

        cancelDefib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relayState = "OFF";
                sendRelayCommand(relayState);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Patient is safe", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void sendRelayCommand(final String relayState)
    {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }

}