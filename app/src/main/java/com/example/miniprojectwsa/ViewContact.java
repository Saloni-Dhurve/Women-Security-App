package com.example.miniprojectwsa;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;


public class ViewContact extends AppCompatActivity {

    private static final String PREFS_NAME = "ContactsPrefs";
    private static final String CONTACTS_KEY = "contacts";

    private ArrayList<String> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewcontact);

        loadContacts();

        ListView listView = findViewById(R.id.listViewContacts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listView.setAdapter(adapter);
    }

    private void loadContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactsSet = prefs.getStringSet(CONTACTS_KEY, new HashSet<String>());
        contactsList = new ArrayList<>(contactsSet);

    }


    public void onBackClick(View view) {
        // Handle back button click (navigate to previous activity)
        finish();
    }
}