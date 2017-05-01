package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.ContextMenu.*;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class ContactsEdit extends AppCompatActivity {
    public String userID;
    public String Alias;
    public String sessionid;
    final Context c = this;
    public ListView LVcontacts;
    ArrayList<Contacts> listContacts;
    ContactAdapter adapter2;
    public int position;
    public boolean isValid = false;
    private String regex = ".*?[^0-9].*";

    public ContactsEdit() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_edit);
        Toolbar myToolbarRename = (Toolbar) findViewById(R.id.my_toolbar_rename);
        setSupportActionBar(myToolbarRename);
        //sessionid = getIntent().getStringExtra("SES");
        getSupportActionBar().setTitle("Manage Contacts");
        listContacts = getIntent().getParcelableArrayListExtra("listContacts");
        LVcontacts = (ListView)findViewById(R.id.listContacts2);
        adapter2 = new ContactAdapter(this, listContacts);
        LVcontacts.setAdapter(adapter2);
        registerForContextMenu(LVcontacts);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        sessionid = getIntent().getStringExtra("SESSIONID");
        System.out.println(sessionid);

        final Button add = (Button)findViewById(R.id.ADD);
        //final Button remove = (Button)findViewById(R.id.REMOVE);
        //final Button edit = (Button)findViewById(R.id.EDIT);

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                final View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                alertDialogBuilderUserInput.setView(mView);
                final EditText usernameContact = (EditText) mView.findViewById(R.id.userID);
                final EditText aliasContact = (EditText) mView.findViewById(R.id.Alias);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                userID = usernameContact.getText().toString();
                                Alias = aliasContact.getText().toString();
                                Contacts contact =  new Contacts(Alias, userID);
                                listContacts.add(contact);
                                saveContacts();
                                recreate();
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                final AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                usernameContact.addTextChangedListener(new TextValidator(usernameContact) {
                    @Override
                    public void validate(TextView textView, String text) {
                        System.out.println(text);
                        if (text.matches(regex)){
                            showToast();
                            ((AlertDialog) alertDialogAndroid).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            ((AlertDialog) alertDialogAndroid).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });

            }
        });

    }

    /**
     * MENU
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        position = info.position;
        System.out.println("Position is: " + position);
        if (v.getId()==R.id.listContacts2) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_items, menu);
        }
    }

    public boolean onOptionsItemsSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                System.out.println("tb: "+ sessionid);
                Intent i = new Intent(ContactsEdit.this, MainActivity.class);
                i.putExtra("KEY", userID);
                i.putExtra("KEY2", sessionid);
                System.out.println("turdybirdy: "+ sessionid);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.edit:
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                View mView = layoutInflaterAndroid.inflate(R.layout.user_edit_dialog_box, null);
                final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                alertDialogBuilderUserInput.setView(mView);
                final EditText usernameContact = (EditText) mView.findViewById(R.id.userID);
                final EditText aliasContact = (EditText) mView.findViewById(R.id.Alias);
                Contacts tempContact = listContacts.get(position);
                usernameContact.setText(tempContact.getUsername());
                aliasContact.setText(tempContact.getAlias());
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                userID = usernameContact.getText().toString();
                                Alias = aliasContact.getText().toString();
                                Contacts contact =  new Contacts(Alias, userID);
                                //listContacts = getIntent().getParcelableArrayListExtra("listContacts");
                                listContacts.set(position,contact);
                                saveContacts();

                                recreate();




                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                final AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                usernameContact.addTextChangedListener(new TextValidator(usernameContact) {
                    @Override
                    public void validate(TextView textView, String text) {
                        System.out.println(text);
                        if (text.matches(regex)){
                            showToast();
                            ((AlertDialog) alertDialogAndroid).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            ((AlertDialog) alertDialogAndroid).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                return true;
            case R.id.delete:
                listContacts.remove(position);
                saveContacts();
                recreate();
                return true;

            default:
                return super.onContextItemSelected(item);

        }
    }

    public void showToast(){
        Toast.makeText(c, "Only numbers are allowed!", Toast.LENGTH_SHORT).show();
    }

    public void saveContacts(){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("contacts", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fos);
        } catch (IOException e) {e.printStackTrace();

        }
        try {
            oos.writeObject(listContacts);
        } catch(IOException exception) {
            exception.printStackTrace();
        }
        try {
            oos.close();
        }catch (Exception e){e.printStackTrace();}
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.items_edit, menu);
        return true;
    }
}