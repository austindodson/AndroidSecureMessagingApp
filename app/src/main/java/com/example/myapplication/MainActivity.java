package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {
    private String username;
    private String seshid;
    private String myPubkeyStr;
    private String myPrivkeyStr;
    byte[] privy, publy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        username = getIntent().getStringExtra("KEY");
        seshid = getIntent().getStringExtra("KEY2");

        setKeyPair(username, seshid);

        final ListView listView = (ListView) findViewById(R.id.listview);
        String[] values = new String[]{"155343325764021", "163669241865747", "Contact 2", "Contact 3"};
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_expandable_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sendingTo = (String) parent.getItemAtPosition(position);
                System.out.println(sendingTo);
                Intent intent = new Intent(MainActivity.this, Messaging.class);
                intent.putExtra("SEND", sendingTo);
                intent.putExtra("USER", username);
                intent.putExtra("SESH", seshid);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // User chose the "Settings" item, show the message edit screen
                startActivity(new Intent(MainActivity.this, ContactsEdit.class));
                return true;

            case R.id.logout:
                // User chose the "Logout" action, attempt logout
                LogoutAuthenticate(username, seshid);
                startActivity(new Intent(MainActivity.this, Login.class));
                return true;

            default:
                // If we got here, the user's action was not recognized
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public int LogoutAuthenticate(String username, String sesh) {
        //hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //hack

        URL url;
        HttpURLConnection conn;
        byte[] encrypted, encrypted1;
        DataInputStream dis = null;
        PublicKey publicKey = null;
        byte[] keyBytes = new byte[4096];

        try {
            //open public.der and convert to public key
            BufferedInputStream bufStream = new BufferedInputStream((getApplicationContext().getResources().openRawResource(R.raw.public123)));
            bufStream.read(keyBytes, 0, keyBytes.length);
            bufStream.close();
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(publicSpec);
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cipher cipher = null;
        //instantiate cipher with RSA
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        //assign public key to cipher
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //encrypt username and password
        encrypted = null;
        encrypted1 = null;
        try {
            encrypted = cipher.doFinal(username.getBytes());
            encrypted1 = cipher.doFinal(sesh.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        //encode username and password
        String str = null;
        String str1 = null;
        try {
            str = Base64.encodeToString(encrypted, Base64.DEFAULT);
            str1 = Base64.encodeToString(encrypted1, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //remove newlines from username and password
        str.replaceAll("\\n", "");
        str1.replaceAll("\\n", "");


        try {
            url = new URL("http://54.218.252.173/sms/logout.php");
            String param = "user=" + URLEncoder.encode(str, "UTF-8") +
                    "&sessionid=" + URLEncoder.encode(str1, "UTF-8");

            //set up HTTP connection
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setFixedLengthStreamingMode(param.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            //write username and password to HTTP connection
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(param);
            writer.flush();
            writer.close();
            os.close();

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String[] myArr;
            Scanner inStream = new Scanner(conn.getInputStream());
            while ((response = inStream.nextLine()) != null) {

            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return 0;
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = new byte[2048];
        try {
            // get an RSA cipher object
            final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }


    public int setKeyPair(String username, String sesh) {
        //hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //hack

        URL url;
        HttpURLConnection conn;
        byte[] encrypted, encrypted1, encrypted2;
        DataInputStream dis = null;
        PublicKey publicKey = null;
        PublicKey myPubKey = null;
        PrivateKey myPrivKey = null;
        String myPubKeyStr = null;
        String myPrivKeyStr = null;
        byte[] keyBytes = new byte[4096];
        KeyFactory keyFactory;

        try {
            //open public.der and convert to public key
            BufferedInputStream bufStream = new BufferedInputStream((getApplicationContext().getResources().openRawResource(R.raw.public123)));
            bufStream.read(keyBytes, 0, keyBytes.length);
            bufStream.close();
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(keyBytes);
            keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(publicSpec);
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //create files in internal
            File pubFile = new File(MainActivity.this.getFilesDir(), "mypubkey.txt");
            File privFile = new File(MainActivity.this.getFilesDir(), "myprivkey.txt");
            //output streams to files in internal
            FileOutputStream fosPub = openFileOutput("mypubkey.txt", Context.MODE_APPEND);
            FileOutputStream fosPriv = openFileOutput("myprivkey.txt", Context.MODE_APPEND);
            //Readers for file
            BufferedReader bufPub = new BufferedReader(new FileReader(pubFile));
            BufferedReader bufPriv = new BufferedReader(new FileReader(privFile));
            //first line of each file
            String firstPubLine = bufPub.readLine();
            String firstPrivLine = bufPriv.readLine();
            //check if anything in the files
            if(firstPubLine == null || firstPrivLine == null) {
                //generate key pairs if files are empty
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair myPair = keyGen.generateKeyPair();
                myPubKey = myPair.getPublic();
                myPrivKey = myPair.getPrivate();
                byte[] myPubKeyBytes = myPubKey.getEncoded();
                byte[] myPrivKeyBytes = myPrivKey.getEncoded();
                fosPub.write(myPubKeyBytes);
                fosPub.close();
                fosPriv.write(myPrivKeyBytes);
                fosPriv.close();
            }
            //read from files in internal
            FileInputStream pubIn = MainActivity.this.openFileInput("mypubkey.txt");
            FileInputStream privIn = MainActivity.this.openFileInput("myprivkey.txt");
            //byte arrays to read into from file
            publy = new byte[(int)pubFile.length()];
            privy = new byte[(int)privFile.length()];
            //read from files into byte array
            pubIn.read(publy);
            privIn.read(privy);
            //get strings
            myPubKeyStr = Base64.encodeToString(publy, Base64.DEFAULT);
            myPrivKeyStr = Base64.encodeToString(privy, Base64.DEFAULT);
            System.out.println("PUB: "+ myPubKeyStr);
            System.out.println("PRIV: "+ myPrivKeyStr);
            pubIn.close();
            privIn.close();
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Cipher cipher = null;
        //instantiate cipher with RSA
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        //assign public key to cipher
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //encrypt username, password
        encrypted = null;
        encrypted1 = null;
        try {
            encrypted = cipher.doFinal(username.getBytes());
            encrypted1 = cipher.doFinal(sesh.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        //encode username and password
        String str = null;
        String str1 = null;
        try {
            str = Base64.encodeToString(encrypted, Base64.DEFAULT);
            str1 = Base64.encodeToString(encrypted1, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //remove newlines from username and password
        str.replaceAll("\\n", "");
        str1.replaceAll("\\n", "");

        try {
            //send encoded username and pass, plain text public
            url = new URL("http://54.218.252.173/sms/setpubkey.php");
            String param = "user=" + URLEncoder.encode(str, "UTF-8") +
                    "&sessionid=" + URLEncoder.encode(str1, "UTF-8") +
                    "&pubkey=" + URLEncoder.encode(myPubKeyStr, "UTF-8");

            //set up HTTP connection
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setFixedLengthStreamingMode(param.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            //write username and password to HTTP connection
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(param);
            writer.flush();
            writer.close();
            os.close();

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String[] myArr;
            Scanner inStream = new Scanner(conn.getInputStream());
            while ((response = inStream.nextLine()) != null) {
                System.out.println(response);
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return 0;
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
