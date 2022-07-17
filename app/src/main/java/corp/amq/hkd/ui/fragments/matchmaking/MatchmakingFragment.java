package corp.amq.hkd.ui.fragments.matchmaking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import corp.amq.hkd.R;
import corp.amq.hkd.data.Filters;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.databinding.FragmentMatchmakingBinding;
import me.thanel.swipeactionview.SwipeActionView;
import me.thanel.swipeactionview.SwipeGestureListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchmakingFragment extends Fragment {

    private FragmentMatchmakingBinding binding;
    private FirebaseAuth mAuth;
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
        loaded = false;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMatchmakingBinding.inflate(inflater, container, false);

        if(!loaded) {
            loadMatches();
        }

        return binding.getRoot();
    }

    private List<String> matches;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(loaded) {
            changeMatch();
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


        binding.swiperefresh.setOnRefreshListener(this::loadMatches);

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

                            // Check if theres a match from your right swipe
                            db.collection("matches")
                                    .whereEqualTo("uid_1", matches.get(0))
                                    .whereEqualTo("uid_2", mAuth.getUid())
                                    .whereEqualTo("matched", true).get()
                                    .addOnCompleteListener(task1 -> {

                                        if (task1.isSuccessful()) {
                                            if(!task1.getResult().isEmpty()) {

                                                // Looks like theres a match
                                                DatabaseReference msgThreadNew = firebaseDatabase.
                                                        getReference("main-data").child("messageThread")
                                                        .push();

                                                assert mAuth.getUid() != null;
                                                DatabaseReference user1Ref = firebaseDatabase.
                                                        getReference("main-data").child("users")
                                                        .child(mAuth.getUid())
                                                        .child("activeThreads");


                                                user1Ref.get().addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {

                                                        List<String> stringArrayList = new ArrayList<>();
                                                        for (DataSnapshot ds: task2.getResult().getChildren()) {

                                                            assert ds.getValue() != null;
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

                                                            assert ds.getValue() != null;
                                                            stringArrayList.add(ds.getValue().toString());

                                                        }

                                                        stringArrayList.add(msgThreadNew.getKey());
                                                        user2Ref.setValue(stringArrayList);
                                                    }
                                                });


                                                assert msgThreadNew.getKey() != null;
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
                                                                new AlertDialog.Builder(context)
                                                                        .setTitle("It's a match!")
                                                                        .setMessage("Check the messages tab to start messaging!")
                                                                        .setPositiveButton(android.R.string.ok, null)
                                                                        .show();
                                                            }
                                                        }
                                                );
                                            }
                                        }

                                        matches.remove(0);
                                        changeMatch();

                                    });
                        }
                    });
        }
    }


    private void loadMatches() {
        binding.swiperefresh.setRefreshing(true);
        CollectionReference userRef = db.collection("users");
        CollectionReference matchesRef = db.collection("matches");

        assert getActivity() != null;
        SharedPreferences sharedPreferences = getActivity()
                .getPreferences(Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString("filters", "");
        Filters filter = gson.fromJson(json, Filters.class);

        Query reference = userRef.whereNotEqualTo("uid", mAuth.getUid());

        if(filter != null) {
            if(filter.filteredRank(context) != null) {
                reference = reference.whereEqualTo("rank", filter.filteredRank(context));
            }

            if(filter.filterGender(context) != null) {
                reference = reference.whereEqualTo("gender", filter.filterGender(context));
            }

            if(filter.filterRoles(context) != null) {
                reference = reference.whereIn("role", filter.filterRoles(context));
            }
        }

        reference.get().addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    User user = document.toObject(User.class);

                                    matchesRef
                                            .whereEqualTo("uid_1", mAuth.getUid())
                                            .whereEqualTo("uid_2", user.getUid())
                                            .get()
                                            .addOnCompleteListener(
                                                    task1 -> {
                                                        if(task1.isSuccessful()) {
                                                            if(task1.getResult().isEmpty()) {
                                                                matches.add(user.getUid());
                                                            }
                                                            changeMatch();
                                                            loaded = true;
                                                        }
                                                    });
                                }
                            }
                            binding.swiperefresh.setRefreshing(false);
                        }
                );
    }

    private void changeMatch() {
        if(matches.size() > 0) {
            binding.textView4.setVisibility(View.GONE);
            binding.swipeView.setVisibility(View.VISIBLE);
            binding.floatingActionButton.setVisibility(View.VISIBLE);
            binding.floatingActionButton2.setVisibility(View.VISIBLE);

            db.collection("users")
                    .whereEqualTo("uid", matches.get(0))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(task.getResult().getDocuments().size() > 0) {
                                User user = task.getResult().getDocuments().get(0).toObject(User.class);

                                assert user != null;
                                binding.ign.setText(user.getDisplay_name());
                                binding.role.setText(user.getRole());
                                binding.gender.setText(user.getGender());
                                binding.ign.setText(user.getDisplay_name());


                                binding.swipeView.setOnClickListener(
                                        view -> {
                                            Log.d("TAG", "onComplete: clicked");
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("profileArg", matches.get(0));
                                            Navigation.findNavController(view)
                                                    .navigate(R.id.action_matchmaking_fragment_to_profile_fragment, bundle);
                                        }
                                );

                                if(!user.getProfile_img_url().isEmpty()) {
                                    Picasso.get().load(user.getProfile_img_url()).into(binding.profileImg);
                                }

                                binding.rank.setText(user.getRank());
                                loaded = true;
                            }
                        }
                    });
        } else {
            binding.textView4.setVisibility(View.VISIBLE);
            binding.swipeView.setVisibility(View.GONE);
            binding.floatingActionButton.setVisibility(View.GONE);
            binding.floatingActionButton2.setVisibility(View.GONE);
        }

        binding.swiperefresh.setRefreshing(false);

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