package com.example;

import java.io.InputStream;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;


public class main {

    //The variables will be assigned in loadProperties()
    static String certsPath = "";
    static String issuer = "";
    static String keyID = "";
    static String qlikSaaSInstance = "";
    static String qlikIntegrationID = "";


    public static void main(String[] args) throws Exception {
        
    }

    //Load application specific properties from the application.properties file under src/main>/resources
    private static void loadProperties() throws Exception {
        System.out.println("--> Loading properties from application.properties");

        try (InputStream inputStream = main.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            if (inputStream == null)
            {
               throw new IOException("File application.properties not found");
            }

            properties.load(inputStream);
            properties.forEach((key, value) -> System.out.println("Key : " + key + ", Value : " + value));

            certsPath = properties.getProperty("certificatesPath");
            issuer = properties.getProperty("issuer");
            keyID = properties.getProperty("keyID");
            qlikSaaSInstance = properties.getProperty("qlikSaaSInstance");
            qlikIntegrationID = properties.getProperty("qlikIntegrationID");

        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    //Function is called from index.jsp to retrieve the tenant URL
    public static String getQlikCloudURL() throws Exception {
        return qlikSaaSInstance;
    }

    //Function is called from index.jsp to retrieve the Qlik integration ID
    public static String getQlikIntegrationID() throws Exception {
        return qlikIntegrationID;
    }

    //Function is called from index.jsp to retrieve the JSON Web Token
    public static String getJWT() throws Exception {
       
        //Load the properties
        loadProperties();

        System.out.println("--> Loading certificates");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair;

        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey private_key = PrivateKeyReader.get(certsPath + "private_key.der");
        PublicKey public_key = PublicKeyReader.get(certsPath + "public_key.der");
        keyPair = new KeyPair(public_key, private_key);

        //Prepare the time values
        Instant instant = Instant.now();
        long creationTime = instant.getEpochSecond();
        long expTime = instant.plusSeconds(500).getEpochSecond();
        long nbfTime = instant.getEpochSecond();

        //Prepare the header
        System.out.println("--> Preparing header");
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", keyID);
        System.out.println("Header: " + header.toString());

        //Prepare the payload
        System.out.println("--> Preparing payload");
        Map<String, Object> payloadClaims = new HashMap<>();
        payloadClaims.put("sub", java.util.UUID.randomUUID().toString());
        payloadClaims.put("subType", "user");
        payloadClaims.put("name", "John Doe");
        payloadClaims.put("email", "John.Doe@example.com");
        payloadClaims.put("email_verified", true );
        payloadClaims.put("iss", issuer);
        payloadClaims.put("groups", new String[]{"Administrators", "Sales", "Marketing"});
        payloadClaims.put("aud", "qlik.api/login/jwt-session");
        payloadClaims.put("jti", java.util.UUID.randomUUID().toString());
        payloadClaims.put("iat", creationTime);
        payloadClaims.put("nbf", nbfTime);
        payloadClaims.put("exp", expTime);    
        System.out.println("Payload: " + payloadClaims.toString());

        //Create the JWT token
        System.out.println("--> Building JWT");
        Builder tokenBuilder = JWT.create()
                .withHeader(header)
                .withPayload(payloadClaims);

        String jwt = tokenBuilder
                .sign(Algorithm.RSA256(((RSAPublicKey) keyPair.getPublic()), ((RSAPrivateKey) keyPair.getPrivate())));
        
        System.out.println("JWT: " + jwt);
        return jwt;

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
