package com.example.sisca_app.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sisca_app.DoctorMainActivity;
import com.example.sisca_app.MultiColorCircle;
import com.example.sisca_app.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class DoctorHomeFragment extends Fragment implements OnChartValueSelectedListener {

    private boolean wasRun = true;
    //    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    private TextView doctorNickName, bpmValue, conditionValue, conditionDescription, highBPMtv, medBPMtv;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId, bpm, rrDetection, imageURL, btStatus;
    private ImageView relayButton, doctorImage, btIcon;
    private String relayState = "OFF";
    private DatabaseReference sensorReference, patientParamsReference;
    private Integer bpmInt, hrMax, patientAge, ecgSignalInt;
    private double lightThres, moderateThres, hardThres;
    private ProgressBar progressBar;
    private MultiColorCircle colorRing;
    public Queue dataECG = new LinkedList();
    public Integer ecgSignalIntValue;
    private static final String TAG = "doctorHomeTAG";
    Bundle btData;
    DoctorMainActivity dActivity;
    Double tmp = 0.0;
    Integer bufferCounter = 0;

    // MPChart
    private LineChart mpChartGraph;
//    private Thread thread;
    private String[] parsedData = {"0.0"};
    private float maxEKGValue = 800f;

    public DoctorHomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_doctor_home, container, false);
//        btData = getArguments();
//        if (btData != null) {
//            Log.d(TAG, btData.getString("btData"));
//        }

        dActivity = (DoctorMainActivity) getActivity();

        doctorNickName = (TextView) view.findViewById(R.id.doctor_home_name);
        relayButton = (ImageView) view.findViewById(R.id.defib_button);
        bpmValue = (TextView) view.findViewById(R.id.bpm_value);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        conditionValue = (TextView) view.findViewById(R.id.condition);
        conditionDescription = (TextView) view.findViewById(R.id.condition_description);
        highBPMtv = (TextView) view.findViewById(R.id.high_bpm);
        medBPMtv = (TextView) view.findViewById(R.id.med_bpm);
        colorRing = (MultiColorCircle) view.findViewById(R.id.myRing);
        doctorImage = (ImageView) view.findViewById(R.id.doctor_home_image);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        ecgSignalIntValue = 0;

        colorRing.setWidthOfCircleStroke(15);
        colorRing.setWidthOfBoarderStroke(2);
        colorRing.setColorOfBoarderStroke(ContextCompat.getColor(getContext(), R.color.black));
        mpChartGraph = (LineChart) view.findViewById(R.id.linechart);

        initMPChart(mpChartGraph);

//        graphView = (GraphView) view.findViewById(R.id.graphview);

