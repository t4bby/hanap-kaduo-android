package corp.amq.hkd.ui.fragments.filters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;

import corp.amq.hkd.R;
import corp.amq.hkd.data.Filters;
import corp.amq.hkd.databinding.FragmentFilterBinding;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

        assert getActivity() != null;
        SharedPreferences sharedPreferences = getActivity()
                .getPreferences(Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString("filters", "");
        Filters filter = gson.fromJson(json, Filters.class);

        if(filter != null) {

            for (Integer i: filter.roles) {
                switch (i) {
                    case 0:
                        binding.rolesTop.setChecked(true);
                        break;
                    case 1:
                        binding.rolesMiddle.setChecked(true);
                        break;
                    case 2:
                        binding.rolesBot.setChecked(true);
                        break;
                    case 3:
                        binding.rolesJungle.setChecked(true);
                        break;
                    case 4:
                        binding.rolesSupport.setChecked(true);
                        break;
                }
            }

            binding.rankSpinner.setSelection(filter.rank);
            binding.genderSpinner.setSelection(filter.gender);
        }

        binding.savePreferenceBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Filters filters = new Filters();

            filters.gender = binding.genderSpinner.getSelectedItemPosition();
            filters.rank = binding.rankSpinner.getSelectedItemPosition();

            List<Integer> roles = new ArrayList<>();
            if(binding.rolesTop.isChecked()) roles.add(0);
            if(binding.rolesMiddle.isChecked()) roles.add(1);
            if(binding.rolesBot.isChecked()) roles.add(2);
            if(binding.rolesJungle.isChecked()) roles.add(3);
            if(binding.rolesSupport.isChecked()) roles.add(4);

            filters.roles = roles;

            editor.putString("filters", gson.toJson(filters));
            editor.apply();

            Toast.makeText(getContext(), R.string.preferences_save_success, Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(FilterFragment.this).navigate(R.id.action_filter_fragment_to_matchmaking_fragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}