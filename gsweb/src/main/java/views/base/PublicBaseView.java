package views.base;

import gamesurvey.GSApplication;
import gamesurvey.GSConfiguration;
import io.dropwizard.views.View;

import java.nio.charset.Charset;

/**
 * Created by Martin on 30.12.2015.
 */
public abstract class PublicBaseView extends View{
    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private GSConfiguration config;
    private String FBAppID;

    protected PublicBaseView(String templateName) {
        //force UTF-8 for all templates
        super(templateName, UTF8_CHARSET);
        this.config=GSApplication.getConfiguration();
        FBAppID=config.getFBAppID();
    }
    public String getBasePath()
    {
        return config.getBasePath();
    }

    public String getFBAppID() {
        return FBAppID;
    }
}
