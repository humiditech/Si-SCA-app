package com.example.sisca_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.sisca_app.MainActivity;
import com.example.sisca_app.Models.DataPointModel;
import com.example.sisca_app.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty   public constructor
    }

    private final Handler mHandler = new Handler();
    private boolean wasRun = true;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
    private TextView patientNickName, bpmValue, conditionValue;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId, bpm;
    private Button relayButton;
    private String relayState = "OFF";
    private DatabaseReference reference;
    private Long epoch;
    private Integer bpmInt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_home, container, false);



        // ------------
        patientNickName = (TextView) view.findViewById(R.id.patient_home_name);
        relayButton = (Button) view.findViewById(R.id.relay_button);
        bpmValue = (TextView) view.findViewById(R.id.bpm_value);
        conditionValue = (TextView) view.findViewById(R.id.condition);
        graphView = (GraphView) view.findViewById(R.id.graphview);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();





        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX)
                {
                    Date currentTime = Calendar.getInstance().getTime();
                    return sdf.format(currentTime.getTime());
                }
                return super.formatLabel(value, isValueX);
            }
        });

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                patientNickName.setText(value.getString("nName"));
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("Sensor");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bpm = snapshot.child("ecgBPM").getValue().toString();
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);
                bpmValue.setText(bpm);
                Log.d("myLOG", String.valueOf(bpmInt));
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
                    series = new LineGraphSeries<>(new DataPoint[] {
                            new DataPoint(epoch,50)
                    });
                    graphView.addSeries(series);

                }
                mHandler.postDelayed(this,1000);
            }
        },1000);


        relayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relayState == "OFF") {
                    relayState = "ON";
                    sendRelayCommand(relayState);
                } else {
                    relayState = "OFF";
                    sendRelayCommand(relayState);
                }
            }
        });

        return view;
    }

    private void sendRelayCommand(final String relayState) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Relay");
        reference.child("relayState").setValue(relayState);
    }

    @Override
    public void onStart() {
        super.onStart();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bpm = snapshot.child("ecgBPM").getValue().toString();
                bpmInt = snapshot.child("ecgBPM").getValue(Integer.class);
                bpmValue.setText(bpm);
                Log.d("myLOG", String.valueOf(bpmInt));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}