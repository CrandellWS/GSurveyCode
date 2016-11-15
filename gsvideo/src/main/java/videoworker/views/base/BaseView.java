package videoworker.views.base;

import io.dropwizard.views.View;
import videoworker.VWApplication;
import videoworker.VWConfiguration;

import java.nio.charset.Charset;

/**
 * Created by Martin on 30.12.2015.
 */
public abstract class BaseView extends View{
    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");
    VWConfiguration config;

    protected BaseView(String templateName) {
        //force UTF-8 for all templates
        super(templateName, UTF8_CHARSET);
        this.config= VWApplication.getConfiguration();
    }
}
