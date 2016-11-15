package videoworker;


import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import videoworker.resources.RootResource;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.awt.*;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Martin on 24.10.2015.
 */
public class VWApplication extends Application<VWConfiguration> {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static VWConfiguration configuration;

    static { /* force headless mode*/
        System.setProperty("java.awt.headless", "true");
        // This triggers creation of the toolkit.
        // Because java.awt.headless property is set to true, this
        // will be an instance of headless toolkit.
        Toolkit tk = Toolkit.getDefaultToolkit();
        // Standard beep is available.
        tk.beep();
        System.out.println("Headless: "+java.awt.GraphicsEnvironment.isHeadless());
    }

    public static void main(String[] args) throws Exception {
        new VWApplication().run(args);
    }

    public static VWConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getName() {
        return "GS 2.0: VideoWorker";
    }

    @Override
    public void initialize(Bootstrap<VWConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<VWConfiguration>());
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets", "index.html"));
    }

    @Override
    public void run(VWConfiguration configuration,
                    Environment environment) {
        this.configuration=configuration;
        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration())
                .build(getName());
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new  RootResource(configuration, client));

        //StreamServlet streamServlet = new StreamServlet(configuration.getVideoDirName());
        DefaultServlet streamServlet = new DefaultServlet();
        ServletRegistration.Dynamic s = environment.servlets().addServlet("Streaming", streamServlet);
        s.addMapping("/stream/*");
        s.setInitParameter("resourceBase", configuration.getVideoDirName());
        s.setInitParameter("pathInfoOnly", "true");
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("crossOriginRequsts", CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/stream/*");


        // Add additional features such as support for Multipart.
        environment.jersey().register(MultiPartFeature.class);
        WebTarget req = client.target(configuration.getGsServiceURL());

        // Enable CORS headers
        final FilterRegistration.Dynamic filter =
                environment.servlets().addFilter("CORSFilter", CrossOriginFilter.class);

        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, environment.getApplicationContext().getContextPath() + "*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");



        //Configure delete task
        DeleteTask dt=new DeleteTask(req,configuration);
        scheduler.scheduleAtFixedRate(dt, 1, 10, TimeUnit.SECONDS);

    }


}
