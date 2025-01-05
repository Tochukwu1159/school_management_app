package examination.teacherAndStudents.Security;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import examination.teacherAndStudents.dto.SchoolDto;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.utils.SubscriptionType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {
    private final ObjectMapper objectMapper;
        private static final String SECRET_KEY = "59703373357638792F423F4528482B4D6251655468576D5A7134743777397A24";

    public JwtUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }


        public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        private Claims extractAllClaims(String token){
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        }
        private Key getSignInKey(){
            byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    public String generateToken(String email, School school) throws JsonProcessingException {
        Map<String, Object> extraClaims = new HashMap<>();
        SchoolDto schoolDto = new SchoolDto(school.getId(), school.getSchoolName(),school.getSelectedServices(), school.getSubscriptionExpiryDate(),school.getSchoolAddress(), school.getPhoneNumber(),school.getSubscriptionKey(), school.getSubscriptionType());
        String serializedSchool = objectMapper.writeValueAsString(schoolDto);
        extraClaims.put("school", serializedSchool);
        return generateToken(extraClaims, email);
    }

        public String generateToken(Map<String,Object> extraClaims
                , String email){
            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(email)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 40000 * 60 * 24))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        }

        public boolean isTokenValid(String token, UserDetails userDetails){
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }

        public boolean isTokenExpired(String token) {
            return extractExpiration(token).before(new Date());
        }

        private Date extractExpiration(String token) {
            return extractClaim(token, Claims::getExpiration);
        }
    }


