package com.example.sisca_app.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sisca_app.Adapters.UserAdapter;
import com.example.sisca_app.Models.UsersModel;
import com.example.sisca_app.R;
import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorListFragment extends Fragment {

    public DoctorListFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private List<UsersModel> usersList;
    private FirebaseUser fUser;
    private UserAdapter uAdapter;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_doctor__list, container, false);
        recyclerView = view.findViewById(R.id.recyclerview_doctors);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        displayUsers();


        return view;
    }

    private void displayUsers() {
        usersList = new ArrayList<>();

        CollectionReference reference = fStore.collection("users");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                usersList.clear();

                if (task.isSuccessful()) {
                    Log.d("myDebug"," reference success");
                    for (DocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            UsersModel user = document.toObject(UsersModel.class);

                            if (!user.getUid().equals(userId)) {
                                usersList.add(user);
                            }

                            uAdapter = new UserAdapter(getContext(), usersList, true);
                            recyclerView.setAdapter(uAdapter);
                        }
                    }
                }
            }
        });

    }


}