package com.example.sisca_app.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sisca_app.LoginActivity;
import com.example.sisca_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

public class DoctorProfileFragment extends Fragment {

    public DoctorProfileFragment() {
        // Required empty public constructor
    }

    private TextView doctorFullName, doctorNickname, doctorAddress,doctorAge;
    private Button scanQrButton,logoutButton;
    private ImageView doctorImage;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId,imageURL;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        if(container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_doctor_profile,container,false);
        scanQrButton = (Button) view.findViewById(R.id.scan_qr_button);
        logoutButton = (Button) view.findViewById(R.id.logout_button);
        doctorFullName = (TextView) view.findViewById(R.id.doctor_fullname);
        doctorNickname = (TextView) view.findViewById(R.id.doctor_nickname);
        doctorAddress = (TextView) view.findViewById(R.id.address);
        doctorAge = (TextView) view.findViewById(R.id.age);
        doctorImage = (ImageView) view.findViewById(R.id.profile_image);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("doctors").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                doctorFullName.setText(value.getString("fName"));
                doctorNickname.setText(value.getString("nName"));
                doctorAddress.setText(value.getString("addr"));
                doctorAge.setText(value.getString("age"));

                imageURL = value.getString("imageURL");
                if(imageURL.equals("default"))
                {
                    doctorImage.setImageResource(R.drawable.default_profile);
                } else
                {
                    Glide.with(getActivity().getApplicationContext()).load(imageURL).into(doctorImage);
                }
            }
        });

        scanQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Scan QR Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(),LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }
}