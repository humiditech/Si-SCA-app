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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText rFullName, rNickName, rDoctorName, rAddress, rAge, rEmailAddress, rPassword;
    private Button rButton;
    private TextView rLogin;
    private ProgressBar rProgressBar;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String rUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        rFullName = findViewById(R.id.fullname_register);
        rNickName = findViewById(R.id.nickname_register);
        rDoctorName = findViewById(R.id.doctor_name_register);
        rAddress = findViewById(R.id.address_register);
        rAge = findViewById(R.id.age_register);
        rEmailAddress = findViewById(R.id.email_register);
        rPassword = findViewById(R.id.password_register);
        rLogin = findViewById(R.id.already_registered);
        rButton = findViewById(R.id.register_button);
        rProgressBar = findViewById(R.id.progress_bar_register);
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
                String doctorName = rDoctorName.getText().toString().trim();
                String address = rAddress.getText().toString().trim();
                String age = rAge.getText().toString().trim();


                if(TextUtils.isEmpty(email)){
                    rEmailAddress.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    rPassword.setError("Password is required");
                    return;
                }

                if(password.length() < 8){
                    rPassword.setError("Password must be at least 8 character");
                    return;
                }

                rProgressBar.setVisibility(View.VISIBLE);

                // Register using Firebase

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();
                            rUserID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(rUserID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fName",fullName);
                            user.put("nName",nickName);
                            user.put("dName",doctorName);
                            user.put("addr",address);
                            user.put("age",age);
                            user.put("emailAddr",email);
                            user.put("uid",rUserID);
                            user.put("imageURL", "default");
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("myDebug","onSuccess : user profile is created for " + rUserID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("myDebug", "onFailure : " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            rProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        rLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });
    }
}