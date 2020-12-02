package com.example.bulletjournal2020;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.CalendarView;
import android.content.Intent;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.EditText;
import android.widget.Toast;

public class CalendarActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private mySQLiteDBHandler dbHandler;
    private EditText editText;
    private CalendarView calendarView;
    private String selectedDate;
    private SQLiteDatabase sqLiteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);

        editText = findViewById(R.id.editEvent);
        calendarView = findViewById(R.id.calendarView1);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = Integer.toString(year) + Integer.toString(month) + Integer.toString(dayOfMonth);
                ReadDatabase(view);
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.Calendar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.Journal:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.Calendar:
                        return true;
                    case R.id.Timeline:
                        startActivity(new Intent(getApplicationContext(), TimelineActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.Settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                }

                return false;
            }
        });

        try{

            dbHandler = new mySQLiteDBHandler(this, "CalendarDatabase", null,1);
            sqLiteDatabase = dbHandler.getWritableDatabase();
            sqLiteDatabase.execSQL("CREATE TABLE EventCalendar(Date TEXT, Event TEXT)");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendarmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.guideToActivity) {

            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            final View newView = inflater.inflate(R.layout.calendar_manual, null);


            builder.setCancelable(false)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            builder.setView(newView);
            AlertDialog alert = builder.create();
            alert.show();
            return true;

        } else if (id == R.id.googleCalendarOption){

            //open up custom dialog for Google Calendar event

            View view = LayoutInflater.from(CalendarActivity.this).inflate(R.layout.calendarform, null);

            final EditText eventTitle = (EditText) view.findViewById(R.id.editEventName);

            final EditText eventDescription = (EditText) view.findViewById(R.id.editEventDescrip);

            final EditText eventLocation = (EditText) view.findViewById(R.id.editEventLocation);

            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);

            builder.setView(view);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (!eventTitle.getText().toString().isEmpty()) {

                        Intent intent = new Intent(Intent.ACTION_INSERT);

                        intent.setData(CalendarContract.Events.CONTENT_URI);

                        intent.putExtra(CalendarContract.Events.TITLE, eventTitle.getText().toString());

                        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventLocation.getText().toString());

                        intent.putExtra(CalendarContract.Events.DESCRIPTION, eventDescription.getText().toString());

                        if (intent.resolveActivity(getPackageManager()) != null) {

                            startActivity(intent);

                        } else {

                            Toast.makeText(CalendarActivity.this, "Google Calendar can not be accessed on this device", Toast.LENGTH_SHORT).show();

                        }

                    }
                }
            }).setNegativeButton("Cancel", null).setCancelable(false);

            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void InsertDatabase(View view){
        ContentValues contentValues = new ContentValues();
        contentValues.put("Date", selectedDate);
        contentValues.put("Event", editText.getText().toString());
        sqLiteDatabase.insert("EventCalendar", null, contentValues);
        Toast.makeText(getApplicationContext(),"Event was added", Toast.LENGTH_SHORT).show();

    }

    public void ReadDatabase(View view){
        String query = "Select Event from EventCalendar where Date = " + selectedDate;
        try{
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            cursor.moveToFirst();
            editText.setText(cursor.getString(0));
        }
        catch (Exception e){
            e.printStackTrace();
            editText.setText("");
        }
    }

}