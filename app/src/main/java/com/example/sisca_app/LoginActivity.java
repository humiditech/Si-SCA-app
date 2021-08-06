package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    TextView registerHere;
    EditText lEmail, lPassword;
    Button lButton,doctorLButton;
    ProgressBar lProgressBar;
    String personID;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

        registerHere = findViewById(R.id.register_here);
        lEmail = findViewById(R.id.email_login);
        lPassword = findViewById(R.id.password_login);
        lButton = findViewById(R.id.login_button);
        doctorLButton = findViewById(R.id.login_as_doctor);
        lProgressBar = findViewById(R.id.progress_bar_login);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        registerHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                finish();
            }
        });

        lButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = lEmail.getText().toString().trim();
                String password = lPassword.getText().toString().trim();

                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(lPassword.getApplicationWindowToken(),0);

                Log.d("login_debug",email);
                Log.d("login_debug",password);

                if(TextUtils.isEmpty(email))
                {
                    lEmail.setError("Insert your email");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    lPassword.setError("Insert your password");
                    return;
                }

                lProgressBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            personID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(personID);
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                        } else {
                                            Log.d("TAG", "No such document");
                                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                        }
                                    }
                                    else {
                                        Log.d("TAG", "get failed with ", task.getException());
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            lProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        doctorLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = lEmail.getText().toString().trim();
                String password = lPassword.getText().toString().trim();

                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(lPassword.getApplicationWindowToken(),0);

                Log.d("login_debug",email);
                Log.d("login_debug",password);

                if(TextUtils.isEmpty(email))
                {
                    lEmail.setError("Insert your email");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    lPassword.setError("Insert your password");
                    return;
                }

                lProgressBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            personID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("doctors").document(personID);
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),DoctorMainActivity.class));
                                        } else {
                                            Log.d("TAG", "No such document");
                                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                        }
                                    }
                                    else {
                                        Log.d("TAG", "get failed with ", task.getException());
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            lProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }



}