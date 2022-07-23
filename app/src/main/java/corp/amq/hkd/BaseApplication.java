package corp.amq.hkd;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import corp.amq.hkd.services.MyFirebaseMessagingService;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = getString(R.string.channel_name_background);
        NotificationChannel mChannel = new NotificationChannel(MyFirebaseMessagingService.NOTIFICATION_CHANNEL,
                name, NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.setDescription(getString(R.string.channel_description_background));
        mChannel.enableLights(true);

        mNotificationManager.createNotificationChannel(mChannel);
    }
}
