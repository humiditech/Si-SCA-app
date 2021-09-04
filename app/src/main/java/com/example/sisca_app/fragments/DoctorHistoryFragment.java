package com.example.sisca_app.fragments;

import android.app.DatePickerDialog;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sisca_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Text;

import java.util.Calendar;

public class DoctorHistoryFragment extends Fragment {
    private TextView datepicker,doctorNickName,patientAge;
    private DatePickerDialog.OnDateSetListener setListener;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private ImageView doctorImage;
    private String userId,imageURL;

    public DoctorHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        if(container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_doctor_history, container, false);
        datepicker = (TextView) view.findViewById(R.id.datepicker_tv);
        doctorNickName = (TextView) view.findViewById(R.id.doctor_history_name);
        patientAge = (TextView) view.findViewById(R.id.patient_age);
        doctorImage = (ImageView) view.findViewById(R.id.doctor_history_image);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("doctors").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                doctorNickName.setText(value.getString("nName"));
                imageURL = value.getString("imageURL");
                if(imageURL.equals("default"))
                {
                    doctorImage.setImageResource(R.drawable.default_profile);
                } else
                {
                    if(getActivity() != null) Glide.with(getActivity().getApplicationContext()).load(imageURL).into(doctorImage);
                }
            }
        });

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        datepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String date = day + "/" + month + "/" + year;
                        datepicker.setText(date);
                    }
                },
                year,month,day);
                datePickerDialog.show();
            }
        });

        return view;
    }
}