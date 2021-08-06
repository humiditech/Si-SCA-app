package com.example.sisca_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DoctorMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.doctor_navigation);
        NavController navController = Navigation.findNavController(this,R.id.doctor_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView,navController);
    }
}