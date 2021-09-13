package rmit.ad.rmitrides;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DriverAcceptedService extends IntentService {

    public static final String EXTRA_MESSAGE = "Get ready";
    public static final int NOTIFICATION_ID = 5453;

    public DriverAcceptedService() {
        super("DriverAcceptedService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        showText(EXTRA_MESSAGE);

    }

    private void showText(final String text) {
        //Create a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelId")
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(text)
                .setContentText("We found you a buddy driver")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] {0, 1000})
                .setAutoCancel(true);

        //Create an action
        Intent actionIntent = new Intent(this, MainActivity.class);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(actionPendingIntent);

        //Issue the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
