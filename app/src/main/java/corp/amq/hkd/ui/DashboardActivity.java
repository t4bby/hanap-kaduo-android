package corp.amq.hkd.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import corp.amq.hkd.R;
import corp.amq.hkd.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

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
                    if(navDestination.getId() == R.id.message_fragment) {
                        binding.navView.setVisibility(View.GONE);
                    } else if(navDestination.getId() == R.id.profile_settings_fragment) {
                        binding.navView.setVisibility(View.GONE);
                    }
                    else {
                        binding.navView.setVisibility(View.VISIBLE);
                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}