package com.mes.mesBackend.config;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("{jwt.token.secret-key}")
    private String secretKey;

    private static final Long ACCESS_TOKEN_EXPIRE_TIME = 20 * 1000L;                // 20초
//    private static final Long REFRESH_TOKEN_EXPIRE_TIME = 2 * 60 * 1000L;      // 4분
    private static final String ROLES = "roles";

//    private static final Long ACCESS_TOKEN_EXPIRE_TIME = 2 * 10 * 1000L;                // 2분
    private static final Long REFRESH_TOKEN_EXPIRE_TIME = 4 * 60 * 1000L;      // 4분
//    private static final Long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;      // 7일

    // 객체 초기화, secretKey를 Base64로 인코딩한다.
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // AccessToken 생성
    public String createAccessToken(Authentication authentication) {
        Long now = new Date().getTime();

        JwtBuilder builder = Jwts.builder();

        // Header
        builder.setHeaderParam("typ", "JWT");       // header "typ": JWT
        builder.signWith(SignatureAlgorithm.HS256, secretKey);  // header "alg": HS256

        // payLoad
//        builder.claim(ROLES, authentication.getAuthorities().toString().split(","));      // "roles": "ROLE_USER"
        builder.setSubject(authentication.getName());       // "sub": userCode
        builder.setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME));    // "exp": 1516239022
        builder.setIssuedAt(new Date());            // "iss": 15162120  토큰 발행시간 정보

        String accessToken = builder.compact();
        return accessToken;
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
        Collection<? extends GrantedAuthority> authorities =
                Collections.emptyList();
//                Arrays.stream(claims.get(ROLES).toString().split(","))
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList());

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
    public Boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.info("잘못된 JWT 서명입니다.");

            System.out.println("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException ex) {
            log.info("잘못된 JWT 서명입니다.");
            log.error("잘못된 JWT 서명입니다.");
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException ex) {
            try {
                throw new com.mes.mesBackend.exception.ExpiredJwtException("만료된 토큰임 !!!!!!!!!!!!!");
            } catch (com.mes.mesBackend.exception.ExpiredJwtException e) {
                e.printStackTrace();
            }
            log.info("만료된 JWT 토큰입니다.");
            log.error("만료된 JWT 토큰입니다.");
            System.out.println("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException ex) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException ex) {
            log.info("JWT 토큰이 잘못되었습니다.");
            System.out.println("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

//    private Authentication getAuthentication(HttpServletRequest request) {
//        String authorization = request.getHeader(JwtProperties.HEADER_STRING);
//        if(authorization == null) {
//            return null;
//        }
//
//        String token = authorization.substring(JwtProperties.TOKEN_PREFIX.length());
//
//        Claims claims = null;
//
//        try {
//            claims = jwtUtil.parseToken(token);
//        } catch (ExpiredJwtException e) {
//            e.printStackTrace();
//            request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN.getCode());
//        } catch (JwtException e) {
//            e.printStackTrace();
//            request.setAttribute("exception", ErrorCode.INVALID_TOKEN.getCode());
//        }
//
//        return new UserAuthentication(claims);
//    }

}
