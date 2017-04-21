package com.example.myapplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import android.content.Intent;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import javax.crypto.Cipher;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import android.util.Base64;

public class Logout extends AppCompatActivity {

    private String username;
    private String password;
    private FragmentManager manager;
    private int sessionID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button logout = (Button) findViewById(R.id.logout);
       /* login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Login.this, MainActivity.class));
            }
        });
        */
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText usernameObj = (EditText) findViewById(R.id.userName);
                username = usernameObj.getText().toString();
                EditText passwordObj = (EditText) findViewById(R.id.password);
                password = passwordObj.getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    manager = getFragmentManager();
                    AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
                    alertDialogFragment.show(manager, "fragment_logout_error");
                } else {
                    LoginAuthenticate(username, password);
                    Intent i = new Intent(Logout.this, MainActivity.class);
                    i.putExtra("KEY", username);
                    i.putExtra("SessionID", sessionID);
                    startActivity(i);
                }
            }
        });


    }

    public static String getRequest() throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL("http://54.218.252.173/sms/getkey.php");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        String[] results = result.toString().split("-----");
        return results[2];
    }

    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }
    public int LoginAuthenticate(String username, String password) {
        try {
            URL url = new URL("http://54.218.252.173/sms/login.php");
            String ServerPubkey = getRequest();
            byte[] publicBytes = Base64.decode(ServerPubkey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey ServerpubKey = keyFactory.generatePublic(keySpec);
            byte[] usernameE, passwordE;
            usernameE = encrypt(username, ServerpubKey);
            passwordE = encrypt(username, ServerpubKey);
            String urlParameters = ("user=" + usernameE + "&" + "pass=" + passwordE);
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            connection.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("SessionID")) {
                    String lines[] = line.split(" ");
                    int sessionID = Integer.parseInt(lines[1]);
                    return sessionID;
                    // System.out.println(sessionID);
                }

                //System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionID;
    }
}
