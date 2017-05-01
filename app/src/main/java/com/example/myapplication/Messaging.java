package com.example.myapplication;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
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
    String myPrivStr;
    String alias;
    PrivateKey privateKey;
    String[] messages;
    String[] oldmessages = {"No messages"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_messaging);
        setSupportActionBar(myToolbar);

        username = getIntent().getStringExtra("USER");
        sendingTo = getIntent().getStringExtra("SEND");
        sessionid = getIntent().getStringExtra("SESH");
        myPrivStr = getIntent().getStringExtra("PRIV");
        alias = getIntent().getStringExtra("ALIAS");

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] privByte = Base64.decode(myPrivStr, Base64.DEFAULT);
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privByte));
        }
        catch(InvalidKeySpecException e){
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        //System.out.println(myPrivStr);
        getSupportActionBar().setTitle(alias);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        final ListView listView = (ListView) findViewById(R.id.MessagingList);
        messages = getMessages(username, sessionid, sendingTo, privateKey);
        ArrayList<String> list1 = new ArrayList<String>();
        if(!oldmessages[0].equals("No messages")){
            for(int a = 0; a < oldmessages.length; a++){
                list1.add(oldmessages[a]);
            }
        }
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                // User chose the "Settings" item, show the message edit screen
                //messages = getMessages(username,sessionid,sendingTo, privateKey);
                oldmessages = messages;
                recreate();
                return true;

            case android.R.id.home:
                System.out.println("MADE IT!!!!");
                Intent i = new Intent(Messaging.this, MainActivity.class);
                i.putExtra("KEY", username);
                i.putExtra("KEY2", sessionid);
                startActivity(i);

            default:
                // If we got here, the user's action was not recognized
                // Invoke the superclass to handle it.
                //System.out.println(item.getItemId());
                return super.onOptionsItemSelected(item);

        }
    }

    public String[] getMessages(String username, String sessionid, String touser, PrivateKey key){
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
        Cipher cipherDec = null;
        try {
            cipherDec = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipherDec.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }catch(InvalidKeyException e){
            e.printStackTrace();
        }


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
            encrypted2 = cipher.doFinal(touser.getBytes());
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
        str2.replaceAll("\\n", "");
        //System.out.println("GET MESSAGE PARAMS");
        //System.out.println(str);
        //System.out.println(str1);
        //System.out.println(str2);

       // RSAPublicKey somekeyfortesting =null;
        try {
           // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            url = new URL("http://54.218.252.173/sms/getmessagesfrom.php");
            String param = "user=" + URLEncoder.encode(str, "UTF-8") +
                    "&sessionid=" + URLEncoder.encode(str1, "UTF-8")+
                    "&fromuser=" + URLEncoder.encode(str2, "UTF-8");

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

            //System.out.println("MADE IT MESSAGES");

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String[] myArr = null;
            InputStream inStream2 = conn.getInputStream();
            BufferedReader bufStream = new BufferedReader(new InputStreamReader(inStream2));
            String new1 = "";


            while((response = bufStream.readLine()) != null){
                if(response.contains("Getting Messages........[OK!]") || response.contains("---- BEGIN MSG ----")){
                    continue;
                }
                if(response.contains("---- DELTIME ----")){
                    continue;
                }
                System.out.println(response);
                new1 = new1+response;
                //if (response.contains("---- MSG ----")) {
                    //System.out.println("MADE IT MESSAGES");

                      //  myArr = response.split("----");
                    //    System.out.println("HELLO +" +myArr[2]);
                  //      new1 = myArr[2];
                        //byte[] encMsg = myArr[1].getBytes();
                        //byte[] deBase = Base64.decode(encMsg, Base64.DEFAULT);
                        //byte[] decMsg = cipherDec.doFinal(encMsg);
                        //String decrypted = new String(decMsg);
                        //System.out.println(decrypted);
                        //setPublicKey(username, ret, publicKey);
                //}

                //new1.concat(response);
            }

            if (!new1.contains("---- MSG ----")){
                String[] array = {"No messages"};
                return array;
            }
            new1 = new1.replace("---- MSG ----", "");
            if(new1.contains("---- END MSG ----")) {
                myArr = new1.split("---- END MSG ----");
            }
            else{
                myArr[0] = new1;
            }
            System.out.println("MADE IT + " + new1);
            bufStream.close();
            //byte[] encMsg = new1.getBytes();
            //byte[] deBase = Base64.decode(encMsg, Base64.DEFAULT);
            String[] decArray = new String[myArr.length];
            for (int a = 0; a < myArr.length; a++) {
                byte[] decMsg = cipherDec.doFinal(Base64.decode(myArr[a], Base64.DEFAULT));
                String decrypted = new String(decMsg);
                decArray[a] = decrypted;
                System.out.println(decrypted);
            }
            return decArray;
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        //System.out.println(ret.toString());
        String[] nullArray = {""};
        return nullArray;
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
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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

            //System.out.println("MADE IT REQUEST");

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String myArr = null;
            InputStream inStream = conn.getInputStream();
            Scanner s = new Scanner(inStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            //System.out.println(result);

            result.replaceAll("\\n", "");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] publicByte = Base64.decode(result, Base64.DEFAULT);
            ret = kf.generatePublic(new X509EncodedKeySpec(publicByte));
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
        //System.out.println(toKey.toString());


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
            //System.out.println("MADE IT:" +param);

            //get session id from HTTP connection output
            conn.connect();
            String response = "";
            String[] myArr;
            InputStream inStream = conn.getInputStream();
            Scanner s = new Scanner(inStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            //System.out.println(result);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
