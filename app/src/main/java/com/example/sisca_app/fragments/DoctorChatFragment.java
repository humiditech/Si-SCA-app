package com.example.sisca_app.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sisca_app.Adapters.UserAdapter;
import com.example.sisca_app.Models.UsersModel;
import com.example.sisca_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorChatFragment extends Fragment {
    public DoctorChatFragment() {
        // Required empty public constructor
    }

    private TextView doctorNickName;
    private RecyclerView recyclerView;
    private List<UsersModel> userList;
    private UserAdapter uAdapter;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        if(container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_doctor_chat, container, false);
        doctorNickName = (TextView) view.findViewById(R.id.doctor_chat_name);
        recyclerView = view.findViewById(R.id.recyclerview_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("doctors").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                doctorNickName.setText(value.getString("nName"));
            }
        });

        displayUsers();

        return view;
    }

    private void displayUsers() {
        userList = new ArrayList<>();

        CollectionReference reference = fStore.collection("users");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                userList.clear();

                if(task.isSuccessful()){
                    for(DocumentSnapshot document :task.getResult())
                    {
                        if(document.exists())
                        {
                            UsersModel user = document.toObject(UsersModel.class);
                            userList.add(user);
                            uAdapter = new UserAdapter(getContext(),userList);
                            recyclerView.setAdapter(uAdapter);
                        }
                    }
                }
            }
        });
    }
}