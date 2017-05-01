
package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by JDL-PC on 4/20/2017.
 */

public class Contacts implements Parcelable, Serializable {

    private String alias;
    private String username;

    public Contacts(String alias, String username) {
        super();
        this.username = username;
        this.alias = alias;
    }

    protected Contacts(Parcel in) {
        alias = in.readString();
        username = in.readString();
    }

    public Contacts() {
        super();
    }

    public static final Creator<Contacts> CREATOR = new Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel in) {
            return new Contacts(in);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alias);
        dest.writeString(username);
    }
}