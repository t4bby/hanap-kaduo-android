package corp.amq.hkd.ui.fragments.registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationHolder;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.custom.CustomValidation;
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import corp.amq.hkd.R;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.databinding.FragmentRegistrationBinding;
import corp.amq.hkd.ui.DashboardActivity;
import corp.amq.hkd.ui.LoginActivity;
import corp.amq.hkd.ui.SplashScreenActivity;


public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private Context context;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        context = getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String[] GENDER = getResources().getStringArray(R.array.gender_array);
        String[] RANKS = getResources().getStringArray(R.array.rank_array);
        String[] ROLES = getResources().getStringArray(R.array.role_array);

        ArrayAdapter<String> gender_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, GENDER);
        ArrayAdapter<String> ranks_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, RANKS);
        ArrayAdapter<String> roles_adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, ROLES);
        binding.autoCompleteTextView.setAdapter(gender_adapter);
        binding.autoCompleteTextView1.setAdapter(ranks_adapter);
        binding.autoCompleteTextView2.setAdapter(roles_adapter);

        binding.termsConditionBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://amqcorp.blogspot.com/2021/12/conditions-by-downloading-or-using-app.html"));
            startActivity(intent);
        });

        AwesomeValidation mAwesomeValidation = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        mAwesomeValidation.addValidation(binding.inputEmailLayout, Patterns.EMAIL_ADDRESS, "Please check the input");

        String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";
        mAwesomeValidation.addValidation(binding.inputPasswordLayout, regexPassword, "Password requires 8 characters with at least 1 capital letter, special character and number");

        mAwesomeValidation.addValidation(binding.inputIgnLayout, "^[ A-Za-z0-9_@./#&+-]*$", "Please check the input");

        mAwesomeValidation.addValidation(binding.gender, "^[ A-Za-z0-9_@./#&+-]*$", "Please check the input");
        mAwesomeValidation.addValidation(binding.role, "[a-zA-Z\\s]+", "Please check the input");
        mAwesomeValidation.addValidation(binding.rank, "[a-zA-Z\\s]+", "Please check the input");


        binding.registerBtn.setOnClickListener(view -> {
            if(mAwesomeValidation.validate()) {
                if(binding.checkBox1.isChecked()) {
                    progressDialog = ProgressDialog.show(context, "Registering","Please Wait...", true);
                    mAuth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(),
                            binding.inputPassword.getText().toString()).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("display_name", binding.inputIgn.getText().toString());
                                    user.put("gender", binding.gender.getEditText().getText().toString());
                                    user.put("rank", binding.rank.getEditText().getText().toString());
                                    user.put("role",binding.role.getEditText().getText().toString());
                                    user.put("profile_img_url",
                                            "https://firebasestorage.googleapis.com/v0/b/hanapkaduo-bb466.appspot.com/o/unknown.png?alt=media&token=4d8bfcfe-6ee4-49f7-80c4-b7a31b0f3901");
                                    user.put("bio", "New user");

                                    assert firebaseUser != null;
                                    user.put("uid", firebaseUser.getUid());
                                    user.put("featured_image", new ArrayList<String>());

                                    createDocument(firebaseUser.getUid(), user);

                                    Snackbar snackbar = Snackbar
                                            .make(binding.getRoot(),
                                                    "Registration Success! Logging in..", Snackbar.LENGTH_LONG);
                                    snackbar.show();

                                } else {
                                    Snackbar snackbar = Snackbar
                                            .make(binding.getRoot(),
                                                    "Network Error", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }

                            progressDialog.dismiss();
                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Snackbar snackbar = Snackbar.make(binding.getRoot(),
                                            "User already exist", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            });

                } else {
                    Snackbar snackbar = Snackbar
                            .make(binding.getRoot(),
                                    "Please agree to the terms and conditions", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        return binding.getRoot();
    }

    private void createDocument(String uid, Object user) {
        Log.d("TAG", uid + user);
        db.collection("users")
                .add(user)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user1 = mAuth.getCurrentUser();
                        if(user1 != null){
                            Intent intent = new Intent(context, DashboardActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}