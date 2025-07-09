package org.slothmq.server.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slothmq.server.web.dto.JwtToken;

import java.util.Date;
import java.util.List;

public class JwtUtil {
    private static final String SECRET = "<redacted>"; //inject through Env?
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; //1 hour
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static JwtToken generateToken(String userName, String[] accessGroups) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS);
        String stringToken = JWT.create()
                .withSubject("user-auth")
                .withClaim("userName", userName)
                .withIssuedAt(new Date())
                .withExpiresAt(expireDate)
                .withAudience(accessGroups)
                .sign(ALGORITHM);

        return new JwtToken(stringToken, expireDate.toInstant().toEpochMilli());
    }

    public static List<String> verifyAccessGroups(String token) {
        return JWT.require(ALGORITHM)
                .build()
                .verify(token)
                .getAudience();
    }

    public static void verifyToken(String token) {
        JWT.require(ALGORITHM)
                .build()
                .verify(token);
    }

    public static DecodedJWT decodeToken(String token) {
        return JWT.require(ALGORITHM)
                .build()
                .verify(token);
    }
}
