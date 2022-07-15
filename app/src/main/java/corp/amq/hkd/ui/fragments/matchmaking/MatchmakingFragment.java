package corp.amq.hkd.ui.fragments.matchmaking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import corp.amq.hkd.R;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.databinding.FragmentMatchmakingBinding;
import me.thanel.swipeactionview.SwipeActionView;
import me.thanel.swipeactionview.SwipeGestureListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchmakingFragment extends Fragment {

    private FragmentMatchmakingBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseDatabase firebaseDatabase;
    private boolean loaded;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        context = getContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        matches = new ArrayList<>();
        progressDialog = ProgressDialog.show(context, "Loading Matches","Please Wait...", true);
        loaded = false;
        loadMatches();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMatchmakingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private List<String> matches;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(loaded) {
            changeMatch(0);
        }

        binding.swipeView.setSwipeGestureListener(new SwipeGestureListener() {
            @Override
            public void onSwipeRightComplete(@NotNull SwipeActionView swipeActionView) {

            }

            @Override
            public void onSwipeLeftComplete(@NotNull SwipeActionView swipeActionView) {

            }

            @Override
            public boolean onSwipedLeft(@NotNull SwipeActionView swipeActionView) {
                swipe(false);
                return true;
            }

            @Override
            public boolean onSwipedRight(@NotNull SwipeActionView swipeActionView) {
                swipe(true);
                return true;
            }
        });


        binding.floatingActionButton.setOnClickListener(v -> swipe(false));

        binding.floatingActionButton2.setOnClickListener(v -> swipe(true));

    }

    private void swipe(boolean b) {

        if(matches.size() > 0) {
            Map<String, Object> match = new HashMap<>();
            match.put("uid_1", mAuth.getUid());
            match.put("uid_2", matches.get(0));
            match.put("matched", b);

            db.collection("matches")
                    .add(match)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            db.collection("matches")
                                    .whereEqualTo("uid_1", matches.get(0))
                                    .whereEqualTo("uid_2", mAuth.getUid())
                                    .whereEqualTo("matched", true).get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {

                                            DatabaseReference msgThreadNew = firebaseDatabase.
                                                    getReference("main-data").child("messageThread")
                                                    .push();

                                            DatabaseReference user1Ref = firebaseDatabase.
                                                    getReference("main-data").child("users")
                                                    .child(mAuth.getUid())
                                                    .child("activeThreads");


                                            user1Ref.get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {

                                                    List<String> stringArrayList = new ArrayList<>();
                                                    for (DataSnapshot ds: task2.getResult().getChildren()) {
                                                        stringArrayList.add(ds.getValue().toString());
                                                    }

                                                    stringArrayList.add(msgThreadNew.getKey());
                                                    user1Ref.setValue(stringArrayList);


                                                }
                                            });


                                            DatabaseReference user2Ref = firebaseDatabase.
                                                    getReference("main-data").child("users")
                                                    .child(matches.get(0))
                                                    .child("activeThreads");

                                            user2Ref.get().addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {

                                                    List<String> stringArrayList = new ArrayList<>();
                                                    for (DataSnapshot ds: task2.getResult().getChildren()) {
                                                        stringArrayList.add(ds.getValue().toString());
                                                    }

                                                    stringArrayList.add(msgThreadNew.getKey());
                                                    user2Ref.setValue(stringArrayList);
                                                }
                                            });


                                            DatabaseReference userMetadataRef = firebaseDatabase.
                                                    getReference("main-data").child("messageThreadMetadata")
                                                    .child(msgThreadNew.getKey());

                                            Map<String, String> hashMap = new HashMap<>();
                                            hashMap.put("lastChatId", "");
                                            hashMap.put("user1_uid", mAuth.getUid());
                                            hashMap.put("user2_uid", matches.get(0));

                                            userMetadataRef.setValue(hashMap).addOnCompleteListener(
                                                    task22 -> {
                                                        if (task22.isSuccessful()) {
                                                            Snackbar snackbar = Snackbar
                                                                    .make(binding.getRoot(),
                                                                            "It's a match! Check messages to start conversation",
                                                                            Snackbar.LENGTH_LONG);
                                                            snackbar.show();
                                                        }
                                                    }
                                            );
                                        }

                                        matches.remove(0);
                                        changeMatch(0);

                                    });
                        }
                    });
        }
    }


    private void loadMatches() {
        // TODO: load filters
        db.collection("users")
                .whereEqualTo("rank", "Iron")
                .whereEqualTo("gender", "Male")
                .limit(15)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.getId().equals(mAuth.getUid())) {
                                matches.add(document.getId());
                            }
                        }

                        for (int i = 0; i < matches.size(); i++) {

                            int finalI = i;
                            db.collection("matches")
                                    .whereEqualTo("uid_1", mAuth.getUid())
                                    .whereEqualTo("uid_2", matches.get(i))
                                    .whereIn("matched", Arrays.asList(true, false))
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().getDocuments().size() > 0) {
                                                    matches.remove(finalI);
                                                }
                                                changeMatch(0);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void changeMatch(int c) {
        if(matches.size() > 0) {
            db.collection("users")
                    .document(matches.get(c))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);

                                assert user != null;
                                binding.ign.setText(user.getDisplay_name());
                                binding.role.setText(user.getRole());
                                binding.gender.setText(user.getGender());
                                binding.ign.setText(user.getDisplay_name());


                                binding.swipeView.setOnClickListener(
                                        view -> {
                                            Log.d("TAG", "onComplete: clicked");
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("profileArg", matches.get(c));
                                            Navigation.findNavController(view)
                                                    .navigate(R.id.action_matchmaking_fragment_to_profile_fragment, bundle);
                                        }
                                );

                                if(user.getProfile_img_url().isEmpty()) {
                                    Picasso.get().load(R.drawable.sample).into(binding.profileImg);
                                } else {
                                    Picasso.get().load(user.getProfile_img_url()).into(binding.profileImg);
                                }

                                binding.rank.setText(user.getRank());
                                loaded = true;

                                progressDialog.dismiss();
                            }
                        }
                    });
        } else {
            progressDialog.dismiss();

            new AlertDialog.Builder(context)
                    .setTitle("There is no available people to match")
                    .setMessage("Oops. Looks like you already swiped all the users")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.matchmaking, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        assert getActivity() != null;
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }
}