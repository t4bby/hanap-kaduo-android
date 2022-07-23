package corp.amq.hkd.ui.fragments.messages;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    private boolean load = false;

    public void getChats() {
        chats = new ArrayList<>();
        threads = new ArrayList<>();
        dialogsAdapter.clear();
        binding.swiperefresh.setRefreshing(true);

        firebaseDatabase.getReference("main-data")
                .child("users")
                .child(auth.getUid())
                .child("activeThreads")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        binding.swiperefresh.setRefreshing(false);

                        for (DataSnapshot dts: task.getResult().getChildren()) {

                            assert dts.getValue() != null;
                            threads.add((String) dts.getValue());

                            if(!load) {
                                DatabaseReference newMsgThread = firebaseDatabase.getReference("main-data")
                                        .child("unSeenMsgCountData")
                                        .child((String) dts.getValue());

                                ChildEventListener childEventListener = new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                        getChats();
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
                                newMsgThread.addChildEventListener(childEventListener);
                                load = true;
                            }

                        }

                        for (String thread: threads) {
                            firebaseDatabase.getReference("main-data")
                                    .child("messageThreadMetadata")
                                    .child(thread)
                                    .get()
                                    .addOnCompleteListener(task12 -> {
                                                if(task12.isSuccessful()) {

                                                    ArrayList<MessageUser> messageUserArrayList = new ArrayList<>();
                                                    String user1_uid = (String) task12.getResult().child("user1_uid").getValue();
                                                    String user2_uid = (String) task12.getResult().child("user2_uid").getValue();
                                                    String lastchatid = (String) task12.getResult().child("lastChatId").getValue();

                                                    assert user1_uid != null;
                                                    firebaseFirestore.collection("users").
                                                            whereEqualTo("uid", user1_uid)
                                                            .get().addOnCompleteListener(
                                                                    task1 -> {
                                                                        if(task1.isSuccessful()) {
                                                                            if (task1.getResult().getDocuments().size() > 0) {
                                                                                User user1 = task1.getResult().getDocuments().get(0).toObject(User.class);
                                                                                messageUserArrayList.add(new MessageUser(user1_uid, user1));

                                                                                assert user2_uid != null;
                                                                                firebaseFirestore.collection("users").
                                                                                        whereEqualTo("uid", user2_uid)
                                                                                        .get().addOnCompleteListener(
                                                                                                task2 -> {
                                                                                                    if (task2.isSuccessful()) {

                                                                                                        if (task2.getResult().getDocuments().size() > 0) {
                                                                                                            User user2 = task2.getResult().getDocuments().get(0).toObject(User.class);
                                                                                                            messageUserArrayList.add(new MessageUser(user2_uid, user2));


                                                                                                            assert lastchatid != null;
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

                                                                                                                                                MessageDialog messageDialog = null;
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
                                                                                                                                                            messageUserArrayList.get(1).getUser().getDisplay_name(),
                                                                                                                                                            messageUserArrayList.get(1).getUser().getProfile_img_url(),
                                                                                                                                                            messageUserArrayList,
                                                                                                                                                            message,
                                                                                                                                                            unreadCount.intValue());
                                                                                                                                                } else if (auth.getUid().equals(user2_uid)) {
                                                                                                                                                    messageDialog = new MessageDialog(
                                                                                                                                                            thread,
                                                                                                                                                            messageUserArrayList.get(0).getUser().getDisplay_name(),
                                                                                                                                                            messageUserArrayList.get(0).getUser().getProfile_img_url(),
                                                                                                                                                            messageUserArrayList,
                                                                                                                                                            message,
                                                                                                                                                            unreadCount.intValue());
                                                                                                                                                }

                                                                                                                                                chats.add(messageDialog);
                                                                                                                                                dialogsAdapter.setItems(chats);
                                                                                                                                                binding.textView5.setVisibility(View.GONE);
                                                                                                                                                binding.dialogsList.setVisibility(View.VISIBLE);
                                                                                                                                            }
                                                                                                                                        });


                                                                                                                            }

                                                                                                                        }
                                                                                                                    });
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                        );
                                                                            }
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

    private DialogsListAdapter<MessageDialog> dialogsAdapter;
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageLoader imageLoader = (imageView, url, payload) -> {

            assert url != null;
            if(!url.isEmpty()) {
                Picasso.get().load(url).into(imageView);
            }
        };
        dialogsAdapter = new DialogsListAdapter<>(imageLoader);

        dialogsAdapter.setOnDialogClickListener(this);
        dialogsAdapter.setDatesFormatter(this);

        dialogsAdapter.setOnDialogLongClickListener(dialog -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Safety Toolkit")
                    .setItems(R.array.message_menu,
                            (dialog1, which) -> {
                                if(which == 0) {
                                    new AlertDialog.Builder(getActivity())
                                            .setMessage("Are you sure?")
                                            .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {

                                                // Remove thread
                                                DatabaseReference metaData = firebaseDatabase.
                                                        getReference("main-data")
                                                        .child("messageThreadMetadata")
                                                        .child(dialog.getId());


                                                metaData.get().addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()) {

                                                        String uid1 = (String) task.getResult().child("user1_uid").getValue();
                                                        String uid2 = (String) task.getResult().child("user2_uid").getValue();

                                                        assert uid1 != null;
                                                        DatabaseReference user1Ref = firebaseDatabase.
                                                                getReference("main-data").child("users")
                                                                .child(uid1)
                                                                .child("activeThreads");

                                                        user1Ref.get().addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {

                                                                List<String> stringArrayList = new ArrayList<>();
                                                                for (DataSnapshot ds: task2.getResult().getChildren()) {
                                                                    assert ds.getValue() != null;
                                                                    String v = ds.getValue().toString();
                                                                    if(!v.equals(dialog.getId())) {
                                                                        stringArrayList.add(v);
                                                                    }
                                                                }
                                                                user1Ref.setValue(stringArrayList);
                                                            }
                                                        });


                                                        assert uid2 != null;
                                                        DatabaseReference user2Ref = firebaseDatabase.
                                                                getReference("main-data").child("users")
                                                                .child(uid2)
                                                                .child("activeThreads");

                                                        user2Ref.get().addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {

                                                                List<String> stringArrayList = new ArrayList<>();
                                                                for (DataSnapshot ds: task2.getResult().getChildren()) {
                                                                    assert ds.getValue() != null;
                                                                    String v = ds.getValue().toString();
                                                                    if(!v.equals(dialog.getId())) {
                                                                        stringArrayList.add(v);
                                                                    }
                                                                }
                                                                user2Ref.setValue(stringArrayList);
                                                            }
                                                        });
                                                    }
                                                });


                                            })
                                            .setNegativeButton(android.R.string.no, null)
                                            .create()
                                            .show();

                                }
                            });
            builder.create();
            builder.show();
        });

        binding.dialogsList.setAdapter(dialogsAdapter);
        binding.swiperefresh.setOnRefreshListener(this::getChats);
    }

    @Override
    public void onDialogClick(MessageDialog dialog) {
        assert getContext() != null;

        Bundle bundle = new Bundle();
        bundle.putSerializable("messageArg", dialog.getId());
        bundle.putSerializable("conversationNameArg", dialog.getDialogName());

        assert getView() != null;
        Navigation.findNavController(getView())
                .navigate(R.id.action_messages_fragment_to_message_fragment, bundle);
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