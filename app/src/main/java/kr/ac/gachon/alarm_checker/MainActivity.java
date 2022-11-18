package kr.ac.gachon.alarm_checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click_main(View V){
        Intent i1 = new Intent(MainActivity.this, calendar.class);
        startActivity(i1);
    }
}

