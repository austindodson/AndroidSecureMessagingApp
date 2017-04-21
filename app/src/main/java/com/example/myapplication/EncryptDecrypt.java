package com.example.myapplication;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

//import org.apache.commons.codec.binary.Base64;
import android.util.Base64;
//import java.util.logging.*;
import android.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

    public class EncryptDecrypt {


        private static PublicKey getPublicKeyFromPemFormat(String PEMString,
                                                           boolean isFilePath) throws IOException, NoSuchAlgorithmException,
                InvalidKeySpecException {

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL("http://54.218.252.173/sms/getkey.php");

                //String urlParameters = ("user=" + username + "&" + "pass=" + password);
                //byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                //int postDataLength = postData.length;

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("charset", "utf-8");
                connection.setUseCaches(false);


                InputStream content = (InputStream) connection.getInputStream();
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String newString = result.toString();
            System.out.println(newString);

            BufferedReader pemReader = null;
            if (isFilePath) {
                pemReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(PEMString)));
            } else {
                pemReader = new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(PEMString.getBytes("UTF-8"))));
            }
            StringBuffer content = new StringBuffer();
            String line = null;
            while ((line = pemReader.readLine()) != null) {
                if (line.indexOf("-----BEGIN PUBLIC KEY-----") != -1) {
                    while ((line = pemReader.readLine()) != null) {
                        if (line.indexOf("-----END PUBLIC KEY") != -1) {
                            break;
                        }
                        content.append(line.trim());
                    }
                    break;
                }
            }
            if (line == null) {
                throw new IOException("PUBLIC KEY" + " not found");
            }
            Log.i("PUBLIC KEY: ", "PEM content = : " + content.toString());

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(content.toString(), Base64.DEFAULT)));

        }







        public static String getContentWithPublicKeyFromPemFormat(String PEMString,
                                                                  String content,boolean isFilePath) throws NoSuchAlgorithmException,
                InvalidKeySpecException, IOException, NoSuchProviderException,
                NoSuchPaddingException, InvalidKeyException,
                IllegalBlockSizeException, BadPaddingException {

            PublicKey publicKey = getPublicKeyFromPemFormat(PEMString,isFilePath);
            if (publicKey != null)
                Log.i("PUBLIC KEY: ", "FORMAT : " + publicKey.getFormat()
                        + " \ntoString : " + publicKey.toString());

            byte[] contentBytes = Base64.decode(content, Base64.DEFAULT);
            byte[] decoded = null;

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");//BC=BouncyCastle Provider
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            decoded = cipher.doFinal(contentBytes);
            return new String(decoded, "UTF-8");
        }
    }


