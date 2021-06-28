package jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <ul>
 * 	<li>Elementul de tip JWT, folosit pentru a secura datele sensibile vehiculate in comunicarea dintre client si sistem.</li>
 * 	<li> Se va folosi si la autorizarea utilizatorului prin intermediul claim-ului <strong>role</strong>.</li>
 * 	<li> Va fi semnat cu algoritmul <strong>HS512</strong>.</li>
 * </ul>
 */
public class JWT {
    /**
     * Cheia secreta de semnare digitala.
     */
    private static final String SECRET_KEY = "SafeStorageSecretKey";
    /**
     * Algoritmul de semnare digitala.
     */
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    /**
     * Sirul de caractere care va contine token-ul efectiv.
     */
    private String jwt;

    /**
     * Constructor vid care initializeaza token-ul cu valoarea null.
     */
    public JWT(){
        this.jwt = null;
    }

    /**
     * Constructor cu parametri care initializeaza token-ul cu o valoare data.
     * @param jwt Valoarea data a token-ului.
     */
    public JWT(String jwt){
        this.jwt = jwt;
    }

    /**
     * <ul>
     * 	<li>Functia de creare a unui <strong>JWT</strong> pe baza datelor clientului.</li>
     * 	<li> Token-ul va contine <strong>id</strong>-ul, <strong>username</strong>-ul si <strong>rolul</strong> utilizatorului.</li>
     * 	<li>  Token-ul va fi semnal digital si va avea un timp de expirare.</li>
     * </ul>
     * @param id Identificatorul unic al utilizatorului
     * @param username Numele utilizatorului
     * @param userRole Rolul utilizatorului, folosit la autorizare.
     * @param sessionTime Durata unei sesiuni de comunicare cu clientul.
     */
    public JWT(int id, String username, String userRole, long sessionTime){
        byte[] apiSecretKey = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiSecretKey, signatureAlgorithm.getJcaName());
        Map<String, Object> additionalClaims = new HashMap<String, Object>(){{
            put("role", userRole);
            put("username", username);
        }};
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(String.format("%d", id))
                .setExpiration(new Date(System.currentTimeMillis() + sessionTime))
                .signWith(signatureAlgorithm, signingKey);
        jwtBuilder.addClaims(additionalClaims);
        this.jwt = jwtBuilder.compact();
    }

    /**
     * Getter pentru JWT
     * @return Sirul de caractere ce contine token-ul.
     */
    public String getJWT(){return this.jwt;}

    /**
     * <ul>
     * 	<li>Getter pentru identificatorul unic al utilizatorului.</li>
     * 	<li> Avand in vedere ca token-ul este salvat in format <strong>codificat</strong>, se va decodifica pentru a putea extrage id-ul utilizatorului.</li>
     * </ul>
     */
    public String getUserId(){
        return this.decodeJWT().get("sub").toString();
    }

    /**
     * <ul>
     * 	<li>Getter pentru rolul utilizatorului.</li>
     * 	<li> Avand in vedere ca token-ul este salvat in format <strong>codificat</strong>,
     * 	     se va decodifica pentru a putea extrage rolul utilizatorului.</li>
     * </ul>
     */
    public String getRole() { return this.decodeJWT().get("role").toString(); }

    /**
     * Functie pentru decodificarea token-ului.
     * @return Claim-uril din token (id, username, role)
     */
    public Claims decodeJWT(){
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .parseClaimsJws(this.jwt).getBody();
        return claims;
    }
}