package com.rest_api.interfaces;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * <ul>
 *     <li>Interfata definita pentru a expune totalitatea operatiilor necesare procesului de criptare si decriptare
 *         a parolei utilizatorului</li>
 * </ul>
 */
public interface PasswordEncrypter {
    /**
     * <ul>
     *  <li>Clasa care va genera un <strong>encoder</strong> de tip Base64</li>
     *  <li>Encoderul va avea o singura instanta, deci se asigura folosirea instantei existente</li>
     * </ul>
     */
    Base64.Encoder getEncoder();

    /**
     * <ul>
     *  <li>Clasa care va genera un <strong>decoder</strong> de tip Base64</li>
     *  <li>Decoderul va avea o singura instanta, deci se asigura folosirea instantei existente</li>
     * </ul>
     */
    Base64.Decoder getDecoder();

    /**
     * <ul>
     *     <li>Functie care va prelucra cheia secreta astfel incat sa se genereze un hash unic, de dimensiune fixa</li>
     *     <li>Se va folosi un algoritm specific pentru algoritmul de calculare a hash-ului (SHA-1, SHA-256, etc)</li>
     *     <li>Se va genera o cheie specifica algoritmului ce se va folosi pentru criptare/decriptare</li>
     * </ul>
     * @param myKey Cheia definita la nivelul configuratiei sistemului.
     */
    SecretKeySpec generateSecretKey(String myKey);

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
    String encrypt(String strToEncrypt, String secret);

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
    String decrypt(String strToDecrypt, String secret);
}

