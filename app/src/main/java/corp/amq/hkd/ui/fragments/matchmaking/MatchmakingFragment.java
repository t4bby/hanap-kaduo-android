package corp.amq.hkd.ui.fragments.matchmaking;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import corp.amq.hkd.R;
import corp.amq.hkd.databinding.FragmentMatchmakingBinding;
import me.thanel.swipeactionview.SwipeActionView;
import me.thanel.swipeactionview.SwipeGestureListener;
import org.jetbrains.annotations.NotNull;

public class MatchmakingFragment extends Fragment {

    private FragmentMatchmakingBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMatchmakingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        binding.swipeView.setSwipeGestureListener(new SwipeGestureListener() {
            @Override
            public void onSwipeRightComplete(@NotNull SwipeActionView swipeActionView) {

            }

            @Override
            public void onSwipeLeftComplete(@NotNull SwipeActionView swipeActionView) {

            }

            @Override
            public boolean onSwipedLeft(@NotNull SwipeActionView swipeActionView) {
                Toast.makeText(getContext(), "Swiped Left", Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onSwipedRight(@NotNull SwipeActionView swipeActionView) {
                Toast.makeText(getContext(), "Swiped Right", Toast.LENGTH_LONG).show();
                return true;
            }
        });

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
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }
}