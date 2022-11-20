package kr.ac.gachon.alarm_checker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    // get current date information
    long currentDate = System.currentTimeMillis();
    Date mDate = new Date(currentDate);
    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM");
    String stringDate = date.format(mDate);
    String array[] = stringDate.split("-");
    int year = Integer.parseInt(array[0]);
    int month = Integer.parseInt(array[1]);

    // set alarm day values
    int mHour_wake = 7;
    int mMinute_wake = 0;
    int mHour_sleep = 23;
    int mMinute_sleep = 30;
    String wakeTime = "07:00";
    String sleepTime = "23:30";

    long now = System.currentTimeMillis();
    Calendar calendar = Calendar.getInstance();


    // set base firebase database reference
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // set new alarm manager
    private AlarmManager alarmManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize database when month/year value changes
        database.child("lastTimeVisited").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getLastTimeVisited lastTimeVisited = snapshot.getValue(getLastTimeVisited.class);
                int last_year = lastTimeVisited.getYear();
                int last_month = lastTimeVisited.getMonth();

                if(year > last_year || month > last_month) {
                    for (int i = 0; i < 31; i++) {
                        if (i < 10) {
                            database.child("stats").child("day0" + i).setValue(2);
                        } else {
                            database.child("stats").child("day" + i).setValue(2);
                        }
                    }
                    // update last time visited
                    database.child("lastTimeVisited").child("month").setValue(month);
                    database.child("lastTimeVisited").child("year").setValue(year);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // set new alarm manager
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Button sleepTimeBtn = findViewById(R.id.sleepTimeView);
        Button wakeTimeBtn = findViewById(R.id.wakeTimeView);

        Intent timeIntent = getIntent();
        String savedTime = timeIntent.getStringExtra("TIME");
        if (savedTime != null) {
            StringTokenizer st = new StringTokenizer(savedTime, " ");
            String SLEEP_TIME_FROM = st.nextToken();
            String WAKE_TIME_FROM = st.nextToken();

            sleepTimeBtn.setText(SLEEP_TIME_FROM);
            wakeTimeBtn.setText(WAKE_TIME_FROM);
            sleepTime = SLEEP_TIME_FROM;
            wakeTime = WAKE_TIME_FROM;
        }

        // set alarm as user input: morning or night
        Intent alarmIntent = new Intent(getApplicationContext(), Alarm.class);
        Button SAVE = findViewById(R.id.SaveBtn);
        SAVE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMorningAlarm(alarmIntent);
                setNightAlarm(alarmIntent);
            }
        });

        // get current time for initial time picker
        Calendar cal = new GregorianCalendar();
        mHour_wake = cal.get(Calendar.HOUR_OF_DAY);
        mMinute_wake = cal.get(Calendar.MINUTE);
        mHour_sleep = cal.get(Calendar.HOUR_OF_DAY);
        mMinute_sleep = cal.get(Calendar.MINUTE);

        // view time picker dialog when alarm/sleeping time button has clicked
        wakeTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new TimePickerDialog(MainActivity.this, wakeTimeSetListener, mHour_wake, mMinute_wake, false).show();
            }
        });

        sleepTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new TimePickerDialog(MainActivity.this, sleepTimeSetListener, mHour_sleep, mMinute_sleep,false).show();
            }
        });

        // go to calendar
        Button calendarButton = findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), calendar.class);
                startActivityForResult(intent, 101);
                overridePendingTransition(0,0);
            }
        });
    }

    // inflate time picker
    TimePickerDialog.OnTimeSetListener wakeTimeSetListener
            = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            // get user time-input
            mHour_wake = hourOfDay;
            mMinute_wake = minute;

            // update textview as selected time
            UpdateNow("WAKE");
        }
    };

    TimePickerDialog.OnTimeSetListener sleepTimeSetListener
            = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            mHour_sleep = hourOfDay;
            mMinute_sleep = minute;

            UpdateNow("SLEEP");
        }
    };

    // update button text as user selected
    @SuppressLint("DefaultLocale")
    void UpdateNow(String id) {

        if (id.compareTo("WAKE") == 0) {
            Button wakeTimeView = findViewById(R.id.wakeTimeView);
            wakeTime = String.format("%02d:%02d", mHour_wake, mMinute_wake);
            wakeTimeView.setText(wakeTime);
        } else {
            Button sleepTimeView = findViewById(R.id.sleepTimeView);
            sleepTime = String.format("%02d:%02d", mHour_sleep, mMinute_sleep);
            sleepTimeView.setText(sleepTime);
        }
    }

    // set morning alarm
    // @param alarmIntent Connect Alarm.java activity and pass morning/night alarm time via bundle. Set alarm manager as 'exact' for android to exactly handle time event
    // If current time is earlier than alarm to set, set as tomorrow's alarm
    // setNightAlarm's logic is same as setMorningAlarm
    void setMorningAlarm(Intent alarmIntent){
        Bundle bundle = new Bundle();
        bundle.putString("state", "morning");
        alarmIntent.putExtras(bundle);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 20, alarmIntent, PendingIntent.FLAG_MUTABLE);

        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, mHour_wake);
        calendar.set(Calendar.MINUTE, mMinute_wake);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        if(calendar.getTimeInMillis() < now){
            Log.d("알람","현재시간보다 이전 - morning");
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String[] today = sdf.format(date).split("-"); //year,month,day of today

            int year_today = Integer.parseInt(today[0]);
            int month_today = Integer.parseInt(today[1]);
            int day_today = Integer.parseInt(today[2]);

            GregorianCalendar nowcalendar= new GregorianCalendar(year_today, month_today, day_today + 1,mHour_wake, mMinute_wake);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nowcalendar.getTimeInMillis(), pendingIntent);
                //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nowcalendar.getTimeInMillis(), pendingIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            }
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            }
        }

    }

    void setNightAlarm(Intent alarmIntent){
        Bundle bundle = new Bundle();
        bundle.putString("state", "night");
        alarmIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 30, alarmIntent, PendingIntent.FLAG_MUTABLE);//MUTABLE이라 바꾸면 자동으로 바뀐다.

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, mHour_sleep);
        calendar2.set(Calendar.MINUTE, mMinute_sleep);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();

        if(calendar2.getTimeInMillis() < now){
            Log.d("알람","현재시간보다 이전 - night");
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String[] today = sdf.format(date).split("-"); //year,month,day of today

            int year_today = Integer.parseInt(today[0]);
            int month_today = Integer.parseInt(today[1]);
            int day_today = Integer.parseInt(today[2]);

            GregorianCalendar nowcalendar= new GregorianCalendar(year_today, month_today, day_today + 1,mHour_wake, mMinute_wake);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nowcalendar.getTimeInMillis(), pendingIntent);
                //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nowcalendar.getTimeInMillis(), pendingIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pendingIntent);
                //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pendingIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            }
        }
    }
}

