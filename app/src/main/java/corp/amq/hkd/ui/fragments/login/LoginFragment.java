package corp.amq.hkd.ui.fragments.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import corp.amq.hkd.R;
import corp.amq.hkd.databinding.FragmentLoginBinding;
import corp.amq.hkd.ui.DashboardActivity;

public class LoginFragment extends Fragment {


    private FragmentLoginBinding binding;
    private ProgressDialog progressDialog;
    private Context context;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        context = getContext();
        mAuth = FirebaseAuth.getInstance();

        AwesomeValidation mAwesomeValidation = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        mAwesomeValidation.addValidation(binding.inputEmailLayout, Patterns.EMAIL_ADDRESS, "Please check the email address");

        String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d]).{8,}";
        mAwesomeValidation.addValidation(binding.inputPasswordLayout, regexPassword, "Password requires 8 characters with at least 1 capital letter, special character and number");

        binding.loginBtn.setOnClickListener(view -> {
            if(mAwesomeValidation.validate()) {
                progressDialog = ProgressDialog.show(context, "Logging-in","Please Wait...", true);
                signIn(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString());
            }
        });

        binding.forgotPasswordBtn.setOnClickListener(view -> {
            TextInputLayout textInputLayout = new TextInputLayout(context);
            textInputLayout.setPadding(getResources().getDimensionPixelOffset(R.dimen.dp_19), 0,
                    getResources().getDimensionPixelOffset(R.dimen.dp_19),
                    0);

            EditText input = new EditText(context);
            textInputLayout.setHint("Email");
            textInputLayout.addView(input);

            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Reset Password")
                    .setView(textInputLayout)
                    .setMessage("Please enter your email address")
                    .setPositiveButton("Submit", (dialogInterface, i) ->
                            mAuth.sendPasswordResetEmail(input.getText().toString()).addOnCompleteListener(
                                    task -> {
                                if (task.isSuccessful()) {
                                    dialogInterface.dismiss();
                                    Snackbar snackbar = Snackbar
                                            .make(binding.loginCoordinator, "Please check your email", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar
                                            .make(binding.loginCoordinator, "Email not found", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            })).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                    .create();

            alertDialog.show();
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent mainIntent= new Intent(context, DashboardActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            getActivity().finish();
                        } else {
                            progressDialog.dismiss();
                            Snackbar snackbar = Snackbar
                                    .make(binding.loginCoordinator, "Invalid Email or Password. Please try again", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }
                });
    }
}