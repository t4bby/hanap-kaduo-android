package corp.amq.hkd.ui.fragments.filters;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;
import corp.amq.hkd.R;
import corp.amq.hkd.databinding.FragmentFilterBinding;
import org.jetbrains.annotations.NotNull;

public class FilterFragment extends Fragment {

    private FragmentFilterBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.savePreferenceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: save filters to local preferences for future use
                Toast.makeText(getContext(), R.string.preferences_save_success, Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(FilterFragment.this).navigate(R.id.action_filter_fragment_to_matchmaking_fragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}