//        series = new LineGraphSeries();
//        graphView.addSeries(series);
//        graphView.getGridLabelRenderer().setNumHorizontalLabels(5);
//        graphView.getGridLabelRenderer().setHumanRounding(true);
//        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
//        graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
//        Viewport viewport = graphView.getViewport();
//
//        viewport.setXAxisBoundsManual(true);
//        viewport.setYAxisBoundsManual(true);
//        viewport.setMinX(0);
//        viewport.setMaxX(1000);
//        viewport.setMinY(0);
//        viewport.setMaxY(800);
//        viewport.setScalable(true);
//        viewport.setScalableY(true);
//        viewport.setScrollable(true);

        DocumentReference documentReference = fStore.collection("doctors").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("ERROR", error.getMessage());
                    return;
                }

                if (value != null && value.exists()) {
                    doctorNickName.setText(value.getString("nName"));
                    imageURL = value.getString("imageURL");
                    if (imageURL.equals("default")) {
                        doctorImage.setImageResource(R.drawable.default_profile);
                    } else {
                        Glide.with(getActivity().getApplicationContext()).load(imageURL).into(doctorImage);
                    }
                }

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
                float mediumBPM = (float) (hrMax * 0.64);
                String medBPM = new DecimalFormat("#.00").format(mediumBPM);
                highBPMtv.setText(String.valueOf(hrMax));
                medBPMtv.setText(medBPM);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        relayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relayState == "OFF") {
                    Toast.makeText(getActivity(), "Defib Triggerred", Toast.LENGTH_SHORT).show();
                    relayState = "ON";
                } else {
                    relayState = "OFF";
                }
                sendRelayCommand(relayState);
            }
        });
        readSensorData();
        return view;
    }

    private void sendRelayCommand(String relayState) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }

    @Override
    public void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity != null) {

                    while (true) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String bluetoothData = dActivity.passBtData();
                                parsedData = bluetoothData.split("\n");
                                try {
                                    if (bluetoothData != null) {
                                        addEntry(Float.parseFloat(parsedData[-1]));
                                        Log.d(TAG,bluetoothData);
                                    }
                                } catch (NumberFormatException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        try{
                            Thread.sleep(10);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }


    public void readSensorData() {
        sensorReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bpm = snapshot.child("ecgBPM").getValue().toString();
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);
                ecgSignalInt = snapshot.child("ecgSignal").getValue(Integer.class);
                ecgSignalIntValue = ecgSignalInt;
//                myCallback.onCallback(ecgSignalInt);
                bpmValue.setText(bpm);
                rrDetection = snapshot.child("rrDetection").getValue().toString();
//                dataECG.offer(bpmInt);
                progressBar.setProgress(bpmInt);
                progressBar.setMin(60);
                progressBar.setMax(hrMax);

                Context context = getContext();

                if (context != null) {
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
                        if (bpmInt < 100) {
                            if (rrDetection.equals("NORMAL")) {
                                conditionDescription.setText("Your heartbeat normal and have regular rythm");
                            } else if (rrDetection.equals("ABNORMAL")) {
                                conditionDescription.setText("Your heartbeat normal and have irregular rythm");
                            }
                            conditionValue.setText("Normal");
                            conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.green_navy));
                        } else {
                            if (rrDetection.equals("NORMAL")) {
                                conditionDescription.setText("Your heartbeat too fast and have regular rythm");
                            } else if (rrDetection.equals("ABNORMAL")) {
                                conditionDescription.setText("Your heartbeat too fast and have irregular rythm");
                            }
                            conditionValue.setText("Tachycardia");
                            conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                        }
                    } else if (bpmInt > lightThres && bpmInt <= moderateThres) {
                        MultiColorCircle.CustomStrokeObject s = new MultiColorCircle.CustomStrokeObject(100, 0, ContextCompat.getColor(getContext(), R.color.bronze));
                        List<MultiColorCircle.CustomStrokeObject> myList = new ArrayList<>();
                        myList.add(s);
                        colorRing.setCircleStrokes(myList);
                        bpmValue.setTextColor(ContextCompat.getColor(getContext(), R.color.bronze));

                        if (bpmInt < 100) {
                            if (rrDetection.equals("NORMAL")) {
                                conditionDescription.setText("Your heartbeat is normal and have regular rythm");
                            } else if (rrDetection.equals("ABNORMAL")) {
                                conditionDescription.setText("Your heartbeat is normal and have irregular rythm");
                            }
                            conditionValue.setText("Normal");
                            conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.green_navy));
                        } else {
                            if (rrDetection.equals("NORMAL")) {
                                conditionDescription.setText("Your heartbeat is too fast and have regular rythm");
                            } else if (rrDetection.equals("ABNORMAL")) {
                                conditionDescription.setText("Your heartbeat is too fast and have irregular rythm");
                            }
                            conditionValue.setText("Tachycardia");
                            conditionValue.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                        }
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


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void initMPChart(LineChart graph) {
        graph.setOnChartValueSelectedListener(this);
        graph.getDescription().setEnabled(false);
        graph.setTouchEnabled(true);

        graph.setDragEnabled(true);
        graph.setScaleEnabled(true);
        graph.setDrawGridBackground(false);

        graph.setPinchZoom(true);

        graph.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        graph.setData(data);

        XAxis xl = graph.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(800f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.BLACK);

        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "ECG Signal");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLACK);
        set.setLineWidth(2f);
        set.setCircleRadius(0);
        set.setFillAlpha(65);
        set.setFillColor(Color.BLACK);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLUE);
        set.setValueTextSize(9f);
        set.setCubicIntensity(0.2f);
        set.setDrawCircles(false);
        set.setCircleRadius(0f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        return set;
    }

    private void addEntry(float value) {
        LineData data = mpChartGraph.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), value), 0);

            data.notifyDataChanged();
            mpChartGraph.notifyDataSetChanged();
            mpChartGraph.setVisibleXRangeMaximum(360);
            mpChartGraph.moveViewToX(data.getEntryCount());
        }
    }


}