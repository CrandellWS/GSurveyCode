package videoworker;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by Martin on 24.10.2015.
 */
public class VWConfiguration extends Configuration {

    @Valid
    @NotNull
    private String videoDirName;

    @Valid
    @NotNull
    private String gsServiceURL;

    @Valid
    @NotNull
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClientConfiguration=new JerseyClientConfiguration();

    @Valid
    @NotNull
    private int serverID;

    private String jwtkey=null;

    @NotNull
    private String ffmpeg; //Path to the ffmpeg executable

    @NotNull
    private String ffprobe; //Path to the ffprobe executable

    public void setVideoDirName(String videoDirName) {
        this.videoDirName = videoDirName;
    }

    public String getVideoDirName() {
        return videoDirName;
    }

    public String getGsServiceURL() {
        return gsServiceURL;
    }

    public void setGsServiceURL(String gsServiceURL) {
        this.gsServiceURL = gsServiceURL;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }

    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClientConfiguration) {
        this.jerseyClientConfiguration = jerseyClientConfiguration;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public String getJwtkey() {
        return jwtkey;
    }

    public void setJwtkey(String jwtkey) {
        this.jwtkey = jwtkey;
    }

    public String getFfmpeg() {
        return ffmpeg;
    }

    public void setFfmpeg(String ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    public String getFfprobe() {
        return ffprobe;
    }

    public void setFfprobe(String ffprobe) {
        this.ffprobe = ffprobe;
    }
}
