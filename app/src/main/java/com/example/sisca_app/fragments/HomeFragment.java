package com.example.sisca_app.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sisca_app.MultiColorCircle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty   public constructor
    }

    private final Handler mHandler = new Handler();
    private boolean wasRun = true;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
    private TextView patientNickName, bpmValue, conditionValue, conditionDescription,highBPMtv;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId, bpm, rrDetection;
    private String relayState = "OFF";
    private DatabaseReference sensorReference,patientParamsReference;
    private Long epoch;
    private Integer bpmInt;
    private double lightThres,moderateThres,hardThres;
    private Integer hrMax;
    private Integer patientAge;
    private ProgressBar progressBar;
    private MultiColorCircle colorRing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_home, container, false);
        patientNickName = (TextView) view.findViewById(R.id.patient_home_name);
        bpmValue = (TextView) view.findViewById(R.id.bpm_value);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        conditionValue = (TextView) view.findViewById(R.id.condition);
        conditionDescription = (TextView) view.findViewById(R.id.condition_description);
        highBPMtv = (TextView) view.findViewById(R.id.high_bpm);
        colorRing = (MultiColorCircle) view.findViewById(R.id.myRing);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        colorRing.setWidthOfCircleStroke(15);
        colorRing.setWidthOfBoarderStroke(2);
        colorRing.setColorOfBoarderStroke(ContextCompat.getColor(getContext(),R.color.black));
//        graphView = (GraphView) view.findViewById(R.id.graphview);

//        graphView.addSeries(series);
//        graphView.getViewport().setScrollable(true);
//        graphView.getViewport().setScrollableY(true);
//        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
//            @Override
//            public String formatLabel(double value, boolean isValueX) {
//                if(isValueX)
//                {
//                    return sdf.format(new Date((long) value));
//                }
//                else{
//                    return super.formatLabel(value, isValueX);
//                }
//            }
//        });
//        graphView.getGridLabelRenderer().setNumHorizontalLabels(3);
//        graphView.getGridLabelRenderer().setHumanRounding(false);

//        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
//            @Override
//            public String formatLabel(double value, boolean isValueX) {
//                if(isValueX)
//                {
//                    Date currentTime = Calendar.getInstance().getTime();
//                    return sdf.format(currentTime.getTime());
//                }
//                else{
//                    return super.formatLabel(value, isValueX);
//                }
//            }
//        });

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                patientNickName.setText(value.getString("nName"));
            }
        });

        sensorReference = FirebaseDatabase.getInstance().getReference().child("Sensor");
        patientParamsReference = FirebaseDatabase.getInstance().getReference().child("PatientParams");
        patientParamsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientAge = snapshot.child("age").getValue(Integer.class);
                hrMax = 220 - patientAge;
                lightThres = hrMax * 0.5;
                moderateThres = hrMax * 0.76;
                hardThres = hrMax * 0.93;
                highBPMtv.setText(String.valueOf(hrMax));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(wasRun)
                {
                    Date currentTime = Calendar.getInstance().getTime();
                    epoch = currentTime.getTime();
                    Log.d("myLOG", String.valueOf(epoch));
                    series = new LineGraphSeries<>(new DataPoint[]{});

                }
                mHandler.postDelayed(this,1000);
            }
        },1000);

        return view;
    }

    private void sendRelayCommand(final String relayState) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }

    @Override
    public void onStart() {
        super.onStart();
        sensorReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bpm = snapshot.child("ecgBPM").getValue().toString();
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);
                bpmValue.setText(bpm);
                rrDetection = snapshot.child("rrDetection").getValue().toString();

                progressBar.setProgress(bpmInt);
                progressBar.setMin(60);
                progressBar.setMax(hrMax);

                if(bpmInt <= lightThres)
                {
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100,0,ContextCompat.getColor(getContext(),R.color.green_navy));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(),R.color.green_navy));
                    if(rrDetection.equals("NORMAL"))
                    {
                        conditionDescription.setText("Your heartbeat is too slow and have regular rythm");
                    } else if(rrDetection.equals("ABNORMAL"))
                    {
                        conditionDescription.setText("Your heartbeat is too slow and have irregular rythm");
                    }
                    conditionValue.setText("Bradycardia");
                }
                else if (bpmInt > lightThres && bpmInt <= moderateThres)
                {
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100,0,ContextCompat.getColor(getContext(),R.color.bronze));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(),R.color.bronze));
                    if(rrDetection.equals("NORMAL"))
                    {
                        conditionDescription.setText("Your heartbeat is normal and have regular rythm");
                    } else if(rrDetection.equals("ABNORMAL"))
                    {
                        conditionDescription.setText("Your heartbeat is normal and have irregular rythm");
                    }
                    conditionValue.setText("Normal");
                }
                else if (bpmInt > moderateThres ){
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(50,0,ContextCompat.getColor(getContext(),R.color.red));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(),R.color.red));
                    if(rrDetection.equals("NORMAL"))
                    {
                        conditionDescription.setText("Your heartbeat is too fast and have regular rythm");
                    } else if(rrDetection.equals("ABNORMAL"))
                    {
                        conditionDescription.setText("Your heartbeat is too fast and have irregular rythm");
                    }
                    conditionValue.setText("Tachycardia");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}