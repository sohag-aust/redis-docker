package com.app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtUtil {

    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds; // 60 minutes

    @Value("${security.jwt.refresh-token.expire-length}")
    private long refreshValidityInMilliseconds; // 12 hours

    private String SECRET_KEY = "secret";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails, String type) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", type);
        return createToken(extraClaims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> extraClaims, String subject) {

        Date issuedAt = new Date();
        Date expiredAt;

        if(extraClaims.get("type").equals("refresh")){
            expiredAt = new Date(issuedAt.getTime() + refreshValidityInMilliseconds);
        }else{
            expiredAt = new Date(issuedAt.getTime() + validityInMilliseconds);
        }

        Claims claims = Jwts.claims().setSubject(subject).setId(UUID.randomUUID().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .addClaims(extraClaims)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // from prev project
    public String getJtiFromJwt(String token) {
        // Validation occurs here automatically when `parseClaimsJws` is invoked.
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.getId();
    }

    // from prev project
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
//        Map<String, String[]> queryParameters = getQueryParameters(request);
//        if (queryParameters.containsKey("token") && queryParameters.get("token").length == 1) {
//            String token = queryParameters.get("token")[0];
//            if (StringUtils.hasText(token)) {
//                return token;
//            }
//        }
        return null;
    }

    // from prev project
//    private static Map<String, String[]> getQueryParameters(HttpServletRequest request) {
//        Map<String, String[]> queryParameters = new HashMap<>();
//        String queryString = request.getQueryString();
//
//        if (StringUtils.isEmpty(queryString)) {
//            return queryParameters;
//        }
//
//        String[] parameters = queryString.split("&");
//
//        for (String parameter : parameters) {
//            String[] keyValuePair = parameter.split("=");
//            String[] values = queryParameters.get(keyValuePair[0]);
//            values = ArrayUtils.add(values, keyValuePair.length == 1 ? "" : keyValuePair[1]); //length is one if no value is available.
//            queryParameters.put(keyValuePair[0], values);
//        }
//        return queryParameters;
//    }
}
