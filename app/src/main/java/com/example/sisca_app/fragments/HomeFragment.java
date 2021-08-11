package com.example.sisca_app.fragments;

import android.content.Context;
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

import com.example.sisca_app.MainActivity;
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
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import io.perfmark.Tag;

public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty   public constructor
    }

    private boolean wasRun = true;
    private GraphView graphView;

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;

    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
    private TextView patientNickName, bpmValue, conditionValue, conditionDescription, highBPMtv, medBPMtv;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId, bpm, rrDetection;
    private String relayState = "OFF";
    private DatabaseReference sensorReference, patientParamsReference;
    private Long epoch;
    private Integer bpmInt, hrMax, patientAge;
    private Integer count;
    private double lightThres, moderateThres, hardThres;
    private ProgressBar progressBar;
    private MultiColorCircle colorRing;
    public Queue dataECG = new LinkedList();
    public MyThread graphUpdater;

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
        medBPMtv = (TextView) view.findViewById(R.id.med_bpm);
        colorRing = (MultiColorCircle) view.findViewById(R.id.myRing);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        count = 0;

        colorRing.setWidthOfCircleStroke(15);
        colorRing.setWidthOfBoarderStroke(2);
        colorRing.setColorOfBoarderStroke(ContextCompat.getColor(getContext(), R.color.black));
        graphView = (GraphView) view.findViewById(R.id.graphview);

        series = new LineGraphSeries();
        graphView.addSeries(series);
        Viewport viewport = graphView.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(190);
        viewport.setMinX(0);
        viewport.setMaxX(60);
        viewport.setScrollable(true);

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
                medBPMtv.setText(String.valueOf(hrMax * 0.64));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
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

//                dataECG.offer(bpmInt);
                progressBar.setProgress(bpmInt);
                progressBar.setMin(60);
                progressBar.setMax(hrMax);

                if (bpmInt < 60) {
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100, 0, ContextCompat.getColor(getContext(), R.color.red));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    if (rrDetection.equals("NORMAL")) {
                        conditionDescription.setText("Your heartbeat is too slow and have regular rythm");
                    } else if (rrDetection.equals("ABNORMAL")) {
                        conditionDescription.setText("Your heartbeat is too slow and have irregular rythm");
                    }
                    conditionValue.setText("Bradycardia");
                    conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                } else if (bpmInt >= 60 && bpmInt <= lightThres) {
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100, 0, ContextCompat.getColor(getContext(), R.color.green_navy));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(), R.color.green_navy));
                    if (rrDetection.equals("NORMAL")) {
                        conditionDescription.setText("Your heartbeat normal and have regular rythm");
                    } else if (rrDetection.equals("ABNORMAL")) {
                        conditionDescription.setText("Your heartbeat normal and have irregular rythm");
                    }
                    conditionValue.setText("Normal");
                    conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.green_navy));
                } else if (bpmInt > lightThres && bpmInt <= moderateThres) {
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100, 0, ContextCompat.getColor(getContext(), R.color.bronze));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(), R.color.bronze));
                    if (rrDetection.equals("NORMAL")) {
                        conditionDescription.setText("Your heartbeat is normal and have regular rythm");
                    } else if (rrDetection.equals("ABNORMAL")) {
                        conditionDescription.setText("Your heartbeat is normal and have irregular rythm");
                    }
                    conditionValue.setText("Normal");
                    conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.green_navy));
                } else if (bpmInt > moderateThres) {
                    MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100, 0, ContextCompat.getColor(getContext(), R.color.red));
                    List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                    myList.add(s);
                    colorRing.setCircleStrokes(myList);
                    bpmValue.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    if (rrDetection.equals("NORMAL")) {
                        conditionDescription.setText("Your heartbeat is too fast and have regular rythm");
                    } else if (rrDetection.equals("ABNORMAL")) {
                        conditionDescription.setText("Your heartbeat is too fast and have irregular rythm");
                    }
                    conditionValue.setText("Tachycardia");
                    conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        graphUpdater = new MyThread();
//        graphUpdater.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addRandomEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // manage error
                    }
                }
            }
        }).start();
    }

    private void AddPointChart(int Value) {
        series.appendData(new DataPoint(count, Value), true, 500);
        graphView.addSeries(series);
        graphView.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graphView.getViewport().setScrollable(true);  // activate horizontal scrolling
        graphView.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
        graphView.getViewport().setScrollableY(true);  // activate vertical scrolling
        graphView.getViewport().setMinX(0); // X start from 0
        graphView.getViewport().scrollToEnd();
        count++;
    }

    private void addRandomEntry() {
        series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 80d), true, 200);
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            Integer Value;
            while (true) {
                if (!dataECG.isEmpty()) {
                    Value = Integer.parseInt(String.valueOf(dataECG.poll()));
                    AddPointChart(Value);
                }
            }
        }
    }
}

