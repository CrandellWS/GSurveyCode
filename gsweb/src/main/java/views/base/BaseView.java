package views.base;

import gamesurvey.GSApplication;
import gamesurvey.GSConfiguration;
import gamesurvey.MD5Util;
import io.dropwizard.views.View;
import objects.User;

import java.nio.charset.Charset;

/**
 * Created by Martin on 30.12.2015.
 */
public abstract class BaseView extends View{
    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");
    GSConfiguration config;
    private final User user;

    protected BaseView(String templateName, User user) {
        //force UTF-8 for all templates
        super(templateName, UTF8_CHARSET);
        this.user=user;
        this.config=GSApplication.getConfiguration();
    }

    public String getGravatar(int size)
    {
        return String.format("//www.gravatar.com/avatar/%s?s=%d&d=retro",
                MD5Util.hex(getUser().getEmail().getBytes()),
                size);

    }


    public String getBasePath()
    {
        return config.getBasePath();
    }

    public User getUser() {
        return user;
    }
}
