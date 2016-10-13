package com.codepath.simpletodo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements EditTodoItemDialogFragment.EditTodoItemDialogListener {

    ArrayList<TodoItem> items;
    TodoItemAdapter itemsAdapter;
    ListView lvItems;

    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Construct the data source
        readItems();
        // Create the adapter to convert the array to views
        itemsAdapter = new TodoItemAdapter(this, items);
        // Attach the adapter to a ListView
        lvItems = (ListView) findViewById(R.id.lvItems);
        lvItems.setAdapter(itemsAdapter);

        setupListViewListener();
    }

    public void onAddItem(View v) {
        editTodoItem("Add a todo item", -1);
    }

    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                items.get(pos).delete();
                items.remove(pos);
                itemsAdapter.notifyDataSetChanged();
                return true;
            }
        });

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
                editTodoItem("Edit a todo item", pos);
            }
        });
    }

    private void editTodoItem(String title, int pos) {
        FragmentManager fm = getSupportFragmentManager();
        EditTodoItemDialogFragment editTodoItemDialogFragment =
                EditTodoItemDialogFragment.newInstance(title);
        Bundle args = new Bundle();
        if (pos != -1) {
            args.putString("itemText", items.get(pos).text);
        } else {
            args.putString("itemText", "");
        }
        args.putInt("itemIndex", pos);
        editTodoItemDialogFragment.setArguments(args);
        editTodoItemDialogFragment.show(fm, "activity_edit");
    }

    @Override
    public void onFinishEditDialog(String itemText, int pos) {
        if (pos != -1) {
            updateItem(pos, itemText);
        } else {
            writeNewItem(itemText);
        }
    }

    private void readItems() {
        items = (ArrayList) SQLite.select().from(TodoItem.class).queryList();
    }

    private void writeNewItem(String itemText) {
        TodoItem newItem = new TodoItem();
        newItem.id = maxItemId() + 1;
        newItem.text = itemText;
        newItem.priority = "High";
        itemsAdapter.add(newItem);
        newItem.save();
    }

    private void updateItem(int pos, String text) {
        TodoItem oldItem = items.get(pos);
        oldItem.text = text;
        oldItem.save();
        items.set(pos, oldItem);
        itemsAdapter.notifyDataSetChanged();
    }

    private int maxItemId() {
        int maxId = -1;
        for (TodoItem item : items) {
            if (item.id > maxId) {
                maxId = item.id;
            }
        }
        return maxId;
    }
}
