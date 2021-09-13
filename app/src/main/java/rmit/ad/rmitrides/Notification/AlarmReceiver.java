package rmit.ad.rmitrides.Notification;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import rmit.ad.rmitrides.MainActivity;
import rmit.ad.rmitrides.R;

public class AlarmReceiver extends BroadcastReceiver {
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        addNotification(context);

//        Toast.makeText(context, "Intent Detected."+intent.getAction(), Toast.LENGTH_LONG).show();


        Toast.makeText(context, "wake up from broadcastreceiver alarmreceiver!!!!!", Toast.LENGTH_SHORT).show();
        Vibrator vib=(Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);    //for Vibration
        vib.vibrate(3000);

//        PackageManager pm = context.getPackageManager();
//        Intent launchIntent = pm.getLaunchIntentForPackage("rmit.ad.rmitrides");
////        launchIntent.putExtra("some_data", "value");
//        context.startActivity(launchIntent);
    }

    private void addNotification(Context context) {
        //from doc

//        // Key for the string that's delivered in the action's intent.
//        private static final String KEY_TEXT_REPLY = "key_text_reply";

//        Intent snoozeIntent = new Intent(mContext, MainActivity.class);
////        snoozeIntent.setAction(ACTION_SNOOZE);
////        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
//        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent snoozePendingIntent =
//                PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "myChannelId")
                .setSmallIcon(R.drawable.motorbike_icon)
                .setContentTitle("Offer ride reminder")
//                .setContentText("would you like to share your ride today?")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("would you like to share your ride today?"))
//                .addAction(R.mipmap.ic_launcher, "snooze",
//                        snoozePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);




        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());

//        AlarmManager alarmManager = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),pendingIntent);

    }
}
