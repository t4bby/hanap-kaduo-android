package corp.amq.hkd.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import corp.amq.hkd.R;
import corp.amq.hkd.ui.DashboardActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_CHANNEL = "hanapkaduo_channel";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();

    @Override
    public void onNewToken(@NonNull String token) {
        firebaseDatabase.collection("users")
                .whereEqualTo("uid", firebaseAuth.getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            firebaseDatabase
                                    .collection("users")
                                    .document(task.getResult().getDocuments().get(0).getId())
                                    .update("fcm_tokens", FieldValue.arrayUnion(token));
                        }
                    }
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        if (message.getNotification() != null) {
            Log.d("TAG", "onMessageReceived: " + message.getData());
            showNotification(
                    message.getNotification().getTitle(),
                    message.getNotification().getBody()
            );
        }
    }

    public void showNotification(String title, String message) {

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder
                = new NotificationCompat
                .Builder(getApplicationContext(), NOTIFICATION_CHANNEL)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_message_24);

        NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());
    }
}
