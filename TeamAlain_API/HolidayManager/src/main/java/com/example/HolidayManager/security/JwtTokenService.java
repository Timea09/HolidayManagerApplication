package com.example.HolidayManager.security;

import com.example.HolidayManager.entity.UserType;

import com.example.HolidayManager.util.exceptions.AuthenticationCredentialsNotFoundException;
import com.example.HolidayManager.util.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * the jwt token service creates new token and validates the token given from
 * frontend
 *
 * @author cvisan
 */

@Service
@Slf4j
public class JwtTokenService {

    private static final long EXPIRATIONTIME = Duration.ofDays(3).toMillis();
    private static final String HEADER_STRING = "app-auth";
    private static final String CLAIM_USER = "id";
    private static final String CLAIM_ROLES = "roles";
    @Value("${application.secret}")
    private String secret;

    public Authentication getAuthentication(final HttpServletRequest request) {
        Authentication auth = null;
        // we still need the old header for various external services calling gfs
        final String token = Optional.ofNullable(request.getHeader(HEADER_STRING)).orElse(null);
        try {
            // use token != null && token.isBlack() if jdk11
            if (token != null && !("").equals(token)) {
                // parse the token.
                Jws<Claims> claims;
                claims = Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                        .parseClaimsJws(token);
                if (null != claims) {
                    // Check if there is a userId present
                    final String userId = Optional//
                            .ofNullable(claims.getBody().get(CLAIM_USER))//
                            .map(Object::toString)//
                            .orElseThrow(
                                    () -> new AuthenticationCredentialsNotFoundException("No username given in jwt"));
                    // check roles
                    String role = Optional//
                            .ofNullable(claims.getBody().get(CLAIM_ROLES))//
                            .map(Object::toString)//
                            .orElseThrow(
                                    () -> new AuthenticationCredentialsNotFoundException("No roles given in jwt"));

                    // [ADMIN] -> ADMIN
                    role = role.replaceAll("\\[|\\]", "");
                    ArrayList<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(role));
                    auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                }
            }

            } catch (final SignatureException | MalformedJwtException | UnsupportedJwtException ex) {
                log.error("Unsupported jwt token {} with exception {}",
                        token,
                        ex.getMessage());
                throw new JwtAuthenticationException(ex);
            } catch (final ExpiredJwtException ex) {
                log.error("Expired jwt token {}",
                        ex.getMessage());
                throw new JwtAuthenticationException(ex);
            } catch (final AuthenticationCredentialsNotFoundException ex) {
                log.error("An error occured while trying to create authentication based on jwt token, missing credentials {}",
                        ex.getMessage());
                throw new JwtAuthenticationException(ex);
            } catch (final Exception ex) {
                log.error( "Unexpected exception occured while parsing jwt {} exception: {}",
                        token,
                        ex.getMessage());
                throw new JwtAuthenticationException(ex);
            }

            log.debug("The authentication constructed by the JwtService");
            return auth;
        }

    public String createJwtToken(final Long userId,final Set<UserType> userTypes) {
        // create the jwt token
        String jwtToken;

        jwtToken = Jwts.builder()//
                .claim(CLAIM_USER, userId)//
                .claim(CLAIM_ROLES, new ArrayList<>(userTypes))//
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))//
                .signWith(SignatureAlgorithm.HS512, secret.getBytes(StandardCharsets.UTF_8))//
                .compact();//
        return jwtToken;
    }

    public Long getUserIdFromToken(final String token) {

        Long userId = null;
        try {
            final Jws<Claims> claims = Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token);
            if (null != claims) {
                userId = Long.parseLong(claims.getBody().get(CLAIM_USER).toString());
            }
        } catch (final SignatureException | MalformedJwtException | UnsupportedJwtException ex) {
            log.error("Unsupported jwt token {} with exception {}",
                    token,
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (final ExpiredJwtException ex) {
            log.error("Expired jwt token {}",
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (final AuthenticationCredentialsNotFoundException ex) {
            log.error("An error occured while trying to create authentication based on jwt token, missing credentials {}",
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (final Exception ex) {
            log.error( "Unexpected exception occured while parsing jwt {} exception: {}",
                    token,
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        }
        return userId;
    }

    public UserType getUserTypeFromToken(final String token) {

        UserType userType = null;
        try {
            final Jws<Claims> claims = Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token);
            if (null != claims) {
                String userTypeAsString = ((List<String>)(claims.getBody().get(CLAIM_ROLES))).get(0).toString();
                switch(userTypeAsString){
                    case "EMPLOYEE":
                        userType = UserType.EMPLOYEE;
                        break;
                    case "TEAM_LEAD":
                        userType = UserType.TEAM_LEAD;
                        break;
                    case "ADMIN":
                        userType = UserType.ADMIN;
                        break;
                }
            }
        } catch (final SignatureException | MalformedJwtException | UnsupportedJwtException ex) {
            log.error("Unsupported jwt token {} with exception {}",
                    token,
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (final ExpiredJwtException ex) {
            log.error("Expired jwt token {}",
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (final AuthenticationCredentialsNotFoundException ex) {
            log.error("An error occured while trying to create authentication based on jwt token, missing credentials {}",
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (final Exception ex) {
            log.error( "Unexpected exception occured while parsing jwt {} exception: {}",
                    token,
                    ex.getMessage());
            throw new JwtAuthenticationException(ex);
        }
        return userType;
    }
}
