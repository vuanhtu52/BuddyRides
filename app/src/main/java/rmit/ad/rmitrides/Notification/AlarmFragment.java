package rmit.ad.rmitrides.Notification;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import rmit.ad.rmitrides.PassengerMapFragment;
import rmit.ad.rmitrides.R;
import rmit.ad.rmitrides.SettingsFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmFragment extends Fragment {


    final static int RQS_1 = 1;
    TimePicker timePicker;
    NotificationManagerCompat notificationManager;
    private ImageButton backButton;

    public AlarmFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);




        timePicker = (TimePicker) view.findViewById(R.id.alarmTimePicker);
        backButton = view.findViewById(R.id.back_button);

        //Go back to SettingsFragment when user presses back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                ft.replace(R.id.fragment_container, new SettingsFragment()).commit();
            }
        });

        view.findViewById(R.id.setAlarmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                make intent
                Calendar calendar = Calendar.getInstance();
                calendar.set(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        timePicker.getHour(),
                        timePicker.getMinute(),0);
                setAlarm(calendar);
                Toast.makeText(getContext(), String.valueOf(timePicker.getHour()), Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.cancelAlarmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("notiFragment", "Cancel all scheduled jobs");
                JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(
                        Context.JOB_SCHEDULER_SERVICE);
                List<JobInfo> allPendingJobs = scheduler.getAllPendingJobs();
                for (JobInfo info : allPendingJobs) {
                    int id = info.getId();
                    scheduler.cancel(id);
                }
                Toast.makeText(getContext(), "All Job Canceled", Toast.LENGTH_SHORT).show();
            }
        });



        notificationManager = NotificationManagerCompat.from(getContext());
        createNotificationChannel();

        return view;
    }




    private void setAlarm(Calendar targetCal) {
        Toast.makeText(getContext(), "Alarm is set at " + targetCal.getTime(),
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),pendingIntent);

//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(this, AlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
//        alarmManager.setRepeating(AlarmManager.RTC,timeInMillis,AlarmManager.INTERVAL_DAY,pendingIntent);
//
//        Toast.makeText(this,"alarm is set",Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("myChannelId", "channelName", importance);
            channel.setDescription("this is channel myChannelId");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
