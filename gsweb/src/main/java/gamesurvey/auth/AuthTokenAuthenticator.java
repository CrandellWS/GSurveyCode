package gamesurvey.auth;

import gamesurvey.GSApplication;
import gamesurvey.GSConfiguration;
import gamesurvey.dao.MyDAOImpl;
import io.dropwizard.auth.AuthenticationException;

import io.dropwizard.auth.Authenticator;
import io.jsonwebtoken.*;
import objects.User;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthTokenAuthenticator implements Authenticator<String, User> {
    private final static Logger log = LoggerFactory.getLogger(AuthTokenAuthenticator.class);
    private final MyDAOImpl dao;
    private final GSConfiguration config;
    private final JwtParser jwt;

    public AuthTokenAuthenticator(){
        this.dao=GSApplication.getDao();
        this.config=GSApplication.getConfiguration();
        jwt = Jwts.parser().setSigningKey(Base64.decode(config.getJwtkey().getBytes()));
    }

    @Override
    public java.util.Optional<User> authenticate(String authToken) throws AuthenticationException {
        Jws<Claims> claims = jwt.parseClaimsJws(authToken);
        String email=claims.getBody().getSubject();
        User user=dao.selectUserByEmail(email);
        return java.util.Optional.ofNullable(user);

//        String email = "";
//        try {
//            email = jwtReaderService.getEmailFromJwt(authToken);
//        } catch (InvalidJwtException e) {
//            throw new AuthenticationException(e);
//        }
//        if (StringUtils.isBlank(email)) {
//            LOG.error("Email is blank.");
//            return Optional.absent();
//        } else {
//            User user = userDao.findUserByEmail(email);
//            return Optional.fromNullable(user);
//        }
    }
}