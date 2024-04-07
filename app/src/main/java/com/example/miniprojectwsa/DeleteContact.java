package com.example.miniprojectwsa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class DeleteContact extends AppCompatActivity {

    private static final String PREFS_NAME = "ContactsPrefs";
    private static final String CONTACTS_KEY = "contacts";

    private ArrayList<String> contactsList;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletecontact);

        loadContacts();

        ListView listView = findViewById(R.id.listViewContacts);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deleteContact(position);
            }
        });
    }

    private void loadContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactsSet = prefs.getStringSet(CONTACTS_KEY, new HashSet<String>());
        contactsList = new ArrayList<>(contactsSet);
    }

    private void deleteContact(int position) {
        contactsList.remove(position);
        adapter.notifyDataSetChanged();

        saveContacts();

        Toast.makeText(this, "Contact deleted successfully!", Toast.LENGTH_SHORT).show();
    }

    private void saveContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> contactsSet = new HashSet<>(contactsList);
        editor.putStringSet(CONTACTS_KEY, contactsSet);
        editor.apply();
    }

    public void onBackClick(View view) {
        // Handle back button click (navigate to previous activity)
        finish();
    }

    private static class CustomArrayAdapter extends ArrayAdapter<String> {
        public CustomArrayAdapter(Context context, int resource, ArrayList<String> items) {
            super(context, resource, items);
        }

    }
}