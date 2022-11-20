package kr.ac.gachon.alarm_checker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            Log.d("ALARM", intent.getExtras().getString("state"));
        }

        String what_msg = intent.getExtras().getString("what");

        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(intent.getExtras().getString("state").equals("morning")) {
            PendingIntent pendingIntent=PendingIntent.getBroadcast(context,20,intent,PendingIntent.FLAG_MUTABLE);

            // set alarm(20secs later) + 20000 -> 8640000(24hr - 1 day) /2min(20000 * 3 * 2 = 120000)
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 8640000,pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 8640000,pendingIntent);
            }

            Intent tempIntent = new Intent(context.getApplicationContext(), MorningAlarmActivity.class);
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(tempIntent);
        }
        if(intent.getExtras().getString("state").equals("night")){
            PendingIntent pendingIntent2=PendingIntent.getBroadcast(context,30,intent,PendingIntent.FLAG_MUTABLE);

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+8640000,pendingIntent2);
            }else{
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+8640000,pendingIntent2);
            }

            Intent tempIntent = new Intent(context.getApplicationContext(), NightAlarmActivity.class);
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(tempIntent);
        }
    }
}
