package com.example;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;


public class main {
    static String certsPath = "C:\\jsp\\demo\\certs\\";
    static String issuer ="test2";
    static String keyID = "test2";
    static String expiresIn="30s";
    static String notBefore= "0s";
    static String QlikSaaSInstance = "ebv2801full.eu.qlikcloud.com";
    static String QlikIntegrationID = "iIYLxGEXTpeCYDR1DIoKLgRWoRKK3bp6";


    public static void main(String[] args) throws Exception {
  
    }

    public static String getQlikCloudURL() throws Exception {
        return QlikSaaSInstance;
    }


    public static String getQlikIntegrationID() throws Exception {
        return QlikIntegrationID;
    }


    public static String getJWT() throws Exception {
       
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair;

        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey private_key = PrivateKeyReader.get(certsPath + "private_key.der");
        PublicKey public_key = PublicKeyReader.get(certsPath + "public_key.der");
        keyPair = new KeyPair(public_key, private_key);

        
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", "SomeSampleSeedValue1");
        claims.put("subType", "user");
        claims.put("name", "John Doe");
        claims.put("email", "JohnD@john.com");
        claims.put("iss", issuer);
        claims.put("aud", "qlik.api/login/jwt-session");
        claims.put("jti", generateRandomString(32));
        
        

        Map<String, Object> header = new HashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", keyID);
        header.put("expiresIn", expiresIn);
        header.put("notBefore", notBefore);

        Builder tokenBuilder = JWT.create()
                .withHeader(header)
                .withIssuer(issuer)
                .withClaim("email_verified", true)
                .withArrayClaim("groups", new String[]{"Administrators", "Sales", "Marketing"})    
                .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
                .withIssuedAt(Date.from(Instant.now()));

        claims.entrySet().forEach(action -> tokenBuilder.withClaim(action.getKey(), action.getValue()));

        String jwt = tokenBuilder
                .sign(Algorithm.RSA256(((RSAPublicKey) keyPair.getPublic()), ((RSAPrivateKey) keyPair.getPrivate())));
        
        System.out.println(jwt);
                return jwt;

    }


 
public static String generateRandomString(int length) {
 
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = length;
    Random random = new Random();
    StringBuilder buffer = new StringBuilder(targetStringLength);
    for (int i = 0; i < targetStringLength; i++) {
        int randomLimitedInt = leftLimit + (int) 
          (random.nextFloat() * (rightLimit - leftLimit + 1));
        buffer.append((char) randomLimitedInt);
    }
    String generatedString = buffer.toString();

    System.out.println(generatedString);
    return generatedString;
}

    public static class PrivateKeyReader {
        public static PrivateKey get(String filename)
                throws Exception {

            byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }

    public static class PublicKeyReader {
        public static PublicKey get(String filename)
                throws Exception {

            byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
    }

}
