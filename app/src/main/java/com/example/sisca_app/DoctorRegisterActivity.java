package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class DoctorRegisterActivity extends AppCompatActivity {

    private EditText rFullName, rNickName, rAddress, rAge, rEmailAddress, rPassword;
    private Button rButton;
    private TextView rLogin;
    private ProgressBar rProgressBar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String rDoctorID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_register);

        rFullName = findViewById(R.id.d_fullname_register);
        rNickName = findViewById(R.id.d_nickname_register);
        rAddress = findViewById(R.id.d_address_register);
        rAge = findViewById(R.id.d_age_register);
        rEmailAddress = findViewById(R.id.d_email_register);
        rPassword = findViewById(R.id.d_password_register);
        rLogin = findViewById(R.id.d_already_registered);
        rButton = findViewById(R.id.d_register_button);
        rProgressBar = findViewById(R.id.d_progress_bar_register);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null)
        {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        }

        rButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = rEmailAddress.getText().toString().trim();
                String password = rPassword.getText().toString().trim();
                String fullName = rFullName.getText().toString().trim();
                String nickName = rNickName.getText().toString().trim();
                String address = rAddress.getText().toString().trim();
                String age = rAge.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    rEmailAddress.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    rPassword.setError("Password is required");
                    return;
                }

                if(password.length() < 8)
                {
                    rPassword.setError("Password must be at least 8 character");
                    return;
                }

                rProgressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(DoctorRegisterActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();
                            rDoctorID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("doctors").document(rDoctorID);
                            Map<String, Object> doctor = new HashMap<>();
                            doctor.put("fName",fullName);
                            doctor.put("nName",nickName);
                            doctor.put("addr",address);
                            doctor.put("age",age);
                            doctor.put("emailAddr",email);
                            doctor.put("uid", rDoctorID);
                            doctor.put("imageURL","default");
                            doctor.put("role","doctor");
                            documentReference.set(doctor).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("myDebug", "onSuccess : docto profile is created for " + rDoctorID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("myDebug", "onFailure : " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        }
                        else {
                            Toast.makeText(DoctorRegisterActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            rProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        });
    }
}