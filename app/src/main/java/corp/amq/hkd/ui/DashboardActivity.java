package corp.amq.hkd.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import corp.amq.hkd.R;
import corp.amq.hkd.databinding.ActivityDashboardBinding;
import corp.amq.hkd.ui.fragments.messages.MessageFragment;

public class DashboardActivity extends AppCompatActivity implements MessageFragment.MessageToolbarListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Setup AppBar
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.matchmaking_fragment,
                R.id.messages_fragment).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener(
                (navController1, navDestination, bundle) -> {

                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) binding.toolbar.getLayoutParams();
                    CoordinatorLayout.LayoutParams params1 =
                            (CoordinatorLayout.LayoutParams) binding.navView.getLayoutParams();

                    if(navDestination.getId() == R.id.message_fragment) {
                        binding.navView.setVisibility(View.GONE);
                        params.setScrollFlags(0);
                    } else if(navDestination.getId() == R.id.profile_settings_fragment) {
                        binding.navView.setVisibility(View.GONE);
                        params.setScrollFlags(0);
                    }
                    else if(navDestination.getId() == R.id.messages_fragment) {
                        binding.navView.setVisibility(View.VISIBLE);
                        params.setScrollFlags(0);
                        ((BottomNavigationBehavior) params1.getBehavior()).setEnabled(false);
                    }
                    else if(navDestination.getId() == R.id.matchmaking_fragment) {
                        binding.navView.setVisibility(View.VISIBLE);
                        params.setScrollFlags(0);
                        ((BottomNavigationBehavior) params1.getBehavior()).setEnabled(false);
                    }
                    else {
                        binding.navView.setVisibility(View.VISIBLE);
                        ((BottomNavigationBehavior) params1.getBehavior()).setEnabled(true);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

                    }
                });

        FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase.collection("users")
                .whereEqualTo("uid", firebaseAuth.getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task1 -> {
                                        if (!task1.isSuccessful()) {
                                            return;
                                        }
                                        String token = task1.getResult();
                                        firebaseDatabase
                                                .collection("users")
                                                .document(task.getResult().getDocuments().get(0).getId())
                                                .update("fcm_tokens", FieldValue.arrayUnion(token));
                                    });


                        }
                    }
                });


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        return;
                    }
                    String token = task1.getResult();
                    Log.d("TAG", "onCreate: " + token);
                });

    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onChangeToolbarTitle(String title) {
        binding.toolbar.setTitle(title);
    }
}