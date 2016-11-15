package gamesurvey;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Martin on 24.10.2015.
 */
public class GSConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    private String basePath = "/";
    private String jwtkey = null;
    private String FBAppID = "";
    private String FBSecureCode = "";
    private boolean forwardHttps = false;
    private boolean smtpAuth;
    private boolean startTls;
    private boolean ssl;
    private String smtpHost;
    private int smtpPort;
    private String username;
    private String password;
    private String from;

    public void setForwardHttps(boolean forwardHttps) {
        this.forwardHttps = forwardHttps;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public HttpClientConfiguration getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClientConfiguration httpClient) {
        this.httpClient = httpClient;
    }


    public boolean isStartTls() {
        return startTls;
    }

    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    public String getJwtkey() {
        return jwtkey;
    }

    public void setJwtkey(String jwtkey) {
        this.jwtkey = jwtkey;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public void setDatabase(DataSourceFactory database) {
        this.database = database;
    }

    private String videoDirName;

    public void setVideoDirName(String videoDirName) {
        this.videoDirName = videoDirName;
    }

    public String getVideoDirName() {
        return videoDirName;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Valid
    @NotNull
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @JsonProperty("httpClient")
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    @JsonProperty("httpClient")
    public void setHttpClientConfiguration(HttpClientConfiguration httpClient) {
        this.httpClient = httpClient;
    }

    public String getFBAppID() {
        return FBAppID;
    }

    public void setFBAppID(String FBAppID) {
        this.FBAppID = FBAppID;
    }

    public String getFBSecureCode() {
        return FBSecureCode;
    }

    public void setFBSecureCode(String FBSecureCode) {
        this.FBSecureCode = FBSecureCode;
    }

    public boolean isForwardHttps() {
        return forwardHttps;
    }
}
