package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Messaging extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);


        final ListView listView = (ListView) findViewById(R.id.MessagingList);
        String[] messages = new String[] {"Contact 1", "Contact 2", "Contact 3"};
        ArrayList<String> list1 = new ArrayList<String>();
        for (int i = 0; i < messages.length; i++){
            list1.add(messages[i]);
        }
        final ArrayAdapter adapter1 = new ArrayAdapter(this,
                android.R.layout.simple_expandable_list_item_1,list1);
        listView.setAdapter(adapter1);
    }



}
