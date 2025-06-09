package org.slothmq.server.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.List;

public class JwtUtil {
    private static final String SECRET = "ypZAh13dlOk2YxtYRwCYUwS0KgVDr8pd"; //inject through Env?
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; //1 hour
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generateToken(String userName, String[] accessGroups) {

        return JWT.create()
                .withSubject(userName)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .withAudience(accessGroups)
                .sign(ALGORITHM);
    }

    public static void verifyToken(String token) {
        JWT.require(ALGORITHM)
                .build()
                .verify(token);
    }

    public static List<String> verifyAccessGroups(String token) {
        return JWT.require(ALGORITHM)
                .build()
                .verify(token)
                .getAudience();
    }
}
