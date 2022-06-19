package corp.amq.hkd.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import corp.amq.hkd.R;
import corp.amq.hkd.data.fixtures.MessagesFixtures;
import corp.amq.hkd.data.model.Message;
import corp.amq.hkd.databinding.ActivityMessageBinding;

import java.util.Date;


public class MessageActivity extends AppCompatActivity implements MessageInput.InputListener,
        MessageInput.TypingListener,
        DateFormatter.Formatter{
    private MessagesListAdapter<Message> messagesAdapter;
    private ActivityMessageBinding binding;

    private Context context;

    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
    }

    public static void open(Context context) {
        context.startActivity(new Intent(context, MessageActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Message");

        context = this;

        String senderId = "0";
        ImageLoader imageLoader = (imageView, url, payload) -> Picasso.get().load(R.drawable.sample).into(imageView);

        messagesAdapter = new MessagesListAdapter<>(senderId, imageLoader);
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar, new MessagesListAdapter.OnMessageViewClickListener<Message>() {
            @Override
            public void onMessageViewClick(View view, Message message) {
                Toast.makeText(context, "Avatar Clicked", Toast.LENGTH_LONG).show();
            }
        });

        messagesAdapter.setDateHeadersFormatter(this);

        binding.input.setInputListener(this);
        binding.input.setTypingListener(this);

        binding.messagesList.setAdapter(messagesAdapter);
    }


    @Override
    public boolean onSubmit(CharSequence input) {
        messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);
        return true;
    }

    @Override
    public void onStartTyping() {

    }

    @Override
    public void onStopTyping() {

    }


    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return getString(R.string.date_header_today);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

}