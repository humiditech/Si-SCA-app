package com.example.sisca_app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sisca_app.LoginActivity;
import com.example.sisca_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.w3c.dom.Text;


public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }

    private TextView patientFullName,patientNickName,patientAddress,patientAge,patientDoctorName;
    private Button showQrButton,logoutButton;
    private Dialog qrDialog;
    private ImageView qrOutput;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        if (container == null) return null;
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_profile, container, false);
        showQrButton = (Button) view.findViewById(R.id.show_qr_button);
        logoutButton = (Button) view.findViewById(R.id.logout_button);
        patientFullName = (TextView) view.findViewById(R.id.patient_name);
        patientNickName = (TextView) view.findViewById(R.id.patient_nickname);
        patientAddress = (TextView) view.findViewById(R.id.address);
        patientAge = (TextView) view.findViewById(R.id.age);
        patientDoctorName = (TextView) view.findViewById(R.id.doctor_name);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        qrDialog = new Dialog(getContext());

        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                patientFullName.setText(value.getString("fName"));
                patientNickName.setText(value.getString("nName"));
                patientAddress.setText(value.getString("addr"));
                patientAge.setText(value.getString("age"));
                patientDoctorName.setText(value.getString("dName"));
            }
        });


        showQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String data = patientFullName.getText().toString() + "," + patientAge.getText().toString() + "," + "patient" + "," + patientAddress.getText().toString() + "," + patientDoctorName.getText().toString();
                MultiFormatWriter writer = new MultiFormatWriter();
                try{
                    BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE,200,200);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Bitmap bitmap = encoder.createBitmap(matrix);
                    loadPhoto(bitmap,600,600);

                } catch (WriterException e) {
                    e.printStackTrace();
                }

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


    private void loadPhoto(Bitmap bitmap, int width, int height)
    {
        Bitmap tempImageView = bitmap;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog imageDialog = builder.create();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_qr,(ViewGroup) getActivity().findViewById(R.id.qr_container));
        ImageView image = (ImageView) layout.findViewById(R.id.qr_output);
        image.setImageBitmap(tempImageView);
        imageDialog.setView(layout);

        imageDialog.show();
        imageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imageDialog.getWindow().setLayout(width,height);
    }

}