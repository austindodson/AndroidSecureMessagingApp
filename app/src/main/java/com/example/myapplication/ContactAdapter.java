package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by JDL-PC on 4/24/2017.
 */

public class ContactAdapter extends BaseAdapter {

    private Context mContext;
    private List<Contacts> contactList;

    public ContactAdapter(Context context, List<Contacts> list) {
        mContext = context;
        contactList = list;
    }
    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contacts entry = contactList.get(position);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.contacts_row, null);
        }
        TextView alias = (TextView)convertView.findViewById(R.id.Alias);
        alias.setText(entry.getAlias());
        TextView username = (TextView)convertView.findViewById(R.id.userID);
        username.setText(entry.getUsername());


        return convertView;
    }
}