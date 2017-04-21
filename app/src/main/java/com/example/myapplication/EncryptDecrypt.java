package com.example.myapplication;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

//import org.apache.commons.codec.binary.Base64;
import java.util.Base64;
//import java.util.logging.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

    public class EncrypDecrypt {


        private static PublicKey getPublicKeyFromPemFormat(String PEMString,
                                                           boolean isFilePath) throws IOException, NoSuchAlgorithmException,
                InvalidKeySpecException {

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


