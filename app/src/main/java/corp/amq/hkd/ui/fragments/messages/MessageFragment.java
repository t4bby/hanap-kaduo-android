package corp.amq.hkd.ui.fragments.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import corp.amq.hkd.R;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.data.model.Message;
import corp.amq.hkd.data.model.MessageUser;
import corp.amq.hkd.databinding.FragmentMessageBinding;
import corp.amq.hkd.ui.fragments.profile.ProfileFragmentArgs;

public class MessageFragment extends Fragment implements MessageInput.InputListener,
        MessageInput.TypingListener, DateFormatter.Formatter {

    private MessagesListAdapter<Message> messagesAdapter;
    private FragmentMessageBinding binding;
    private String tid = null;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    private String user1_uid;
    private String user2_uid;

    @Override
    public void onStart() {
        super.onStart();
    }

    public void dataChanged(DataSnapshot dataSnapshot) {
        firebaseDatabase.getReference("main-data")
                .child("messageThreadMetadata")
                .child(tid).get().addOnCompleteListener(task12 -> {
                    if(task12.isSuccessful()) {
                        ArrayList<MessageUser> messageUserArrayList = new ArrayList<>();

                        user1_uid = (String) task12.getResult().child("user1_uid").getValue();
                        user2_uid = (String) task12.getResult().child("user2_uid").getValue();

                        assert user1_uid != null;
                        firebaseFirestore.collection("users").
                                whereEqualTo("uid", user1_uid)
                                .get().addOnCompleteListener(
                                        task1 -> {
                                            if(task1.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task1.getResult()) {
                                                    User user1 = document.toObject(User.class);
                                                    messageUserArrayList.add(new MessageUser(user1_uid, user1));

                                                    assert user2_uid != null;
                                                    firebaseFirestore.collection("users").
                                                            whereEqualTo("uid", user2_uid)
                                                            .get().addOnCompleteListener(
                                                                    task2 -> {
                                                                        if (task1.isSuccessful()) {
                                                                            for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                                                                User user2 = document2.toObject(User.class);
                                                                                messageUserArrayList.add(new MessageUser(user2_uid, user2));
                                                                                Message message;
                                                                                if(dataSnapshot.child("uid").getValue().equals(user1_uid)) {
                                                                                    message = new Message(dataSnapshot.getKey(),
                                                                                            messageUserArrayList.get(0),
                                                                                            (String) dataSnapshot.child("chatMessage").getValue(),
                                                                                            (String) dataSnapshot.child("chatTimestamp").getValue()
                                                                                    );
                                                                                } else {
                                                                                    message = new Message(dataSnapshot.getKey(),
                                                                                            messageUserArrayList.get(1),
                                                                                            (String) dataSnapshot.child("chatMessage").getValue(),
                                                                                            (String) dataSnapshot.child("chatTimestamp").getValue());
                                                                                }

                                                                                messagesAdapter.addToStart(message, true);
                                                                            }
                                                                        }});
                                                }
                                            }
                                        });
                    }
                });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(getLayoutInflater());
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if(getArguments() != null) {
            tid = MessageFragmentArgs.fromBundle(getArguments()).getMessageArg();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DatabaseReference unSeenMsgCountDataRef = firebaseDatabase.getReference("main-data")
                .child("unSeenMsgCountData")
                .child(tid);

        unSeenMsgCountDataRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = (String) task.getResult().child("userId").getValue();
                if(uid != null) {
                    if(!uid.equals(auth.getUid())) {
                        unSeenMsgCountDataRef.child("unSeenMsgCount").setValue(0);
                    }
                }
            }
        });

        ImageLoader imageLoader = (imageView, url, payload) ->  {
            assert url != null;
            if(!url.isEmpty()) {
                Picasso.get().load(url).into(imageView);
            }
        };

        messagesAdapter = new MessagesListAdapter<>(auth.getUid(), imageLoader);
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar, new MessagesListAdapter.OnMessageViewClickListener<Message>() {
            @Override
            public void onMessageViewClick(View view, Message message) {

                Bundle bundle = new Bundle();

                if(user1_uid.equals(auth.getUid())) {
                    bundle.putSerializable("profileArg", user2_uid);
                } else {
                    bundle.putSerializable("profileArg", user1_uid);
                }

                Navigation.findNavController(view)
                        .navigate(R.id.action_message_fragment_to_profile_fragment, bundle);

            }
        });

        messagesAdapter.setDateHeadersFormatter(this);

        binding.input.setInputListener(this);
        binding.input.setTypingListener(this);

        binding.messagesList.setAdapter(messagesAdapter);

        DatabaseReference msgThreadRef = firebaseDatabase.getReference("main-data")
                .child("messageThread")
                .child(tid);

        ChildEventListener msgListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                dataChanged(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        msgThreadRef.addChildEventListener(msgListener);
    }

    @Override
    public boolean onSubmit(CharSequence input) {

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", auth.getUid());
        hashMap.put("chatMessage", input.toString());
        hashMap.put("chatTimestamp", new Date().toString());

        DatabaseReference dbref = firebaseDatabase.getReference("main-data")
                .child("messageThread")
                .child(tid)
                .push();

        DatabaseReference unSeenMsgCountDataRef = firebaseDatabase.getReference("main-data")
                .child("unSeenMsgCountData")
                .child(tid);

        dbref.setValue(hashMap)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        firebaseDatabase.getReference("main-data")
                                .child("messageThreadMetadata")
                                .child(tid)
                                .child("lastChatId")
                                .setValue(dbref.getKey());

                        firebaseDatabase.getReference("main-data")
                                .child("messageThreadMetadata")
                                .child(tid).get().addOnCompleteListener(task12 -> {
                                    if(task12.isSuccessful()) {

                                        String user1_uid = (String) task12.getResult().child("user1_uid").getValue();
                                        String user2_uid = (String) task12.getResult().child("user2_uid").getValue();

                                        if (user1_uid.equals(auth.getUid())) {
                                            unSeenMsgCountDataRef.child("userId").setValue(user1_uid);
                                        } else {
                                            unSeenMsgCountDataRef.child("userId").setValue(user2_uid);
                                        }

                                        unSeenMsgCountDataRef.child("unSeenMsgCount").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    Long count = (Long) task.getResult().getValue();

                                                    if (count == null) {
                                                        count = 1l;
                                                    } else {
                                                        count += 1;
                                                    }

                                                    unSeenMsgCountDataRef.child("unSeenMsgCount").setValue(count);
                                                }
                                            }
                                        });
                                    }});


                    }
                });

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
