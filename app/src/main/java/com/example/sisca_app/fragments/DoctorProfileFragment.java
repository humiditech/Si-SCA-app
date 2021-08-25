package com.example.sisca_app.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

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
    private Uri imageUri = null;
    public static final int CAMERA_CODE = 200;
    public static final int GALLERY_CODE = 100;


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
                    Glide.with(getApplicationContext()).load(imageURL).into(doctorImage);
                }
            }
        });

        doctorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage();
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

    private void changeImage() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose an Option");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        openCamera();
                        break;
                    case 1:
                        openGallery();
                        break;
                    default:
                        break;
                }
            }
        });

        builder.create().show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.TITLE, "Temp Desc");
        imageUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_CODE);
    }

    private void askPermission() {
        Dexter.withContext(getContext())
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            String filepath = "DoctorPhotos/" + "doctorprofile_" + userId;

            StorageReference reference = FirebaseStorage.getInstance().getReference(filepath);
            reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();

                            DocumentReference userReference = fStore.collection("doctors").document(userId);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("imageURL", imageURL);
                            userReference.update(hashMap);
                        }
                    });
                }
            });

        }

        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            Uri uri = imageUri;
            String filepath = "DoctorPhotos/" + "doctorprofile_" + userId;

            StorageReference reference = FirebaseStorage.getInstance().getReference(filepath);
            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();

                            DocumentReference userReference = fStore.collection("doctors").document(userId);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("imageURL", imageURL);
                            userReference.update(hashMap);
                        }
                    });
                }
            });

        }
    }
}