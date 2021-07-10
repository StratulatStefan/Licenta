package com.rest_api.services;

import com.rest_api.interfaces.PasswordEncrypter;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * <ul>
 *  <li>Clasa definita pentru a expune totalitatea operatiilor necesare procesului de criptare si decriptare
 *      a parolei utilizatorului</li>
 *  <li>Va implementa clasa <strong>PasswordEncrypter</strong> care va defini toate operatiile disponibile</li>
 *  <li>Se va adauga adnotarea <strong>@Service</strong> pentru a indica faptul ca aceasta clasa reprezinta un serviciu,
 *      ce va trebui <strong>injectat</strong> in componenta de tip <strong>controller</strong></li>
 * </ul>
 */
@Service
public class PasswordEncrypterService implements PasswordEncrypter {
    /**
     * <ul>
     *     <li>Obiectul de tip encoder Base64</li>
     *     <li>Va avea o instanta unica</li>
     * </ul>
     */
    private static Base64.Encoder encoder;

    /**
     * <ul>
     *     <li>Obiectul de tip decoder Base64</li>
     *     <li>Va avea o instanta unica</li>
     * </ul>
     */
    private static Base64.Decoder decoder;

    /**
     * <ul>
     *  <li>Clasa care va genera un <strong>encoder</strong> de tip Base64</li>
     *  <li>Encoderul va avea o singura instanta, deci se asigura folosirea instantei existente</li>
     * </ul>
     */
    @Override
    public Base64.Encoder getEncoder() {
        if (encoder == null)
            encoder = Base64.getEncoder();
        return encoder;
    }

    /**
     * <ul>
     *  <li>Clasa care va genera un <strong>decoder</strong> de tip Base64</li>
     *  <li>Decoderul va avea o singura instanta, deci se asigura folosirea instantei existente</li>
     * </ul>
     */
    @Override
    public Base64.Decoder getDecoder(){
        if(decoder == null)
            decoder = Base64.getDecoder();
        return decoder;
    }

    /**
     * <ul>
     *     <li>Functie care va prelucra cheia secreta astfel incat sa se genereze un hash unic, de dimensiune fixa</li>
     *     <li>Se va folosi un algoritm specific pentru algoritmul de calculare a hash-ului (SHA-1, SHA-256, etc)</li>
     *     <li>Se va genera o cheie specifica algoritmului ce se va folosi pentru criptare/decriptare</li>
     * </ul>
     * @param myKey Cheia definita la nivelul configuratiei sistemului.
     */
    @Override
    public SecretKeySpec generateSecretKey(String myKey){
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            key = MessageDigest.getInstance("SHA-256").digest(key);
            return new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Error occured while generating the secret key : " + e.getMessage());
            return null;
        }
    }

    /**
     * <ul>
     *     <li>Functia de criptare a unui sir de caracter</li>
     *     <li>Se va folosi un algoritm de tip cheie simetrica</li>
     *     <li>Se va furniza cheia secreta, care va intra in procesul de criptare</li>
     * </ul>
     * @param strToEncrypt Sirul ce se doreste a fi criptat
     * @param secret Cheia secreta de criptare.
     * @return Rezultatul criptarii.
     */
    @Override
    public String encrypt(String strToEncrypt, String secret){
        try{
            SecretKeySpec secretKey = generateSecretKey(secret);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytesToEncrypt = strToEncrypt.getBytes(StandardCharsets.UTF_8);
            return getEncoder().encodeToString(cipher.doFinal(bytesToEncrypt));
        }
        catch (Exception e){
            System.out.println("Error occured while encrypting: " + e.getMessage());
            return null;
        }
    }

    /**
     * <ul>
     *     <li>Functia de decriptare a unui sir de caracter</li>
     *     <li>Se va folosi un algoritm de tip cheie simetrica</li>
     *     <li>Se va furniza cheia secreta, care va intra in procesul de decriptare</li>
     * </ul>
     * @param strToDecrypt Sirul ce se doreste a fi decriptat
     * @param secret Cheia secreta de criptare.
     * @return Rezultatul decriptarii.
     */
    @Override
    public String decrypt(String strToDecrypt, String secret){
        try{
            SecretKeySpec secretKey = generateSecretKey(secret);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytesToDecrypt = strToDecrypt.getBytes(StandardCharsets.UTF_8);
            return new String(cipher.doFinal(getDecoder().decode(bytesToDecrypt)));
        }
        catch (Exception e){
            System.out.println("Error while decrypting: " + e.getMessage());
            return null;
        }
    }
}