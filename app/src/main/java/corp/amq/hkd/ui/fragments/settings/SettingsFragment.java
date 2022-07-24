package corp.amq.hkd.ui.fragments.settings;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sha.photoviewer.PhotoViewer;
import com.sha.photoviewer.listener.ImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import corp.amq.hkd.R;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.databinding.FragmentProfileSettingsBinding;
import corp.amq.hkd.databinding.FragmentRegistrationBinding;
import corp.amq.hkd.ui.DashboardActivity;
import corp.amq.hkd.ui.fragments.profile.adapters.ImageAdapter;


public class SettingsFragment extends Fragment {

    private FragmentProfileSettingsBinding binding;
    private FirebaseFirestore db;
    private Context context;
    private FirebaseAuth mAuth;
    private Uri imageuri;
    private FirebaseStorage storageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance();
    }

    private ImageAdapter imageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);

        String[] GENDER = getResources().getStringArray(R.array.gender_array);
        String[] RANKS = getResources().getStringArray(R.array.rank_array);
        String[] ROLES = getResources().getStringArray(R.array.role_array);


        ArrayAdapter<String> gender_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, GENDER);
        ArrayAdapter<String> ranks_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, RANKS);
        ArrayAdapter<String> roles_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, ROLES);
        binding.autoCompleteTextView.setAdapter(gender_adapter);
        binding.autoCompleteTextView1.setAdapter(ranks_adapter);
        binding.autoCompleteTextView2.setAdapter(roles_adapter);

        AwesomeValidation mAwesomeValidation = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

        mAwesomeValidation.addValidation(binding.inputIgnLayout, "^[a-zA-Z0-9](_(?!(\\.|_))|\\.(?!(_|\\.))|[a-zA-Z0-9]){1,18}[a-zA-Z0-9]$", "Please check the input");
        mAwesomeValidation.addValidation(binding.gender, "^[ A-Za-z0-9_@./#&+-]*$", "Please check the input");
        mAwesomeValidation.addValidation(binding.role, "[a-zA-Z\\s]+", "Please check the input");
        mAwesomeValidation.addValidation(binding.rank, "[a-zA-Z\\s]+", "Please check the input");

        binding.updateBtn.setOnClickListener(view -> {
            if(mAwesomeValidation.validate()) {

                db.collection("users").whereEqualTo("uid", mAuth.getUid()).get()
                        .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getDocuments().size() > 0) {

                                            Map<String, Object> user = new HashMap<>();
                                            user.put("display_name", binding.inputIgn.getText().toString());
                                            user.put("gender", binding.gender.getEditText().getText().toString());
                                            user.put("rank", binding.rank.getEditText().getText().toString());
                                            user.put("role",binding.role.getEditText().getText().toString());
                                            user.put("bio", binding.inputBio.getText().toString());

                                            db.collection("users")
                                                    .document(task.getResult().
                                                            getDocuments().get(0).getId())
                                                    .update(user)
                                                    .addOnSuccessListener(unused -> {
                                                        Snackbar snackbar = Snackbar
                                                                .make(binding.getRoot(),
                                                                        "Update success!", Snackbar.LENGTH_LONG);
                                                        snackbar.show();
                                                    });


                                        }
                                    }
                                }
                        );



            }
        });




        binding.imageGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String[] options = {"Remove the image"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Image Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        db.collection("users").whereEqualTo("uid", mAuth.getUid())
                                .get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                if(task.getResult().getDocuments().size() > 0) {
                                    db.collection("users")
                                            .whereEqualTo("uid", mAuth.getUid())
                                            .get().addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    if (!task1.getResult().isEmpty()) {
                                                        db.collection("users")
                                                                .document(task.getResult().getDocuments().get(0).getId())
                                                                .update("featured_image",
                                                                        FieldValue.arrayRemove(imageAdapter.getItem(i))
                                                                );
                                                        loadProfile();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                });
                builder.create().show();
                return false;
            }
        });

        loadProfile();

        binding.imageBtn.setOnClickListener(view -> {
            TYPE = 0;
            selectPictureDialog();
        });

        binding.addFeatureImage.setOnClickListener(view -> {
            TYPE = 1;
            selectPictureDialog();
        });

        binding.changePasswordBtn.setOnClickListener(view -> {
            View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password,
                    (ViewGroup) getView(), false);
            final EditText input = (EditText) viewInflated.findViewById(R.id.input);
            final TextInputLayout inputLayout = (TextInputLayout) viewInflated.findViewById(R.id.input_password_layout);

            final EditText input2 = (EditText) viewInflated.findViewById(R.id.input2);
            final TextInputLayout inputLayout2 = (TextInputLayout) viewInflated.findViewById(R.id.input_password2_layout);

            AwesomeValidation passValid = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

            String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";
            passValid.addValidation(inputLayout, regexPassword,
                    "Password requires 8 characters with at least 1 capital letter, special character and number");
            passValid.addValidation(inputLayout2, regexPassword,
                    "Password requires 8 characters with at least 1 capital letter, special character and number");

            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context)
                    .setTitle(R.string.prompt_change_password)
                    .setView(viewInflated)
                    .setMessage(R.string.prompt_old_new_pass)
                    .setPositiveButton("Submit", (dialogInterface, i) -> {})
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                    .create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view1 -> {
                if(passValid.validate()) {
                    FirebaseUser user = mAuth.getCurrentUser();

                    assert user != null;
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),
                            input.getText().toString());

                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            user.updatePassword(input2.getText().toString()).addOnCompleteListener(task12 -> {
                                if(!task12.isSuccessful()){
                                    Snackbar snackbar_fail = Snackbar
                                            .make(binding.getRoot(),
                                                    "Something went wrong. Please try again later!", Snackbar.LENGTH_LONG);
                                    alertDialog.dismiss();
                                    snackbar_fail.show();
                                }else {
                                    Snackbar snackbar_su = Snackbar
                                            .make(binding.getRoot(), "Password Successfully modified!", Snackbar.LENGTH_LONG);
                                    alertDialog.dismiss();
                                    snackbar_su.show();
                                }
                            });
                        }else {
                            Snackbar snackbar_su = Snackbar
                                    .make(binding.getRoot(), "Invalid old password. Please try again!", Snackbar.LENGTH_LONG);
                            alertDialog.dismiss();
                            snackbar_su.show();
                        }
                    });
                }
            });
        });

        return binding.getRoot();
    }


    private void selectPictureDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {

                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST);
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        builder.create().show();
    }

    private void loadProfile() {
        db.collection("users").whereEqualTo("uid", mAuth.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getDocuments().size() > 0) {
                            User user = task.getResult().getDocuments().get(0).toObject(User.class);
                            assert user != null;

                            binding.inputBio.setText(user.getBio());
                            binding.inputIgn.setText(user.getDisplay_name());
                            binding.autoCompleteTextView.setText(user.getGender(), false);
                            binding.autoCompleteTextView1.setText(user.getRank(), false);
                            binding.autoCompleteTextView2.setText(user.getRole(), false);
                            binding.image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    PhotoViewer.build(getContext(), user.getProfile_img_url(), new ImageLoader() {
                                        @Override
                                        public void load(@Nullable String url, @NonNull ImageView imageView,
                                                         int index, @NonNull ProgressBar progressBar) {
                                            progressBar.setVisibility(View.VISIBLE);
                                            Picasso.get().load(url).into(imageView);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }).showPagingIndicator(false)
                                            .show();
                                }
                            });
                            Picasso.get().load(user.getProfile_img_url()).into(binding.image);

                            String[] strings = user.getFeatured_image().toArray(new String[0]);
                            imageAdapter = new ImageAdapter(getContext(), strings);
                            binding.imageGrid.setAdapter(imageAdapter);
                            binding.imageGrid.setExpanded(true);

                            binding.imageGrid.setOnItemClickListener((adapterView, view, i, l) -> {
                                PhotoViewer.build(getContext(), strings, new ImageLoader() {
                                    @Override
                                    public void load(@Nullable String url, @NonNull ImageView imageView,
                                                     int index, @NonNull ProgressBar progressBar) {
                                        progressBar.setVisibility(View.VISIBLE);
                                        Picasso.get().load(url).into(imageView);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }).startAtIndex(i).show();
                            });

                        }
                    }
                });
    }

    private void pickFromCamera() {
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .cameraOnly()
                .start();
    }

    private void pickFromGallery() {
        ImagePicker.with(this)
                .crop()
                .galleryMimeTypes(new String[] {"image/png", "image/jpg", "image/jpeg"})
                .compress(1024)
                .galleryOnly()
                .start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(context, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(context, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            imageuri = data.getData();
            uploadProfileCoverPhoto(imageuri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static int TYPE = 0;

    private ProgressDialog pd;
    private void uploadProfileCoverPhoto(final Uri uri) {

        pd = new ProgressDialog(context);
        pd.setTitle("Uploading Image");
        pd.setMessage("Please wait...");
        pd.show();

        String filepathname = "profile_images/image_" + mAuth.getUid() + "_" + UUID.randomUUID().toString();
        StorageReference storageReference1 = storageReference.getReference().child(filepathname);
        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;

                final Uri downloadUri = uriTask.getResult();
                if (uriTask.isSuccessful()) {
                    if(TYPE == 0) {
                        db.collection("users").whereEqualTo("uid", mAuth.getUid()).get()
                                .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().getDocuments().size() > 0) {

                                                    Map<String, Object> user = new HashMap<>();
                                                    user.put("profile_img_url", downloadUri.toString());

                                                    db.collection("users")
                                                            .document(task.getResult().
                                                                    getDocuments().get(0).getId())
                                                            .update(user)
                                                            .addOnSuccessListener(unused -> {
                                                                Snackbar snackbar = Snackbar
                                                                        .make(binding.getRoot(),
                                                                                "Profile image updated!", Snackbar.LENGTH_LONG);
                                                                snackbar.show();
                                                                loadProfile();
                                                                pd.dismiss();
                                                            });


                                                }
                                            }
                                        }
                                );
                    } else {
                        db.collection("users").whereEqualTo("uid", mAuth.getUid())
                                .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().getDocuments().size() > 0) {
                                            db.collection("users")
                                                    .whereEqualTo("uid", mAuth.getUid())
                                                    .get().addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            if (!task1.getResult().isEmpty()) {
                                                                db.collection("users")
                                                                        .document(task.getResult().getDocuments().get(0).getId())
                                                                        .update("featured_image",
                                                                                FieldValue.arrayUnion(downloadUri.toString())
                                                                        ).addOnSuccessListener(unused -> {
                                                                            Snackbar snackbar = Snackbar
                                                                                    .make(binding.getRoot(),
                                                                                            "Upload success!", Snackbar.LENGTH_LONG);
                                                                            snackbar.show();
                                                                            loadProfile();
                                                                            pd.dismiss();
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                } else {
                    pd.dismiss();
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}