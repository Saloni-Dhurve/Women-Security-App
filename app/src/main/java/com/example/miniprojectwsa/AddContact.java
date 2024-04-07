package com.example.miniprojectwsa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class AddContact extends AppCompatActivity {

    private static final String PREFS_NAME = "ContactsPrefs";
    private static final String CONTACTS_KEY = "contacts";
    private static final int MAX_CONTACTS = 5;

    private ArrayList<String> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcontact);

        loadContacts();
    }

    public void onSaveClick(View view) {
        EditText nameEditText = findViewById(R.id.editTextName);
        EditText phoneEditText = findViewById(R.id.editTextPhone);

        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (!name.isEmpty() && !phone.isEmpty()) {
            if (contactsList.size() < MAX_CONTACTS) {
                saveContact(name, phone);
                Toast.makeText(this, "Contact added successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity after saving
            } else {
                Toast.makeText(this, "You can only add up to 5 contacts!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Name and phone number cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contactsSet = prefs.getStringSet(CONTACTS_KEY, new HashSet<String>());
        contactsList = new ArrayList<>(contactsSet);
    }

    private void saveContact(String name, String phone) {
        contactsList.add(name + ": " + phone);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> contactsSet = new HashSet<>(contactsList);
        editor.putStringSet(CONTACTS_KEY, contactsSet);
        editor.apply();
    }
}
