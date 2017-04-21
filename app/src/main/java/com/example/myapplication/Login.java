package com.example.myapplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import android.content.Intent;
import android.app.FragmentManager;
import android.graphics.Path;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.StringBuilderPrinter;
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
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import android.util.Base64;

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
                }
                else if(username.equals("1") && password.equals("1")){
                    sessionID = LoginAuthenticate("163669241865747", "HDIDUmQa7w78rS7!Q");
                    System.out.println(sessionID + "\n");
                }
                else if(username.equals("163669241865747") && password.equals("HDIDUmQa7w78rS7!Q")) {
                    sessionID = LoginAuthenticate(username, password);
                    System.out.println(sessionID + "\n");
                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra("KEY", username);
                    i.putExtra("SessionID", sessionID);
                    startActivity(i);
                }
                else {
                    manager = getFragmentManager();
                    AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
                    alertDialogFragment.show(manager, "fragment_login_error");
                    }
                }
            }
        );

        /*Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {});*/
    }

    /*public static File getRequest() throws Exception {
        String fileURL = "54.218.252.173/sms/public.der";
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = "C:" + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        return
    }*/

    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = new byte[2048];
        try {
            // get an RSA cipher object and print the provider
            //System.out.println("ERROR!!!@!@!@!@!@!@!@!@!@!@!@!@!@");
            final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
            String cText = new String(cipherText.toString());
            System.out.println("RSA: " + cText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public int LoginAuthenticate(String username, String password) {
        String newArray[] = null;
        //hack
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        //hack
        //String user = "h7ZkfWhwDdtdII1jqnAk8yGBT9q9fj8Mv2hyEbIJcke07cQF3r2VfOakAVRCcdz+8SbDkmj86qoSEVom+hZS/8ZS6bSKkDJSbPu+D1tdjaxvaIWBFurVBZR8zxz5EgSqV2hbgvk6xxHRwOFiXJwqM404wLyQ0/5Pub3AWMqCddPsGG6FJkdMiCWkh56LkoTUziYBnW93StTE3uXCgYXrJTemCLEq+ZuOQvVMAkds8DnbP24D9S3s+Q9llxm9fNpGMbr1t6MYimLKRxV0LA6czE0Sc80nWXUDgLBds3FlZWwIfDRRYVmmEiBjmG/lPm9qRMWaGuxTS6z73BxQIhYnFg==";
        //String pass = "X6UF+re0QZxCSGNuIE3RpplDfy1v71/sZgR1Kh77AMonKuQ2nouqjRr2WaRiQYqXDIx6mAklxRidBhPbazPqNbPx8i6GYWnUtfVT6CD4Sd7JaaGCOfrOu3LQVgqdK+zwdQcOec02+1EUPeN1NxtwEmke8xYYvPepgAKxU1wOTDFuQkInDQ3MbYfFwtAPLMXz5fe262islvLtQ/YbzYUxkhjU3rfOF5y/XdAZWspzuRqulbL96jmTj+1Joaab5CsQEu1htjxWqkSAodPyS8yvbu35kAaRtjAmi4mV07JBeHbKjDBRoJxP+0d172YU1KftrFGSHm6WGbg+dXo9YUHFpQ==";

        URL url;
        HttpURLConnection conn;
        byte[] encrypted, encrypted1;
        //try{
            /*url = new URL("http://54.218.252.173/sms/login.php");
            String pubkey = getRequest();

            String param = "user="+URLEncoder.encode(user, "UTF-8")+"&pass=" +URLEncoder.encode(pass, "UTF-8");
            conn=(HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setFixedLengthStreamingMode(param.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.close();

            Scanner inStream = new Scanner(conn.getInputStream());
            String line;
            while((line = inStream.nextLine()) != null) {
                if (line.contains("SessionID:")) {
                    newArray = line.split("\\s+");
                    break;
                }
            }
            return Integer.parseInt(newArray[1]);
        }
        catch(MalformedURLException ex) {
            System.out.println("Error in Malformed");
        }
        catch(UnsupportedEncodingException e){
            System.out.println("Error in unsupported");
        }
        catch(IOException e){
            System.out.println("Error in IO");
        }
        catch(Exception e){
            System.out.println("Error");
        }
        return 1;*/
            //File pubKeyFile = new File();
            DataInputStream dis = null;
            PublicKey publicKey = null;

            //int fileSize = (int) pubKeyFile.length();
            byte[] keyBytes = new byte[4096];

            try{
                BufferedInputStream bufStream = new BufferedInputStream((getApplicationContext().getResources().openRawResource(R.raw.public123)));
                bufStream.read(keyBytes, 0, keyBytes.length);
                bufStream.close();
                X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(publicSpec);
                //System.out.println(publicKey);
            } catch (FileNotFoundException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            catch(NoSuchAlgorithmException e){
                e.printStackTrace();
            }
            catch(InvalidKeySpecException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");

            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            } catch (InvalidKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            encrypted = null;
            encrypted1 = null;
            try {
                encrypted = cipher.doFinal(username.getBytes());
                encrypted1 = cipher.doFinal(password.getBytes());
            } catch (IllegalBlockSizeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BadPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String str = null;
            String str1 = null;
            try {
                //str = new String(encrypted);
                str = Base64.encodeToString(encrypted, Base64.DEFAULT);
                str1 = Base64.encodeToString(encrypted1, Base64.DEFAULT);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(str);
            System.out.println(str1);
            str.replaceAll("\\n", "");
            str.replaceAll("\\n", "");

            //URL url;
            //HttpURLConnection conn;
            try{
                //if you are using https, make sure to import java.net.HttpsURLConnection
                url = new URL("http://54.218.252.173/sms/login.php");

                //you need to encode ONLY the values of the parameters
                String param = "user=" + URLEncoder.encode(str,"UTF-8")+
                        "&pass=" +URLEncoder.encode(str1,"UTF-8");
                //System.out.println("MADE IT");

                conn=(HttpURLConnection)url.openConnection();
                //System.out.println("MADE IT");
                //set the output to true, indicating you are outputting(uploading) POST data
                conn.setDoOutput(true);
                //System.out.println("MADE IT");
                //once you set the output to true, you don’t really need to set the request method to post, but I’m doing it anyway
                conn.setRequestMethod("POST");
                //System.out.println("MADE IT");

                //Android documentation suggested that you set the length of the data you are sending to the server, BUT
                // do NOT specify this length in the header by using conn.setRequestProperty(“Content-Length”, length);
                //use this instead.
                conn.setFixedLengthStreamingMode(param.getBytes().length);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //send the POST out
               /* System.out.println("MADE IT");
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                System.out.println("MADE IT");
                out.print(param);
                System.out.println("MADE IT");
                out.close();
                System.out.println("MADE IT");*/

                //System.out.println("MADE IT");
                OutputStream os = conn.getOutputStream();
                //System.out.println("MADE IT");
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(param);
                writer.flush();
                writer.close();
                os.close();


                //System.out.println("MADE IT");
                conn.connect();
                //System.out.println("MADE IT");
                String response = "";
                String[] myArr;
                Scanner inStream = new Scanner(conn.getInputStream());
                while((response = inStream.nextLine()) != null) {
                    if(response.contains("SessionID:")){
                        myArr = response.split("\\s+");
                        //System.out.println(myArr[1]);
                        return Integer.parseInt(myArr[1]);
                    }
                }
                System.out.println("Response = " + response);

            }
            catch(MalformedURLException ex) {
                System.out.println("Error in Malformed");
            }

            catch(IOException ex){
                ex.printStackTrace();
            }

        return 1;
        }


}


