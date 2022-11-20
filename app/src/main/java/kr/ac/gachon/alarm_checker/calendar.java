package kr.ac.gachon.alarm_checker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;


public class calendar extends AppCompatActivity {
    // create material calendar
    MaterialCalendarView calendarView;

    // get instances for firebase database
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    // date arraylist
    ArrayList<CalendarDay> failDates = new ArrayList<>();
    ArrayList<CalendarDay> successDates = new ArrayList<>();

    // get current date information
    long currentDate = System.currentTimeMillis();
    Date mDate = new Date(currentDate);
    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM");
    String stringDate = date.format(mDate);
    int year = Integer.parseInt(stringDate.substring(0, stringDate.indexOf("-")));
    int month = Integer.parseInt(stringDate.substring(stringDate.indexOf("-") + 1));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // instantiate calendar view and set
        // change locale, set calendar unmovable
        // user cannot manipulate calendar
        calendarView = findViewById(R.id.calendar);
        calendarView.state()
                .edit()
                .commit();
        calendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.month_kor)));
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.day_kor)));
        calendarView.setTopbarVisible(false);
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
        calendarView.setPagingEnabled(false);


        DatabaseReference statsRef = firebaseDatabase.getReference();
        statsRef.child("stats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int statsArray[] = new int[31];
                getStats stats = snapshot.getValue(getStats.class);
                statsArray = stats.getStatsArray();

                for(int i = 0; i < 31; i++){
                    // if user failed alarm mission
                    if(statsArray[i] == 0){
                        int failed_day = i + 1;
                        failDates.add(CalendarDay.from(year, month - 1, failed_day));
                    }
                    // if user succeed alarm mission
                    else if(statsArray[i] == 1){
                        int success_day = i + 1;
                        successDates.add(CalendarDay.from(year, month - 1, success_day));
                    }
                    calendarView.addDecorator(new FailDecorator(failDates));
                    calendarView.addDecorator(new SuccessDecorator(successDates));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // go back to main
        Button main = (Button)findViewById(R.id.mainButton);
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(intent, 101);
                overridePendingTransition(0, 0);
            }
        });
    }

    // decorate date tile of calendar
    public class FailDecorator implements DayViewDecorator {
        private final Drawable fail = getDrawable(R.drawable.fail);
        private CalendarDay mDay;
        private HashSet<CalendarDay> dates;

        public FailDecorator(Collection<CalendarDay> date){
            dates = new HashSet<>(date);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(fail);
        }
    }

    public class SuccessDecorator implements DayViewDecorator{
        private final Drawable success = getDrawable(R.drawable.success);
        private CalendarDay mDay;
        private HashSet<CalendarDay> dates;

        public SuccessDecorator(Collection<CalendarDay> date){
            dates = new HashSet<>(date);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(success);
        }
    }
}
