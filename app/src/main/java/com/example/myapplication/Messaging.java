package com.example.myapplication;

import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public class Messaging extends AppCompatActivity {
    String username;
    String sendingTo;
    String sessionid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_messaging);
        setSupportActionBar(myToolbar);

        username = getIntent().getStringExtra("USER");
        sendingTo = getIntent().getStringExtra("SEND");
        sessionid = getIntent().getStringExtra("SESH");
        getSupportActionBar().setTitle(sendingTo);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        final ListView listView = (ListView) findViewById(R.id.MessagingList);
        String[] messages = new String[] {"Contact 1", "Contact 2", "Contact 3"};
        ArrayList<String> list1 = new ArrayList<String>();
        for (int i = 0; i < messages.length; i++){
            list1.add(messages[i]);
        }
        final ArrayAdapter adapter1 = new ArrayAdapter(this,
                android.R.layout.simple_expandable_list_item_1,list1);
        listView.setAdapter(adapter1);

        EditText messageObj = (EditText) findViewById(R.id.message);
        String message = messageObj.getText().toString();

        Button send = (Button)findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            //send click handling
            public void onClick(View v) {
                EditText messageObj = (EditText) findViewById(R.id.message);
                String message = messageObj.getText().toString();
                sendMessage(username,sessionid, sendingTo, message);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.items2, menu);
        return true;
    }

    public PublicKey getToUserPublic(String user, String sessionid, String sendingTo){
        //hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //hack

        URL url;
        HttpURLConnection conn;
        byte[] encrypted, encrypted1, encrypted2;
        DataInputStream dis = null;
        PublicKey publicKey = null;
        PublicKey ret = null;
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
        encrypted2 = null;
        try {
            encrypted = cipher.doFinal(username.getBytes());
            encrypted1 = cipher.doFinal(sessionid.getBytes());
            encrypted2 = cipher.doFinal(sendingTo.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        //encode username and password
        String str = null;
        String str1 = null;
        String str2 = null;
        try {
            str = Base64.encodeToString(encrypted, Base64.DEFAULT);
            str1 = Base64.encodeToString(encrypted1, Base64.DEFAULT);
            str2 = Base64.encodeToString(encrypted2, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //remove newlines from username and password
        str.replaceAll("\\n", "");
        str1.replaceAll("\\n", "");

        RSAPublicKey somekeyfortesting =null;
        try {
            url = new URL("http://54.218.252.173/sms/getuserpub.php");
            String param = "user=" + URLEncoder.encode(str, "UTF-8") +
                    "&sessionid=" + URLEncoder.encode(str1, "UTF-8") +
                    "&getuser=" + URLEncoder.encode(str2, "UTF-8");

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

            System.out.println("MADE IT REQUEST");

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String myArr = null;
            InputStream inStream = conn.getInputStream();
            Scanner s = new Scanner(inStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            //System.out.println(result);

            result.replaceAll("\\n", "");
            //String decoded = new String(Base64.decode(response, Base64.DEFAULT));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] publicBytes = (new String(Base64.decode(response, Base64.DEFAULT))).getBytes();
            PublicKey retKey = kf.generatePublic(new X509EncodedKeySpec(publicBytes));
            //publicBytes = Base64.decode(result.getBytes(), Base64.DEFAULT);
            //System.out.println(publicBytes.toString());
            //X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            //SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("RSA");
            //somekeyfortesting = (RSAPublicKey)keyFactory.generateSecret(keySpec);
            //System.out.println("somekeyfortesting = " + somekeyfortesting.toString());
            ret = retKey;
            return ret;
            //ret = keyFactory.generatePublic(keySpec)
        }catch(InvalidKeySpecException e){
            e.printStackTrace();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();;
        }catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //System.out.println(ret.toString());
        return ret;
    }

    public void sendMessage(String user, String sessionId, String sendingTo, String message){
        //hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //hack

        URL url;
        HttpURLConnection conn;
        byte[] encrypted, encrypted1, encrypted2, encrypted3;
        DataInputStream dis = null;
        PublicKey publicKey = null;
        PublicKey toKey = null;
        byte[] keyBytes = new byte[4096];

        toKey = getToUserPublic(user, sessionId, sendingTo);
        System.out.println(toKey.toString());


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
        Cipher cipher2 = null;
        //instantiate cipher with RSA
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
            cipher2 = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        //assign public key to cipher
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipher2.init(Cipher.ENCRYPT_MODE, toKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //encrypt username and password
        encrypted = null;
        encrypted1 = null;
        encrypted2 = null;
        encrypted3 = null;
        try {
            encrypted = cipher.doFinal(username.getBytes());
            encrypted1 = cipher.doFinal(sendingTo.getBytes());
            encrypted2 = cipher2.doFinal(message.getBytes());
            encrypted3 = cipher.doFinal(sessionId.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        //encode username and password
        String str = null;
        String str1 = null;
        String str2 = null;
        String str3 = null;
        try {
            str = Base64.encodeToString(encrypted, Base64.DEFAULT);
            str1 = Base64.encodeToString(encrypted1, Base64.DEFAULT);
            str2 = Base64.encodeToString(encrypted2, Base64.DEFAULT);
            str3 = Base64.encodeToString(encrypted3, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //remove newlines from username and password
        str.replaceAll("\\n", "");
        str1.replaceAll("\\n", "");
        str2.replaceAll("\\n", "");
        str3.replaceAll("\\n", "");

        try {
            url = new URL("http://54.218.252.173/sms/sendmessage2.php");
            String param = "user=" + URLEncoder.encode(str, "UTF-8") +
                    "&sessionid=" + URLEncoder.encode(str3, "UTF-8") +
                    "&getuser=" + URLEncoder.encode(str1,"UTF-8") +
                    "&message=" + URLEncoder.encode(str2, "UTF-8");

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
            System.out.println("MADE IT:" +param);

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String[] myArr;
            InputStream inStream = conn.getInputStream();
            Scanner s = new Scanner(inStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            System.out.println(result);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
