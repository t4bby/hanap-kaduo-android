package corp.amq.hkd.ui.fragments.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sha.photoviewer.PhotoViewer;
import com.squareup.picasso.Picasso;
import corp.amq.hkd.R;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.databinding.FragmentProfileBinding;
import corp.amq.hkd.ui.DashboardActivity;
import corp.amq.hkd.ui.LoginActivity;
import corp.amq.hkd.ui.fragments.profile.adapters.ImageAdapter;
import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private Context context;
    private FirebaseFirestore db;
    private String profileUid = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        context = getContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if(getArguments() != null) {
            profileUid = ProfileFragmentArgs.fromBundle(getArguments()).getProfileArg();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = ProgressDialog.show(context, "Loading","Please Wait...", true);
        progressDialog.setCancelable(false);

        if(profileUid == null) {
            profileUid = mAuth.getUid();
        }

        db.collection("users").whereEqualTo("uid", profileUid).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if(task.getResult().getDocuments().size() > 0) {
                            User user = task.getResult().getDocuments().get(0).toObject(User.class);

                            assert user != null;
                            if(!user.getProfile_img_url().isEmpty()) {

                                binding.profileImage.setOnClickListener(view1 ->
                                        PhotoViewer.build(getContext(), user.getProfile_img_url(),
                                                (url, imageView, index, progressBar) -> {
                                                    Picasso.get().load(url).into(imageView);
                                                    progressBar.setVisibility(View.GONE);
                                                }).showPagingIndicator(false).show());

                                Picasso.get().load(user.getProfile_img_url()).into(binding.profileImage);
                            }

                            binding.bioText.setText(user.getBio());
                            binding.gender.setText(user.getGender());
                            binding.role.setText(user.getRole());
                            binding.ign.setText(user.getDisplay_name());
                            binding.rank.setText(user.getRank());
                        }

                    } else {
                        Snackbar snackbar = Snackbar
                                .make(binding.getRoot(),
                                        "Network Error", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                    progressDialog.dismiss();

                });

        db.collection("users").whereEqualTo("uid", profileUid).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(task.getResult().getDocuments().size() > 0) {
                    User user = task.getResult().getDocuments().get(0).toObject(User.class);

                    assert user != null;
                    if(user.getFeatured_image() != null) {
                        String[] strings = user.getFeatured_image().toArray(new String[0]);
                        ImageAdapter imageAdapter = new ImageAdapter(getContext(), strings);

                        binding.imageGrid.setAdapter(imageAdapter);
                        binding.imageGrid.setExpanded(true);
                        binding.imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                PhotoViewer.build(getContext(), strings, (url, imageView, index, progressBar) -> {
                                    Picasso.get().load(url).into(imageView);
                                    progressBar.setVisibility(View.GONE);
                                }).startAtIndex(i)
                                        .show();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_profile_settings:
                Navigation.findNavController(getView())
                        .navigate(R.id.action_profile_fragment_to_profile_settings_fragment);
                return true;

            case R.id.menu_profile_logout:
                mAuth.signOut();
                Intent mainIntent= new Intent(context, LoginActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                getActivity().finish();

                return true;
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        if(profileUid.equals(mAuth.getUid())){
            inflater.inflate(R.menu.profile, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
}