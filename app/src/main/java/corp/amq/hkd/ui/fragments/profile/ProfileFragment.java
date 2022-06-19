package corp.amq.hkd.ui.fragments.profile;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.sha.photoviewer.PhotoViewer;
import com.squareup.picasso.Picasso;
import corp.amq.hkd.R;
import corp.amq.hkd.databinding.FragmentProfileBinding;
import corp.amq.hkd.ui.fragments.profile.adapters.ImageAdapter;
import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private String[] strings;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        strings = new String[] {"hello", "hello", "hello", "hello"};
        ImageAdapter imageAdapter = new ImageAdapter(getContext(), strings);
        binding.imageGrid.setAdapter(imageAdapter);

        binding.imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PhotoViewer.build(getContext(), strings, (url, imageView, index, progressBar) -> {
                    Picasso.get().load(R.drawable.sample2).into(imageView);
                    progressBar.setVisibility(View.GONE);
                }).show();
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
        inflater.inflate(R.menu.profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}