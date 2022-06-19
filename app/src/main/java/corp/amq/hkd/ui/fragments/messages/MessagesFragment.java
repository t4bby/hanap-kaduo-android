package corp.amq.hkd.ui.fragments.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import corp.amq.hkd.R;
import corp.amq.hkd.data.fixtures.DialogsFixtures;
import corp.amq.hkd.data.model.MessageDialog;
import corp.amq.hkd.databinding.FragmentMessagesBinding;
import corp.amq.hkd.ui.MessageActivity;
import org.jetbrains.annotations.NotNull;

import java.util.Date;


public class MessagesFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener<MessageDialog>, DateFormatter.Formatter {

    private FragmentMessagesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageLoader imageLoader = (imageView, url, payload) -> Picasso.get().load(R.drawable.sample).into(imageView);

        DialogsListAdapter<MessageDialog> dialogsAdapter = new DialogsListAdapter<>(imageLoader);
        dialogsAdapter.setItems(DialogsFixtures.getDialogs());
        dialogsAdapter.setOnDialogClickListener(this);
        dialogsAdapter.setDatesFormatter(this);

        binding.dialogsList.setAdapter(dialogsAdapter);
    }

    @Override
    public void onDialogClick(MessageDialog dialog) {
        assert getContext() != null;
        MessageActivity.open(getContext());
    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, DateFormatter.Template.TIME);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else if (DateFormatter.isCurrentYear(date)) {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }
}