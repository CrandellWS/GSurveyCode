package resources;

import freemarker.template.*;
import freemarker.template.Configuration;
import gamesurvey.GSApplication;
import gamesurvey.GSConfiguration;
import gamesurvey.auth.PasswordEncryptionService;
import gamesurvey.dao.MyDAOImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import views.RegisterEmailView;
import views.RegisterView;
import views.ResetPasswordStep2View;
import views.ResetPasswordView;

import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by Martin on 29.10.2015.
 */
@Singleton
public class RegisterResource {
    private final static Logger log = LoggerFactory.getLogger(RegisterResource.class);

    private final GSConfiguration config;
    MyDAOImpl dao;

    public RegisterResource(GSConfiguration configuration, MyDAOImpl dao) {
        this.config=configuration;
        this.dao=dao;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public RegisterView getRegisterView() {
        return new RegisterView(dao);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postRegisterData(HashMap<String, String> map,
    @Context UriInfo uriInfo) {
        String email=map.get("email");
        User u=dao.selectUserByEmail(email);
        if(u!=null)
            return Response
                    .status(Response.Status.OK)
                    .entity("email exists")
                    .build();

        String error="";
        try{
        sendEmailValidation(map.get("name"), email, uriInfo);}
        catch (Exception e){
            error=e.getMessage();
        }
        return Response
                .status(Response.Status.OK)
                .entity(error)
                .build();
    }

    @GET
    @Path("/resetPassword")
    @Produces(MediaType.TEXT_HTML)
    public ResetPasswordView getResetPasswordView() {
        return new ResetPasswordView(dao);
    }


    @POST
    @Path("/resetPassword")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postResetPasswordData(HashMap<String, String> map,
                                     @Context UriInfo uriInfo) {
        String email=map.get("email");
        User u=dao.selectUserByEmail(email);
        if(u==null)
            return Response
                    .status(Response.Status.OK)
                    .entity("no such email")
                    .build();
        String error="";
        try{
            sendResetPasswordEmail(email, uriInfo, u);}
        catch (Exception e){
            log.warn("Send mail failed",e);
            error=e.getMessage();
        }
        return Response
                .status(Response.Status.OK)
                .entity(error)
                .build();
    }

    @GET
    @Path("/resetPassword/{token}")
    @Produces(MediaType.TEXT_HTML)
    public ResetPasswordStep2View getResetPasswordStep2View(@PathParam("token") String token) {
        Jws<Claims> jwt = Jwts.parser()
                .setSigningKey(config.getJwtkey())
                .parseClaimsJws(token);
        return new ResetPasswordStep2View(dao, token, jwt.getBody().getSubject());
    }

    @POST
    @Path("/resetPassword/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Response postResetPasswordStep2View(@Context UriInfo uriInfo,
                                                             @PathParam("token") String token,
                                                             @FormParam("password") String password,
                                                             @FormParam("password2") String password2) {
        Jws<Claims> jwt = Jwts.parser()
                .setSigningKey(config.getJwtkey())
                .parseClaimsJws(token);
        if(password==null||password.length()<6||!password.equals(password2))
        {
            return Response.seeOther(uriInfo.getRequestUri())
                    .build();
        }

        try {
            byte[] salt = PasswordEncryptionService.generateSalt();
            byte[] encPassword = PasswordEncryptionService.getEncryptedPassword(password, salt);
            String email=jwt.getBody().getSubject();
            dao.updatePassword(email, salt, encPassword);
            String authToken = Jwts.builder()
                    .setSubject(email)
                    .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(config.getJwtkey()))
                    .compact();
            String domain = uriInfo.getBaseUri().getHost();
            int maxAgeInSeconds = new Long(TimeUnit.DAYS.toSeconds(7)).intValue();
            NewCookie cookie = new NewCookie("authToken", authToken, "/", domain, "", maxAgeInSeconds, false);
            return Response.seeOther(URI.create(config.getBasePath()))
                    .cookie(cookie)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return Response.seeOther(URI.create(config.getBasePath()))
                .build();
    }

    @GET
    @Path("/email/{token}")
    @Produces(MediaType.TEXT_HTML)
    public RegisterEmailView getRegisterEmailView(@PathParam("token") String token) {
        Jws<Claims> jwt = Jwts.parser()
                .setSigningKey(config.getJwtkey())
                .parseClaimsJws(token);
        return new RegisterEmailView(dao, (String) jwt.getBody().get("name"), jwt.getBody().getSubject());
    }

    @POST
    @Path("/email/{token}")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postRegisterEmailData(@Context UriInfo uriInfo,
                                     @PathParam("token") String token,
                                     @FormParam("name") String name,
                                     @FormParam("password") String password,
                                     @FormParam("password2") String password2) {
        Jws<Claims> jwt = Jwts.parser()
                .setSigningKey(config.getJwtkey())
                .parseClaimsJws(token);

        if(password==null||password.length()<6||!password.equals(password2))
        {
            return Response.seeOther(uriInfo.getRequestUri())
                    .build();
        }

        try {
            byte[] salt = PasswordEncryptionService.generateSalt();
            byte[] encPassword = PasswordEncryptionService.getEncryptedPassword(password, salt);
            String fullname= (String) jwt.getBody().get("name");
            String email=jwt.getBody().getSubject();
            dao.insertUser(fullname, email, salt, encPassword);
            String authToken = Jwts.builder()
                    .setSubject(email)
                    .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(config.getJwtkey()))
                    .compact();
            String domain = uriInfo.getBaseUri().getHost();
            int maxAgeInSeconds = new Long(TimeUnit.DAYS.toSeconds(7)).intValue();
            NewCookie cookie = new NewCookie("authToken", authToken, "/", domain, "", maxAgeInSeconds, false);
            return Response.seeOther(URI.create(config.getBasePath()))
                    .cookie(cookie)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return Response.seeOther(URI.create(config.getBasePath()))
                .build();
    }

    private String sendResetPasswordEmail(String email, UriInfo uriInfo, User u) {
        String s = Jwts.builder()
                .setSubject(email)
                .signWith(SignatureAlgorithm.HS256, GSApplication.getConfiguration().getJwtkey())
                .compact();
        log.info("Sending password reset to: \"{}\"", email);
        sendResetPasswordMailTLS(u.getName(), email, s, uriInfo);
        return "";
    }

    private String sendEmailValidation(String fullname, String email, UriInfo uriInfo)
    {
        HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("name", fullname);
        String s = Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .signWith(SignatureAlgorithm.HS256, GSApplication.getConfiguration().getJwtkey())
                .compact();
        log.info("Sending validation email to: \"{}\"", email);
        sendRegistrationMailTLS(fullname, email, s, uriInfo);
        return "";
    }
    private Session emailSession(){
        Properties props = new Properties();
        props.put("mail.smtp.auth", config.isSmtpAuth());
        props.put("mail.smtp.starttls.enable", config.isStartTls());
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getUsername(), config.getPassword());
                    }
                });
        return session;
    }

    private void sendResetPasswordMailTLS(String fullname, String email, String key, UriInfo uriInfo)
    {
        try {
            Message message = new MimeMessage(emailSession());
            message.setFrom(new InternetAddress(config.getFrom()));
            InternetAddress address=new InternetAddress();
            address.setAddress(email);
            address.setPersonal(fullname);
            message.addRecipient(Message.RecipientType.TO, address);
            message.setSubject("GameSurvey 2.0 password reset");

            // freemarker stuff.
            freemarker.template.Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading(this.getClass(), "/");
            Template template = cfg.getTemplate("resetPassword-template.ftl");
            Map<String, String> rootMap = new HashMap<String, String>();
            rootMap.put("fullname", fullname);
            rootMap.put("link", uriInfo.getBaseUri().resolve("/register/resetPassword/"+key).toString());
            Writer out = new StringWriter();
            template.process(rootMap, out);
            // freemarker stuff ends.
            message.setContent(out.toString(), "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (TemplateNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRegistrationMailTLS(String fullname, String email, String key, UriInfo uriInfo)
    {
        try {
            Message message = new MimeMessage(emailSession());
            message.setFrom(new InternetAddress(config.getFrom()));
            InternetAddress address=new InternetAddress();
            address.setAddress(email);
            address.setPersonal(fullname);
            message.addRecipient(Message.RecipientType.TO, address);
            message.setSubject("GameSurvey 2.0 registration");

            // freemarker stuff.
            freemarker.template.Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading(this.getClass(), "/");
            Template template = cfg.getTemplate("register-template.ftl");
            Map<String, String> rootMap = new HashMap<String, String>();
            rootMap.put("fullname", fullname);
            rootMap.put("link", uriInfo.getBaseUri().resolve("/register/email/"+key).toString());
            Writer out = new StringWriter();
            template.process(rootMap, out);
            // freemarker stuff ends.
            message.setContent(out.toString(), "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (TemplateNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
