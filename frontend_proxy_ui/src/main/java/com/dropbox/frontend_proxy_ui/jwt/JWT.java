package com.dropbox.frontend_proxy_ui.jwt;

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

public class JWT {
   // private final static long expirationTime = 3600 * 1000;
    private static final String SECRET_KEY = "DropboxSecretKey";
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    private String jwt;

    public JWT(){
        this.jwt = null;
    }

    public JWT(String jwt){
        this.jwt = jwt;
    }

    public JWT(int id, String username, String userRole, long sessionTime){
        byte[] apiSecretKey = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiSecretKey, signatureAlgorithm.getJcaName());
        Map<String, Object> additionalClaims = new HashMap<>(){{
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

    public String getHeader(){
        return this.jwt.split("\\.")[0];
    }
    public String getJWT(){return this.jwt;}
    public String getPayload(){
        return this.jwt.split("\\.")[1];
    }
    public String getUserId(){
        return this.decodeJWT().get("sub").toString();
    }
    public String getJti() { return this.decodeJWT().get("jti").toString();}
    public String getRole() { return this.decodeJWT().get("role").toString(); }

    public Claims decodeJWT(){
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .parseClaimsJws(this.jwt).getBody();
        return claims;
    }

    public boolean areEqual(String jwt){
        return this.jwt.equals(jwt);
    }
    public boolean checkJTI(String jti){
        Map<String, Object> jwtDecoded = this.decodeJWT();
        return this.getJti().equals(jti);
    }
    public String checkAndGetRole(String jwt){
        JWT newJwt = new JWT(jwt);
        if(this.getRole().equals(newJwt.getRole())){
            return this.getRole();
        }
        return null;
    }
}