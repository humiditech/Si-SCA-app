package com.example.sisca_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sisca_app.Adapters.ChatAdapter;
import com.example.sisca_app.Adapters.UserAdapter;
import com.example.sisca_app.Models.ChatsModel;
import com.example.sisca_app.Models.DoctorsModel;
import com.example.sisca_app.Models.UsersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String friendId, message, myId;
    CircleImageView imageViewOnToolbar;
    TextView usernameOnToolbar;
    Toolbar toolbar;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    EditText et_messages;
    Button sendButton;
    List<ChatsModel> chatList;
    ChatAdapter cAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar_message);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageViewOnToolbar = findViewById(R.id.profile_image_toolbar_message);
        usernameOnToolbar = findViewById(R.id.username_ontoolbar_message);

        recyclerView = findViewById(R.id.recyclerview_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        et_messages = findViewById(R.id.edit_message_text);
        sendButton = findViewById(R.id.send_button_chat);

        fStore = FirebaseFirestore.getInstance();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        myId = fUser.getUid();

        friendId = getIntent().getStringExtra("friendId");
        fUser = FirebaseAuth.getInstance().getCurrentUser();


        DocumentReference reference = fStore.collection("doctors").document(friendId);
        reference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                DoctorsModel doctor = value.toObject(DoctorsModel.class);
                usernameOnToolbar.setText(doctor.getnName());

                if (doctor.getImageURL().equals("default")) {
                    imageViewOnToolbar.setImageResource(R.drawable.ic_baseline_person_24);
                } else {
                    Glide.with(getApplicationContext()).load(doctor.getImageURL()).into(imageViewOnToolbar);
                }

                readMessage(myId, friendId);

            }
        });

        et_messages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = et_messages.getText().toString();
                if (!text.startsWith(" ")) {
                    et_messages.getText().insert(0, " ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = et_messages.getText().toString();
                sendMessage(myId, friendId, message);
                et_messages.setText(" ");
            }
        });


    }

    private void readMessage(final String myId, final String friendId) {
        chatList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    ChatsModel chats = ds.getValue(ChatsModel.class);

                    if (chats.getSender().equals(myId) && chats.getReceiver().equals(friendId) ||
                            chats.getSender().equals(friendId) && chats.getReceiver().equals(myId))
                    {
                        chatList.add(chats);
                    }

                    cAdapter = new ChatAdapter(ChatActivity.this,chatList);
                    recyclerView.setAdapter(cAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(final String myId, final String friendId, final String message) {

        // Using Realtime Database
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", myId);
        hashMap.put("receiver", friendId);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }

}