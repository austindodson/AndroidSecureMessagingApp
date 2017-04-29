package com.example.myapplication;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import android.content.Intent;
import android.app.FragmentManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import java.util.NoSuchElementException;
import java.util.Scanner;
import android.util.Base64;

public class Login extends AppCompatActivity {

    private String username;
    private String password;
    private FragmentManager manager;
    private int sessionID = 0;
    private static PublicKey publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //login button
        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            //login click handling
            public void onClick(View v) {
                //get information from text boxes
                EditText usernameObj = (EditText) findViewById(R.id.userName);
                username = usernameObj.getText().toString();
                EditText passwordObj = (EditText) findViewById(R.id.password);
                password = passwordObj.getText().toString();
                //checking if nothing in text boxes
                if (username.isEmpty() || password.isEmpty()) {
                    manager = getFragmentManager();
                    AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
                    alertDialogFragment.show(manager, "fragment_login_error");
                    recreate();
                }
                //check if session id returns something valid
                else if ((!username.isEmpty()) && (!password.isEmpty())) {
                    sessionID = LoginAuthenticate(username, password);
                    if (sessionID == 0) {
                        manager = getFragmentManager();
                        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
                        alertDialogFragment.show(manager, "fragment_login_error");
                        recreate();
                    }

                }
                //switch screens if session id is valid
                if (sessionID > 0) {
                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra("KEY", username);
                    i.putExtra("KEY2", Integer.toString(sessionID));
                    startActivity(i);
                }
            }
        });
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

    public int LoginAuthenticate(String username, String password) {
        //hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //hack

        URL url;
        HttpURLConnection conn;
        byte[] encrypted, encrypted1;
        DataInputStream dis = null;

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
            encrypted1 = cipher.doFinal(password.getBytes());
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
        str.replaceAll("\\n", "");


        try {
            url = new URL("http://54.218.252.173/sms/login.php");
            String param = "user=" + URLEncoder.encode(str, "UTF-8") +
                    "&pass=" + URLEncoder.encode(str1, "UTF-8");

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
                //System.out.println(response);
                if (response.contains("SessionID:")) {
                    myArr = response.split("\\s+");
                    int ret = Integer.parseInt(myArr[1]);
                    //setPublicKey(username, ret, publicKey);
                    return ret;
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return 0;
        } catch (MalformedURLException ex) {
            System.out.println("Error in Malformed");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 1;
    }
}




