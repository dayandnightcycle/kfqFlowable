package com.xazktx.flowable.util;

import com.xazktx.flowable.base.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class TokenUtils {

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long expTime = 3600 * 10 * 1000;

    private TokenUtils() {
    }

    public static String getToken(String userId) {
        String token = Jwts.builder()
                .setId(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expTime))
                .signWith(key)
                .compact();
        return token;
    }

    private static Claims getTokenBody(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getUserId(String token) throws BizException {
        try {
            return getTokenBody(token).getId();
        } catch (ExpiredJwtException exception) {
            throw new BizException(-2, "token过期");
        } catch (JwtException exception) {
            throw new BizException(-1, "token验证失败");
        }
    }

}
