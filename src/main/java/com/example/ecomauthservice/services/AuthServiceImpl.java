package com.example.EcomAuthService.services;

import com.example.EcomAuthService.exceptions.IncorrectPasswordException;
import com.example.EcomAuthService.exceptions.UserAlreadyExistsException;
import com.example.EcomAuthService.exceptions.UserNotFoundException;
import com.example.EcomAuthService.models.Session;
import com.example.EcomAuthService.models.SessionStatus;
import com.example.EcomAuthService.models.User;
import com.example.EcomAuthService.repositories.SessionRepository;
import com.example.EcomAuthService.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthServiceImpl implements IAuthService{

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SecretKey secretKey = Jwts.SIG.HS256.key().build();


    public AuthServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sessionRepository = sessionRepository;
    }


    @Override
    public Boolean signUp(String email, String password) throws UserAlreadyExistsException {
        // 1st have to check weather this user is already exist in db.
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            throw new UserAlreadyExistsException("User with email: " + email + " already exists");
        }
        // Create User since user does not exists
        User user = createUser(email, password);
        userRepository.save(user);
        return true;
    }

    @Override
    public String login(String email, String password) throws UserNotFoundException, IncorrectPasswordException {
        // Tasks:-
        // check if user with this email id exists
        // if not --> UserNotFoundException
        // if yes --> verify the pwd of user
        // if it's wrong --> WrongPasswordException
        // if it's right --> generate the token & store it in the DB. and send back the token

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) throw new UserNotFoundException("User with email: " + email + " not found");

        Boolean isValidPassword = bCryptPasswordEncoder.matches(password, optionalUser.get().getPassword());

        if (!isValidPassword) throw new IncorrectPasswordException("Incorrect password");

        String token = createJwtToken(optionalUser.get().getId(), new ArrayList<>(), optionalUser.get().getEmail());

        // Storing token in the DB
        Session session = new Session();
        session.setToken(token);
        session.setUser(optionalUser.get());
        session.setSessionStatus(SessionStatus.ACTIVE);

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date endDate = calendar.getTime();
        session.setExpiringAt(endDate);

        sessionRepository.save(session);

        return token;
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        String generatedPassword = generatePassword(password);
        user.setPassword(generatedPassword);
        return user;
    }

    private String generatePassword(String password) {
        // we have to encrypt the password before storing into the DB.
        return bCryptPasswordEncoder.encode(password);
    }

    private String createJwtToken(Long userId, List<String> roles, String email){
        Map<String, Object> dataInJwt = new HashMap<>();
        dataInJwt.put("user_id", userId);
        dataInJwt.put("roles", roles);
        dataInJwt.put("email", email);

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date datePlus30Days = calendar.getTime();

        String token = Jwts.builder()
                .claims(dataInJwt)
                .expiration(datePlus30Days)
                .issuedAt(new Date())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public boolean validate(String token) {
        try{
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            Date expiryAt = claims.getPayload().getExpiration();
            Long userId = claims.getPayload().get("user_id", Long.class);

        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
