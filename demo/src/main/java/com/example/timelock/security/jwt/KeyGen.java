package com.example.timelock.security.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class KeyGen {
    public static void main(String[] args) {
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println("Generated JWT secret (Base64):");
        System.out.println(Encoders.BASE64.encode(key.getEncoded()));
    }
}

// ./mvnw -q exec:java -Dexec.mainClass="com.example.timelock.security.jwt.KeyGen"
// export JWT_SECRET=8evdgMoilLs4kfweAyXSh3LDTi0fdk6ru+d9NRpFto0=
// export JWT_ISSUER=example.com
// export JWT_EXPIRATION=3600000