package corp.amq.hkd.ui.fragments.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import corp.amq.hkd.R;
import corp.amq.hkd.data.firebase.User;
import corp.amq.hkd.data.model.Message;
import corp.amq.hkd.data.model.MessageDialog;
import corp.amq.hkd.data.model.MessageUser;
import corp.amq.hkd.databinding.FragmentMessagesBinding;
import corp.amq.hkd.ui.MessageActivity;


public class MessagesFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener<MessageDialog>, DateFormatter.Formatter {

    private FragmentMessagesBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private List<String> threads;

    ArrayList<MessageDialog> chats;

    public void getChats() {
        chats = new ArrayList<>();
        threads = new ArrayList<>();

        firebaseDatabase.getReference("main-data")
                .child("users")
                .child(auth.getUid()).child("activeThreads").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for (DataSnapshot dts: task.getResult().getChildren()) {
                            threads.add((String) dts.getValue());
                        }

                        for (String thread: threads) {
                            firebaseDatabase.getReference("main-data")
                                    .child("messageThreadMetadata")
                                    .child(thread).get().addOnCompleteListener(task12 -> {
                                        if(task12.isSuccessful()) {

                                            ArrayList<MessageUser> messageUserArrayList = new ArrayList<>();
                                            String user1_uid = (String) task12.getResult().child("user1_uid").getValue();
                                            String user2_uid = (String) task12.getResult().child("user2_uid").getValue();
                                            String lastchatid = (String) task12.getResult().child("lastChatId").getValue();

                                            assert user1_uid != null;
                                            firebaseFirestore.collection("users").
                                                    document(user1_uid)
                                                    .get().addOnCompleteListener(
                                                            task1 -> {
                                                                if(task1.isSuccessful()) {
                                                                    User user1 = task1.getResult().toObject(User.class);
                                                                    messageUserArrayList.add(new MessageUser(user1_uid, user1));

                                                                    assert user2_uid != null;
                                                                    firebaseFirestore.collection("users").
                                                                            document(user2_uid)
                                                                            .get().addOnCompleteListener(
                                                                                    task2 -> {
                                                                                        if (task1.isSuccessful()) {
                                                                                            User user2 = task2.getResult().toObject(User.class);
                                                                                            messageUserArrayList.add(new MessageUser(user2_uid, user2));


                                                                                            firebaseDatabase.getReference("main-data")
                                                                                                    .child("messageThread").child(thread)
                                                                                                    .child(lastchatid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<DataSnapshot> task3) {
                                                                                                            if(task3.isSuccessful()) {
                                                                                                                firebaseDatabase
                                                                                                                        .getReference("main-data")
                                                                                                                        .child("unSeenMsgCountData")
                                                                                                                        .child(thread).get().addOnCompleteListener(task4 -> {
                                                                                                                            if (task4.isSuccessful()) {

                                                                                                                                Message message =  new Message(lastchatid,
                                                                                                                                        messageUserArrayList.get(0),
                                                                                                                                        "",
                                                                                                                                        new Date().toString());

                                                                                                                                if(task3.getResult().hasChild("chatMessage")) {
                                                                                                                                    if(task3.getResult().child("uid").getValue() == user1_uid) {
                                                                                                                                        message = new Message(lastchatid, messageUserArrayList.get(0),
                                                                                                                                                (String) task3.getResult().child("chatMessage").getValue(),
                                                                                                                                                (String) task3.getResult().child("chatTimestamp").getValue());
                                                                                                                                    } else {
                                                                                                                                        message = new Message(lastchatid,
                                                                                                                                                messageUserArrayList.get(1),
                                                                                                                                                (String) task3.getResult().child("chatMessage").getValue(),
                                                                                                                                                (String) task3.getResult().child("chatTimestamp").getValue());
                                                                                                                                    }
                                                                                                                                }

                                                                                                                                MessageDialog messageDialog;
                                                                                                                                String unreadUid = (String) task4.getResult().child("userId").getValue();
                                                                                                                                Long unreadCount = null;

                                                                                                                                if (unreadUid != null) {
                                                                                                                                    if(!unreadUid.equals(auth.getUid())) {
                                                                                                                                        unreadCount = (Long) task4.getResult().child("unSeenMsgCount").getValue();
                                                                                                                                    }
                                                                                                                                }

                                                                                                                                if(unreadCount == null) {
                                                                                                                                    unreadCount = 0L;
                                                                                                                                }

                                                                                                                                if (auth.getUid().equals(user1_uid)) {
                                                                                                                                    messageDialog = new MessageDialog(
                                                                                                                                            thread,
                                                                                                                                            messageUserArrayList.get(0).getUser().getDisplay_name(),
                                                                                                                                            messageUserArrayList.get(0).getUser().getProfile_img_url(),
                                                                                                                                            messageUserArrayList,
                                                                                                                                            message,
                                                                                                                                            unreadCount.intValue());
                                                                                                                                } else {
                                                                                                                                    messageDialog = new MessageDialog(
                                                                                                                                            thread,
                                                                                                                                            messageUserArrayList.get(1).getUser().getDisplay_name(),
                                                                                                                                            messageUserArrayList.get(1).getUser().getProfile_img_url(),
                                                                                                                                            messageUserArrayList,
                                                                                                                                            message,
                                                                                                                                            unreadCount.intValue());
                                                                                                                                }
                                                                                                                                chats.add(messageDialog);

                                                                                                                                dialogsAdapter.setItems(chats);
                                                                                                                            }
                                                                                                                        });
                                                                                                            }

                                                                                                        }
                                                                                                    });



                                                                                        }
                                                                                    }
                                                                            );
                                                                }
                                                            }
                                                    );




                                        }
                                    }
                                    );
                        }
                    }
                });

    }


    @Override
    public void onResume() {
        super.onResume();
        getChats();
    }

    DialogsListAdapter<MessageDialog> dialogsAdapter;
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageLoader imageLoader = (imageView, url, payload) -> Picasso.get().load(R.drawable.sample).into(imageView);
        dialogsAdapter = new DialogsListAdapter<>(imageLoader);

        dialogsAdapter.setOnDialogClickListener(this);
        dialogsAdapter.setDatesFormatter(this);

        binding.dialogsList.setAdapter(dialogsAdapter);

//        DatabaseReference newMsgThread = firebaseDatabase.getReference("main-data")
//                .child("users")
//                .child(auth.getUid())
//                .child("activeThreads");
//
//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                getChats();
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        };
//
//        newMsgThread.addChildEventListener(childEventListener);
    }

    @Override
    public void onDialogClick(MessageDialog dialog) {
        assert getContext() != null;
        MessageActivity.open(getContext(), dialog.getId());
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