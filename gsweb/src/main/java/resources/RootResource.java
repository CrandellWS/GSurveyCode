package resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gamesurvey.GSConfiguration;
import gamesurvey.ResponseXLS;
import gamesurvey.auth.PasswordEncryptionService;
import gamesurvey.dao.GSRandom;
import gamesurvey.dao.MyDAOImpl;
import io.dropwizard.auth.Auth;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import objects.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import views.*;

import javax.inject.Singleton;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * Created by Martin on 24.10.2015.
 */
@Singleton
@Path("/")
@MultipartConfig(
        maxFileSize=1048576000,     // 10Mb max
        fileSizeThreshold=524288, //512 Kb before buffering to disk
        maxRequestSize=10240      // 10Kb of meta data
)
public class RootResource {

    private final GSConfiguration config;
    private final MyDAOImpl dao;
    private final static Logger log = LoggerFactory.getLogger(RootResource.class);
    private final HttpClient httpClient;


    public RootResource(GSConfiguration config, MyDAOImpl dao, HttpClient httpClient)
    {
        this.config=config;
        this.dao=dao;
        this.httpClient=httpClient;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public DashboardView getDashboard(@Auth User user) {
        return new DashboardView(dao, user);
    }

    @Path("/debug")
    public DebugResource getDebugResource()
    {
      return new DebugResource(config);
    }

    @Path("/register")
    public RegisterResource getRegisterResource()
    {
        return new RegisterResource(config, dao);
    }

    @Path("/su")
    public SUResource getSUResource() {
        return new SUResource(config, dao);
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public EditorView getEditor(@Auth User user,
                                @PathParam("id") long id) {
        return new EditorView(dao,id, user);
    }
    @DELETE
    @Path("/survey/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteSurvey(@Auth User user,
                           @PathParam("id") long id,
                           @Context HttpHeaders headers,
                           String in)
    {
        dao.markSurveyDeleted(user, id);
    }

    @PUT
    @Path("/saveSurvey/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveSurvey(@Auth User user,
                           @PathParam("id") long id,
                           @Context HttpHeaders headers,
                           String in)
    {
        ObjectMapper sMapper = new ObjectMapper();
        try {
            SurveyData sd = sMapper.readValue(in, SurveyData.class);
            dao.updateSurvey(id,user.getId(),in, sd.getName());
        } catch (IOException e) {
            log.warn("survey update failed",e);
        }
    }

    @POST
    @Path("/createEmptySurvey")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String createEmptySurvey(@Auth User user,
                                    @Context HttpHeaders headers,
                                    String in)
    {
        try {
            ObjectMapper sMapper = new ObjectMapper();
            SurveyData sd = sMapper.readValue(in, SurveyData.class);
            long id=dao.insertSurvey(user, in, sd.getName());
            return String.valueOf(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-1";
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public LoginView getLoginView() {
        return new LoginView(dao);
    }



    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postLoginData(@Context UriInfo uriInfo,
                                  @FormParam("email") String email,
                                  @FormParam("password") String password) {
        PasswordData pd=dao.getEncryptedPassword(email);
        try {
            boolean auth=PasswordEncryptionService.authenticate(password,pd.getEncryptedPassword(),pd.getSalt());
            String authToken = Jwts.builder()
                    .setSubject(email)
                    .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(config.getJwtkey()))
                    .compact();

            String domain = uriInfo.getBaseUri().getHost();
            int maxAgeInSeconds = new Long(TimeUnit.DAYS.toSeconds(7)).intValue();
            NewCookie cookie = new NewCookie("authToken", authToken, "/", domain, "", maxAgeInSeconds, false);
            return Response.seeOther(URI.create(config.getBasePath()+"videoList"))
                    .cookie(cookie)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public LogoutView getLogoutView() {
        return new LogoutView(dao);
    }



    @POST
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public Response logout(@Context UriInfo uriInfo) {
        String domain = uriInfo.getBaseUri().getHost();
        return Response
                .seeOther(URI.create(config.getBasePath()+"logout"))
                .header(
                        "Set-Cookie",
                        String.format("authToken=deleted;Domain=%s;Path=/;Expires=Thu, 01-Jan-1970 00:00:01 GMT",domain))
                  .build();
    }

    @POST
    @Path("/loginGoogle")
    public void loginGoogle(String token)
    {

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/loginFacebook")
    public Response loginFacebook(@Context UriInfo uriInfo, String token)
    {
        ObjectMapper mapper = new ObjectMapper();
            try {
            JsonNode root = mapper.readValue(token,JsonNode.class);
                JsonNode authResponse = root.get("authResponse");
                String userAccessToken = authResponse.get("accessToken").asText();
                int expiresIn = authResponse.get("expiresIn").asInt();
                String userID = authResponse.get("userID").asText();
                //access_token=APP_ID|APP_SECRET
                String accessToken=config.getFBAppID()+"|"+config.getFBSecureCode();
                URIBuilder builder = new URIBuilder();
                URI uri=builder.setScheme("https")
                    .setHost("graph.facebook.com")
                    .setPort(443)
                    .setPath("/debug_token")
                    .setParameter("input_token", userAccessToken)
                    .setParameter("access_token", accessToken).build();

                HttpResponse execute = httpClient.execute(
                        new HttpGet(uri));
                String res=EntityUtils.toString(execute.getEntity());
                res.toString();

                URI uriMe=builder.setScheme("https")
                        .setHost("graph.facebook.com")
                        .setPort(443)
                        .setPath("/me")
                        .setParameter("fields", "id, name, email")
                        .setParameter("access_token", userAccessToken).build();

                HttpResponse executeMe = httpClient.execute(
                        new HttpGet(uriMe));
                String resMe=EntityUtils.toString(executeMe.getEntity());
                JsonNode rootMe = mapper.readValue(resMe,JsonNode.class);
                String id=rootMe.get("id").asText();
                String name=rootMe.get("name").asText();
                String email=rootMe.get("email").asText();
                User u=dao.selectUserByEmail(email);
                if(u==null)
                    dao.insertUser(name, email, GSRandom.randomString(10).getBytes(),GSRandom.randomString(10).getBytes());

                String authToken = Jwts.builder()
                        .setSubject(email)
                        .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(config.getJwtkey()))
                        .compact();
                String domain = uriInfo.getBaseUri().getHost();
                int maxAgeInSeconds = new Long(TimeUnit.DAYS.toSeconds(7)).intValue();
                NewCookie cookie = new NewCookie("authToken", authToken, "/", domain, "", maxAgeInSeconds, false);
                return Response.ok(URI.create(config.getBasePath()+"videoList"))
                        .cookie(cookie)
                        .build();
            } catch (Exception e) {
                log.warn("login facebook failed",e);
            }
        return null;
    }

    @GET
    @Path("/videoList")
    @Produces(MediaType.TEXT_HTML)
    public VideosView getEditor(@Auth User user) {
        return new VideosView(dao, user);
    }


    @GET
    @Path("/getXLS/{id}")
    @Produces("application/vnd.ms-excel")
    public Response  getXLS(@Auth User user,
                            @PathParam("id") long survey_id) {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {
                List<objects.Response> responses = dao.selectResponseListByServeyID(survey_id);
                Survey survey=dao.selectSurvey(survey_id);
                ResponseXLS rxls=new  ResponseXLS();
                rxls.generate(responses, survey, os);
            }
        };
        Response.ResponseBuilder response = Response.ok(stream);

        response.header("Content-Disposition",
                String.format("attachment; filename=\"responses_survey_%d.xls\"",survey_id));
        return response.build();
    }

    @PUT
    @Path("/deleteVideo/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean deleteVideo(@Auth User user,
                               @PathParam("uuid") String uuid) {
        dao.markVideoDeleted(user, uuid);
        return true;
    }

    @GET
    @Path("/videoListJSON")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Video> getVideoListJSON(@Auth User user) {
        return dao.selectVideos(user);
    }

    @GET
    @Path("/stream_mpeg1/{uuid}")
    @Produces("video/mpeg")
    public Response getStreamMpeg(@PathParam("uuid") String uuid) {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge((int) TimeUnit.MINUTES.toSeconds(60));
        cacheControl.setNoCache(false);
        cacheControl.setMustRevalidate(true);
        cacheControl.setPrivate(true);
        Video v=dao.selectVideoByUUID(uuid);
        Server s=dao.selectServerByID(v.getServerId());
        URI uri=URI.create(s.getUrl()+"/stream/"+v.getUUID()+"/FORMAT_480P.mpeg");
        return Response
                .status(Response.Status.FOUND)
                .header("Accept-Ranges", "bytes")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .location(uri)
                .cacheControl(cacheControl)
                .build();
    }

    @GET
    @Path("/stream_h264/{uuid}")
    @Produces("video/mp4")
    public Response getStreamMp4(@PathParam("uuid") String uuid) {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge((int) TimeUnit.MINUTES.toSeconds(60));
        cacheControl.setNoCache(false);
        cacheControl.setMustRevalidate(true);
        cacheControl.setPrivate(true);
        Video v=dao.selectVideoByUUID(uuid);
        Server s=dao.selectServerByID(v.getServerId());
        URI uri=URI.create(s.getUrl()+"/stream/"+v.getUUID()+"/FORMAT_480P.mp4");
        return Response
                .status(Response.Status.FOUND)
                .header("Accept-Ranges", "bytes")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "*")
                .header("Access-Control-Allow-Headers", "*")
                .location(uri)
                .cacheControl(cacheControl)
                .build();
    }
    @GET
    @Path("/snapshot_last/{uuid}")
    @Produces("image/png")
    public Response getSnapshotLast(@PathParam("uuid") String uuid) {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge((int) TimeUnit.MINUTES.toSeconds(60));
        cacheControl.setNoCache(false);
        cacheControl.setMustRevalidate(true);
        cacheControl.setPrivate(true);
        Video v=dao.selectVideoByUUID(uuid);
        Server s=dao.selectServerByID(v.getServerId());
        URI uri=URI.create(s.getUrl()+"/stream/"+v.getUUID()+"/snap_last.png");
        return Response
                .status(Response.Status.FOUND)
                .header("Accept-Ranges", "bytes")
                .location(uri)
                .cacheControl(cacheControl)
                .build();
    }


    @GET
    @Path("/snapshot_first/{uuid}")
    @Produces("image/png")
    public Response getSnapshotFirst(@PathParam("uuid") String uuid) {
        final CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge((int) TimeUnit.MINUTES.toSeconds(60));
        cacheControl.setNoCache(false);
        cacheControl.setMustRevalidate(true);
        cacheControl.setPrivate(true);
        Video v=dao.selectVideoByUUID(uuid);
        Server s=dao.selectServerByID(v.getServerId());
        URI uri=URI.create(s.getUrl()+"/stream/"+v.getUUID()+"/snap_first.png");
        return Response
                .status(Response.Status.FOUND)
                .header("Accept-Ranges", "bytes")
                .location(uri)
                .cacheControl(cacheControl)
                .build();
    }

    @GET
    @Path("/getServerURL/{filename}")
    @Produces(MediaType.TEXT_HTML)
    public String getServerURL(@Auth User user,
                               @PathParam("filename") String filename)
    {
        int server_id=1;
        Server s=dao.selectServerByID(server_id);
        String jwt = Jwts.builder()
                .setSubject(user.getEmail())
                .signWith(SignatureAlgorithm.HS512, config.getJwtkey()).compact();
        String url=String.format("%s/api/uploadVideo/%s",s.getUrl(),jwt);
        return url;
    }

    @GET
    @Path("/surveyList")
    @Produces(MediaType.TEXT_HTML)
    public SurveyListView getSurveyList(@Auth User user) {
        return new SurveyListView(dao, user);
    }



    @GET
    @Path("/responseList")
    @Produces(MediaType.TEXT_HTML)
    public ResponseListView getResponseList(@Auth User user) {
        return new ResponseListView(dao,user);
    }

    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_HTML)
    public ProfileView getProfile(@Auth User user) {
        return new ProfileView(dao,user);
    }


    @GET
    @Path("/survey/{id}")
    @Produces(MediaType.TEXT_HTML)
    public SurveyView getSurvey(@PathParam("id") long id) {
        return new SurveyView(dao, id);
    }

    @GET
    @Path("/preview/{id}")
    @Produces(MediaType.TEXT_HTML)
    public SurveyView getSurvey(@Auth User user, @PathParam("id") long id) {
        return new SurveyView(dao, id, user);
    }

    @POST
    @Path("/submitResponse/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String submitResponse(@Context HttpHeaders headers,
                                 @PathParam("id") long survey_id,
                                    String in)
    {
        long id=dao.insertResponse(survey_id, in);
        return String.valueOf(id);
    }


    private static String getUniqueFileName() {
        return new StringBuilder().append("video_")
                .append(System.currentTimeMillis()).append(UUID.randomUUID())
                .append(".").toString();
    }

    private static String getUniqueFileName(String directory, String extension) {
        return new File(directory, new StringBuilder().append("video")
                .append(System.currentTimeMillis()).append(UUID.randomUUID())
                .append(".").append(extension).toString()).getAbsolutePath();
    }


}
