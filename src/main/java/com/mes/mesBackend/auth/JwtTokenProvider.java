package com.mes.mesBackend.auth;

import com.mes.mesBackend.entity.enumeration.UserType;
import com.mes.mesBackend.exception.CustomJwtException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("{jwt.token.secret-key}")
    private String secretKey;

    private static final Long ACCESS_TOKEN_EXPIRE_TIME = 24 * 60 * 60 *  1000L;          // 24시간
    private static final Long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;      // 7일
    private static final String ROLES = "roles";

//    private static final Long ACCESS_TOKEN_EXPIRE_TIME = 60 * 1000L;          // 1분
//    private static final Long REFRESH_TOKEN_EXPIRE_TIME = 120 * 1000L;      // 2분

    // 객체 초기화, secretKey를 Base64로 인코딩한다.
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // AccessToken 생성
    public String createAccessToken(Authentication authentication) {
        Long now = new Date().getTime();

        JwtBuilder builder = Jwts.builder();

        // 권한 가져오기
        String authority = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse(UserType.NEW.getAuthority());

        // Header
        builder.setHeaderParam("typ", "JWT");                         // "typ": JWT
        builder.signWith(SignatureAlgorithm.HS256, secretKey);                     // "alg": HS256

        // payLoad
        builder.claim(ROLES, authority);                                  // "roles": "ROLE_USER"
        builder.setSubject(authentication.getName());                             // "sub": userCode
        builder.setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME));          // "exp": 1516239022
        builder.setIssuedAt(new Date());                                          // "iss": 15162120  토큰 발행시간 정보

        return builder.compact();
    }

//
//    {
//        "sub": "heeeun",
//        "exp": 1638322318,
//        "iat": 1638322298
//    }
    // RefreshToke 생성
    public String createRefreshToken() {
        Long now = new Date().getTime();
        JwtBuilder builder = Jwts.builder();

        // Header
        builder.setExpiration(new Date((now + REFRESH_TOKEN_EXPIRE_TIME)));
        builder.signWith(SignatureAlgorithm.HS256, secretKey);
        return builder.compact();
    }

    // 토큰 복호화
    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthenticationFromAccessToken(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        // claims 에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(ROLES).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        System.out.println(authorities.stream().map(GrantedAuthority::getAuthority).findFirst());
        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // token 의 유효성 + 만료일자 확인
    public Boolean validateToken(String token, String tokenName) throws CustomJwtException {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            throw new CustomJwtException(ex.getMessage());
        } catch (MalformedJwtException ex) {
            // jwt 형식에 맞지 않을때
            throw new CustomJwtException("JWT " + tokenName + " is malformed.");
        } catch (ExpiredJwtException ex) {
            // 토큰 기간 만료
            throw new CustomJwtException("JWT " + tokenName + " is expired.");
        } catch (IllegalArgumentException ex) {
            // 토큰이 null 이거나 empty 일때
            throw new CustomJwtException("JWT " + tokenName + " token is null or empty.");
        } catch (SignatureException ex) {
            // JWT 서명이 로컬 서명과 일치하지 않을때
            throw new CustomJwtException("JWT " + tokenName + " signature does not match signature.");
        }
    }
}
