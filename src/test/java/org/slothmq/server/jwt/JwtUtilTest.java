package org.slothmq.server.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.slothmq.server.web.dto.JwtToken;

import java.util.List;

public class JwtUtilTest {
    @Test
    public void givenIHaveValidUserGenerateNewJWT() {
        //given
        var userName = "lorem";
        var accessGroups = new String[]{"admin", "viewer"};
        //when
        JwtToken jwtToken = JwtUtil.generateToken(userName, accessGroups);
        DecodedJWT decodedJWT = JwtUtil.decodeToken(jwtToken.token());
        List<String> tokenAccessGroups = JwtUtil.verifyAccessGroups(jwtToken.token());
        JwtUtil.verifyToken(jwtToken.token());
        //then
        assert decodedJWT.getSubject().equals("user-auth");
        assert tokenAccessGroups.contains("admin");
        assert tokenAccessGroups.contains("viewer");
        assert decodedJWT.getExpiresAt() != null;
    }
}
