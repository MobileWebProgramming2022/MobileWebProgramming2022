package kr.ac.gachon.alarm_checker;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MorningAlarmActivity extends AppCompatActivity {
    // get current date information
    long currentDate = System.currentTimeMillis();
    Date mDate = new Date(currentDate);
    SimpleDateFormat date = new SimpleDateFormat("dd");
    String stringDate = date.format(mDate);
    int day = Integer.parseInt(stringDate);

    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morning_alarm);

        // ring alarm when arrival to MorningAlarmActivity
        mediaPlayer = MediaPlayer.create(MorningAlarmActivity.this, R.raw.alarm_music_ex);
        mediaPlayer.start();

        // user clicked 'to main' : failed to complete alarm mission.
        // update database as fail(0)
        Button back = (Button)findViewById(R.id.fromNightAlarmGotoActivity_button);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // when fail, update stats into database
                if(day < 10){
                    database.child("stats").child("day0" + day).setValue(0);
                }
                else{
                    database.child("stats").child("day" + day).setValue(0);
                }
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent2);
                finish();
            }
        });

        // user clicked 'stop' : succeeded to complete alarm mission.
        // update database as success(1)
        Button alarm_stop_button = (Button)findViewById(R.id.morning_alarm_stop_button);
        alarm_stop_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // when success, update stats into database
                if(day < 10){
                    database.child("stats").child("day0" + day).setValue(1);
                }
                else{
                    database.child("stats").child("day" + day).setValue(1);
                }
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        });
    }
}
