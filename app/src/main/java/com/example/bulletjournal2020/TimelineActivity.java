package com.example.bulletjournal2020;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.sqlite.SQLiteDatabase;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bulletjournal2020.data.HabitContract.HabitEntry;
import com.example.bulletjournal2020.data.HabitDbHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;

public class TimelineActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<HabitData>> {

    private static final String LOG_TAG = com.example.bulletjournal2020.TimelineActivity.class.getName();

    /**
     * Global declarations
     **/
    ListView habitsList;
    private CustomAdapter customAdapter;
    LoaderManager loaderManager;
    private BottomNavigationView bottomNavigationView;

    //to create HabitDbHelper instance
    private HabitDbHelper mDbHelper;

    //Books loaded ID, default = 1 currently using single Loader
    private static int HABITS_LOADER_ID = 1;

    //FAB and TextViews
    Button addHabit;

    //boolean value for open and close FAB's
    boolean isFabOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        habitsList = findViewById(R.id.habit_list);

        addHabit = findViewById(R.id.addHabitbutton);

        customAdapter = new CustomAdapter(getApplicationContext(), new ArrayList<HabitData>());
        //setting customAdapter for book list view
        habitsList.setAdapter(customAdapter);

        //Run SQL queries in background thread
        loaderManager = getLoaderManager();
        loaderManager.initLoader(HABITS_LOADER_ID, null, this);

        //get current day
        Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        habitsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LayoutInflater factory = LayoutInflater.from(TimelineActivity.this);
                final View dialogView = factory.inflate(R.layout.habitpopup_dialog, null);
                final AlertDialog dialog = new AlertDialog.Builder(TimelineActivity.this).create();

                dialog.setView(dialogView);

                //we don't want title
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.habitpopup_dialog);

                //bind views from dialog layout
                final TextView dialogHabitNumber = dialogView.findViewById(R.id.dialog_habit_number);
                ImageView dialogAdd = dialogView.findViewById(R.id.dialog_add_button);
                ImageView dialogSub = dialogView.findViewById(R.id.dialog_sub_button);
                Button dialogSave = dialogView.findViewById(R.id.dialog_save);

                //get habit records and update habit number textview
                HabitData currentHabit = customAdapter.getItem(position);
                final Integer records[] = currentHabit.getHabitDays();
                dialogHabitNumber.setText(String.valueOf(records[currentDay - 1]));

                final Integer habitId = currentHabit.getHabitId();

                dialogAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = Integer.parseInt(dialogHabitNumber.getText().toString());
                        i += 1;
                        dialogHabitNumber.setText(String.valueOf(i));

                    }
                });

                dialogSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = Integer.parseInt(dialogHabitNumber.getText().toString());
                        i -= 1;
                        dialogHabitNumber.setText(String.valueOf(i));

                    }

                });

                dialogSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //just close dialog box if user hasn't changed value
                        if (dialogHabitNumber.getText().toString().equals
                                (String.valueOf(records[currentDay - 1])
                                )) {
                            dialog.dismiss();
                        } else {

                            //get value from textview and update records array
                            records[currentDay - 1] = Integer.parseInt(dialogHabitNumber.getText().toString());

                            //using column array instead of switch-case to update habit of current day
                            String[] columnNames = {HabitEntry.COLUMN_SUNDAY,
                                    HabitEntry.COLUMN_MONDAY,
                                    HabitEntry.COLUMN_TUESDAY,
                                    HabitEntry.COLUMN_WEDNESDAY,
                                    HabitEntry.COLUMN_THURSDAY,
                                    HabitEntry.COLUMN_FRIDAY,
                                    HabitEntry.COLUMN_SATURDAY};

                            SQLiteDatabase db = mDbHelper.getWritableDatabase();

                            ContentValues values = new ContentValues();

                            //good thing that we have column names array xD
                            for (int i = 0; i <= 6; i++) {
                                values.put(columnNames[i], records[i]);
                            }

                            db.update(HabitEntry.TABLE_NAME, values,
                                    HabitEntry._ID + " = ?",
                                    new String[]{String.valueOf(habitId)});

                            db.close();

                            loaderManager.restartLoader(HABITS_LOADER_ID, null, TimelineActivity.this);

                            dialog.dismiss();


                            Toast.makeText(getApplicationContext(), "Habit updated successfully", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                dialog.show();

            }

        });

        habitsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                HabitData currentHabit = customAdapter.getItem(position);

                if (currentHabit != null) {

                    Intent intent = new Intent(getApplicationContext(), HabitEditorActivity.class);

                    //probably easy way to let EditorActivity know if the data is present or not
                    intent.putExtra("dataPresent", true);

                    intent.putExtra("id", currentHabit.getHabitId());
                    intent.putExtra("name", currentHabit.getHabitName());

                    Integer records[] = currentHabit.getHabitDays();

                    intent.putExtra("dayNum", records[currentDay - 1]);

                    startActivity(intent);

                }
                return true;
            }

        });

        mDbHelper = new HabitDbHelper(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.Timeline);

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
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.Timeline:

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.manualmenu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.guideToActivity:

                AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

                final View newView = inflater.inflate(R.layout.tracker_manual, null);

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

            default:

                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public Loader<ArrayList<HabitData>> onCreateLoader(int id, Bundle args) {
        return new HabitsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<HabitData>> loader, ArrayList<HabitData> data) {
        customAdapter.clear();
        if (data != null && !data.isEmpty()) {
            customAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<HabitData>> loader) {
        customAdapter.clear();
    }

    public void addHabit(View view) {

        Intent intent = new Intent(getApplicationContext(), HabitEditorActivity.class);

        //probably easy way to let EditorActivity know if the data is present or not
        intent.putExtra("dataPresent", false);

        startActivity(intent);

    }

}
