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

public class Login extends AppCompatActivity {


    private String username;
    private String password;
    private FragmentManager manager;
    private int sessionID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login = (Button) findViewById(R.id.login_button);
       /* login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Login.this, MainActivity.class));
            }
        });
        */
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText usernameObj = (EditText) findViewById(R.id.userName);
                username = usernameObj.getText().toString();
                EditText passwordObj = (EditText) findViewById(R.id.password);
                password = passwordObj.getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    manager = getFragmentManager();
                    AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
                    alertDialogFragment.show(manager, "fragment_login_error");
                } else {
                    LoginAuthenticate(username, password);
                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra("KEY", username);
                    i.putExtra("SessionID", sessionID);
                    startActivity(i);
                }
            }
        });


    }
    public int LoginAuthenticate(String username, String password) {
        try {
            URL url = new URL("http://54.218.252.173/sms/login.php");

            String urlParameters = ("user=" + username + "&" + "pass=" + password);
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


