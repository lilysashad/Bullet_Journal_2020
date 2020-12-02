package com.example.bulletjournal2020;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;
    Button addButton;
    EditText editText;
    RecyclerView rvItems;
    toDoAdapter toDoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        addButton = findViewById(R.id.addItemToList);
        editText = findViewById(R.id.id_edit_text);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        toDoAdapter.OnLongClickListener onLongClickListener = new toDoAdapter.OnLongClickListener() {

            @Override
            public void onItemLongClicked(int position) {

                //delete item from model
                items.remove(position);

                //notify adapter
                toDoAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "To-do was removed", Toast.LENGTH_SHORT).show();

                saveItems();

            }

        };

        toDoAdapter.OnClickListener onClickListener = new toDoAdapter.OnClickListener() {

            @Override
            public void onItemClicked(int position){

                Log.d("MainActivity", "Single click at position " + position);

                //create new edit activity
                Intent i = new Intent(MainActivity.this, editToDo.class);

                //pass data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));

                i.putExtra(KEY_ITEM_POSITION, position);

                //display edited activity
                startActivityForResult(i, EDIT_TEXT_CODE);

            }

        };


        toDoAdapter = new toDoAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(toDoAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                String todoItem = editText.getText().toString();

                //add item to model
                items.add(todoItem);

                //notify adapter that item is inserted
                toDoAdapter.notifyItemInserted(items.size()-1);

                editText.setText("");
                Toast.makeText(getApplicationContext(),"To-do was added", Toast.LENGTH_SHORT).show();

                saveItems();
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.Journal);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.Journal:
                        return true;
                    case R.id.Calendar:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                final View newView = inflater.inflate(R.layout.main_manual, null);

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

    //handle result of edit activity
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE){

            //retrieve updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);

            //extract original position of edited item from position key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            //update model at right position with new item text
            items.set(position, itemText);

            //notify adapter
            toDoAdapter.notifyItemChanged(position);

            //persist changes
            saveItems();
            Toast.makeText(getApplicationContext(), "To-do updated successfully", Toast.LENGTH_SHORT).show();

        }

        else{

            Log.w("MainActivity","Unknown call to onActivityResult");

        }

    }

    private File getDataFile(){

        return new File(getFilesDir(), "data.txt");

    }

    //this function loads items by reading each line of data.txt
    private void loadItems() {

        try {

            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));

        }

        catch(IOException e){

            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }

    }

    //this function saves items by writing into data.txt
    private void saveItems(){

        try{

            FileUtils.writeLines(getDataFile(), items);

        }

        catch(IOException e){

            Log.e("MainActivity","Error writing items", e);
        }
    }

}