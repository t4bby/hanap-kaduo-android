package corp.amq.hkd.ui.fragments.loginselector;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import corp.amq.hkd.R;
import corp.amq.hkd.databinding.FragmentLoginSelectorBinding;
import corp.amq.hkd.databinding.FragmentProfileBinding;

public class LoginSelectorFragment extends Fragment {

    private FragmentLoginSelectorBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginSelectorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginSelectorBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_loginSelectorFragment_to_loginFragment));
        binding.registerSelectorBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_loginSelectorFragment_to_registrationFragment));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}