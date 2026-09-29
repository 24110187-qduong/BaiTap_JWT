package vn.iotstar.baitap10.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // Tạo SecretKey từ chuỗi secret (Base64)
    private SecretKey getSignInKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            // Tạo header với thuật toán HS256
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // Tạo claims set
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + jwtExpiration));

            // Thêm extra claims
            extraClaims.forEach(claimsBuilder::claim);

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // Tạo đối tượng JWT đã ký
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // Ký với secret key
            JWSSigner signer = new MACSigner(getSignInKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi khi tạo JWT", e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    private JWTClaimsSet extractAllClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Xác thực chữ ký
            JWSVerifier verifier = new MACVerifier(getSignInKey());
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Chữ ký JWT không hợp lệ");
            }

            return signedJWT.getJWTClaimsSet();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Lỗi khi xác thực JWT", e);
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}