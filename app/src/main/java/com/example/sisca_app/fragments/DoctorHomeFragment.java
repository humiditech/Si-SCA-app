package com.example.sisca_app.fragments;

import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sisca_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DoctorHomeFragment extends Fragment {

    private final Handler mHandler = new Handler();
    private boolean wasRun = true;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
    private TextView doctorNickName, bpmValue, conditionValue;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId, bpm;
    private ImageView relayButton;
    private String relayState = "OFF";
    private DatabaseReference reference;
    private Long epoch;
    private Integer bpmInt;
    private ProgressBar progressBar;

    public DoctorHomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        if(container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_doctor_home, container, false);

        doctorNickName = (TextView) view.findViewById(R.id.doctor_home_name);
        relayButton = (ImageView) view.findViewById(R.id.defib_button);
        bpmValue = (TextView) view.findViewById(R.id.bpm_value);
        conditionValue = (TextView) view.findViewById(R.id.condition);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
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

        reference = FirebaseDatabase.getInstance().getReference().child("Sensor");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(wasRun)
                {
                    Date currentTime = Calendar.getInstance().getTime();
                    epoch = currentTime.getTime();
                    series = new LineGraphSeries<>(new DataPoint[]{});
                }
                mHandler.postDelayed(this,1000);
            }
        },1000);

        relayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(relayState == "OFF")
                {
                    Toast.makeText(getActivity(), "Defib Triggerred", Toast.LENGTH_SHORT).show();
                    relayState = "ON";
                } else {
                    relayState = "OFF";
                }
                sendRelayCommand(relayState);
            }
        });
        return view;
    }

    private void sendRelayCommand(String relayState) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }

    @Override
    public void onStart() {
        super.onStart();

        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bpm = snapshot.child("ecgBPM").getValue().toString();
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);
                bpmValue.setText(bpm);
                progressBar.setProgress(bpmInt);
                progressBar.setMin(80);
                progressBar.setMax(120);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}