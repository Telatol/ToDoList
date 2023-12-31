package comp5216.sydney.edu.au.todolist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;




public class MainActivity extends AppCompatActivity {

    // Define variables
    ListView listView;
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    EditText addItemEditText;
    ToDoItemDB db;
    ToDoItemDao toDoItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use "activity_main.xml" as the layout
        setContentView(R.layout.activity_main);


        // Reference the "listView" variable to the id "lstView" in the layout
        listView = (ListView) findViewById(R.id.lstView);
        //addItemEditText = (EditText) findViewById(R.id.txtNewItem);

        // Create an ArrayList of String
        //items = new ArrayList<String>();
        //items.add("item one");
        //items.add("item two");

        //readItemsFromFile();

        //Connect and read from Database
        db = ToDoItemDB.getDatabase(this.getApplication().getApplicationContext());
        toDoItemDao = db.toDoItemDao();

        readItemsFromDatabase();


        // Create an adapter for the list view using Android's built-in item layout
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        // Connect the listView and the adapter
        listView.setAdapter(itemsAdapter);

        // Setup listView listeners
        setupListViewListener();
    }

    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                /*
                if (result.getResultCode() == RESULT_OK) {
                    String date = data.getStringExtra("date");
                    String item = data.getStringExtra("item");
                    groceryData.putIfAbsent(date, new ArrayList<>());
                    groceryData.get(date).add(item);
                }*/
            }
    );

    public void onAddItemClick(View view) {
        Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
        if (intent != null) {

            // bring up the second activity
            mLauncher.launch(intent);
            itemsAdapter.notifyDataSetChanged();
        }
    }


    private void setupListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long rowId)
            {
                Log.i("MainActivity", "Long Clicked item " + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_msg)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                items.remove(position); // Remove item from the ArrayList
                                itemsAdapter.notifyDataSetChanged(); // Notify listView adapter to update the list
                                //saveItemsToFile();
                                saveItemsToDatabase();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User cancelled the dialog
                                // Nothing happens
                            }
                        });

                builder.create().show();
                return true;
            }
        });

        // Register a request to start an activity for result and register the result callback
        ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Extract name value from result extras
                        String editedItem = result.getData().getExtras().getString("item");
                        int position = result.getData().getIntExtra("position", -1);
                        items.set(position, editedItem);
                        Log.i("Updated item in list ", editedItem + ", position: " + position);

                        // Make a standard toast that just contains text
                        Toast.makeText(getApplicationContext(), "Updated: " + editedItem, Toast.LENGTH_SHORT).show();
                        itemsAdapter.notifyDataSetChanged();
                        saveItemsToDatabase();
                        //saveItemsToFile();
                    }
                }
        );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String updateItem = (String) itemsAdapter.getItem(position);
                Log.i("MainActivity", "Clicked item " + position + ": " + updateItem);

                Intent intent = new Intent(MainActivity.this, EditToDoItemActivity.class);
                if (intent != null) {
                    // put "extras" into the bundle for access in the edit activity
                    intent.putExtra("item", updateItem);
                    intent.putExtra("position", position);

                    // bring up the second activity
                    mLauncher.launch(intent);
                    itemsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void readItemsFromFile(){
    //retrieve the app's private folder.
    //this folder cannot be accessed by other apps
        File filesDir = getFilesDir();
    //prepare a file to read the data
        File todoFile = new File(filesDir,"todo.txt");
    //if file does not exist, create an empty list
        if(!todoFile.exists()){
            items = new ArrayList<String>();
        }else{
            try{
    //read data and put it into the ArrayList
                items = new ArrayList<String>(FileUtils.readLines(todoFile));
            }
            catch(IOException ex){
                items = new ArrayList<String>();
            }
        }
    }

    private void saveItemsToFile(){
        File filesDir = getFilesDir();
    //using the same file for reading. Should use define a global string instead.
        File todoFile = new File(filesDir,"todo.txt");
        try{
    //write list to file
            FileUtils.writeLines(todoFile,items);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void readItemsFromDatabase() {
        //Use asynchronous task to run query on the background and wait for result
        try {
            // Run a task specified by a Runnable Object asynchronously.
            CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    //read items from database
                    List<ToDoItem> itemsFromDB = toDoItemDao.listAll();
                    items = new ArrayList<String>();
                    if (itemsFromDB != null && itemsFromDB.size() > 0) {
                        for (ToDoItem item : itemsFromDB) {
                            items.add(item.getToDoItemName());
                            Log.i("SQLite read item", "ID: " + item.getToDoItemID() + " Name: " +
                                    item.getToDoItemName());
                        }
                    }
                    System.out.println("I'll run in a separate thread than the main thread.");
                }
            });
            // Block and wait for the future to complete
            future.get();
        }
        catch(Exception ex) {
            Log.e("readItemsFromDatabase", ex.getStackTrace().toString());
        }
    }

    private void saveItemsToDatabase() {
        //Use asynchronous task to run query on the background to avoid locking UI
        try {
        // Run a task specified by a Runnable Object asynchronously.
            CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    //delete all items and re-insert
                    toDoItemDao.deleteAll();
                    for (String todo : items) {
                        ToDoItem item = new ToDoItem(todo);
                        toDoItemDao.insert(item);
                        Log.i("SQLite saved item", todo);
                    }
                    System.out.println("I'll run in a separate thread than the main thread.");
                    }
                });


            // Block and wait for the future to complete
            future.get();
        }
        catch(Exception ex) {
            Log.e("saveItemsToDatabase", ex.getStackTrace().toString());
        }
    }


}