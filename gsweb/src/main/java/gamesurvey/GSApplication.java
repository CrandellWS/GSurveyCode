package gamesurvey;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi.InstrumentedTimingCollector;
import gamesurvey.auth.AuthTokenAuthenticator;
import gamesurvey.auth.JWTAuthFilter;
import gamesurvey.auth.UserRoleAuthorizer;
import gamesurvey.dao.MyDAO;
import gamesurvey.dao.MyDAOImpl;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import objects.User;
import org.apache.http.client.HttpClient;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.BackendResource;
import resources.RootResource;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.EnumSet;

import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_METHODS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM;


/**
 * Created by Martin on 24.10.2015.
 */
public class GSApplication extends Application<GSConfiguration> {
    private final static Logger log = LoggerFactory.getLogger(GSApplication.class);

    private static MetricRegistry metrics;


    static { /* force headless mode*/
        System.setProperty("java.awt.headless", "true");
        System.out.println("Headless: "+java.awt.GraphicsEnvironment.isHeadless());
    }
    private static GSConfiguration configuration;

    public static GSConfiguration getConfiguration() {
        return configuration;
    }

    private static MyDAOImpl dao;

    public static MyDAOImpl getDao() {
        return dao;
    }

    public static void main(String[] args) throws Exception {
        new GSApplication().run(args);
    }

    @Override
    public String getName() {
        return "GameSurvey 2.0";
    }

    @Override
    public void initialize(Bootstrap<GSConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<GSConfiguration>());
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets", "index.html"));
        bootstrap.addBundle(new MultiPartBundle());
        metrics=bootstrap.getMetricRegistry();
    }

    @Override
    public void run(GSConfiguration configuration,
                    Environment environment) {
        this.configuration=configuration;

        if(configuration.getJwtkey()==null) {
            configuration.setJwtkey(Base64.getEncoder().encodeToString(MacProvider.generateKey(SignatureAlgorithm.HS256).getEncoded()));
            log.info("GENERATED JWT KEY: \"" + configuration.getJwtkey() + "\"");
        }

        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        jdbi.setTimingCollector(new InstrumentedTimingCollector(metrics));
        final MyDAO myDao = jdbi.onDemand(MyDAO.class);
        final MyDAOImpl myDaoImpl=new MyDAOImpl(myDao);
        this.dao=myDaoImpl;
        log.info("JWT KEY: \""+configuration.getJwtkey()+"\"");
        final HttpClient httpClient = new HttpClientBuilder(environment).using(configuration.getHttpClientConfiguration())
                .build("myHTTPClient");

        environment.jersey().register(new RootResource(configuration,myDaoImpl, httpClient));
        environment.jersey().register(new BackendResource(configuration, myDaoImpl));

        final JWTAuthFilter<User> tokenAuthFilter = new JWTAuthFilter.Builder<User>()
                .setAuthorizer(new UserRoleAuthorizer())
                .setUnauthorizedHandler(new UnauthorizedHandler() {
                    @Override
                    public Response buildResponse(String prefix, String realm) {
                        Response r=  Response
                                .seeOther(URI.create(configuration.getBasePath()+"login"))
                                .type(MediaType.TEXT_HTML_TYPE)
                                .entity("Credentials are required to access this resource.")
                                .build();
                        return r;
                    }
                })
                .setAuthenticator(new AuthTokenAuthenticator()).buildAuthFilter();
        environment.jersey().register(new AuthDynamicFeature(tokenAuthFilter));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<User>(User.class));
        if (configuration.isForwardHttps()) {
            addHttpsForward(environment.getApplicationContext());
        }

    }

    public static MetricRegistry getMetrics() {
        return metrics;
    }

    void addHttpsForward(ServletContextHandler handler) {
        handler.addFilter(new FilterHolder(new Filter() {

            public void init(FilterConfig filterConfig) throws ServletException {}

            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                StringBuffer url = ((HttpServletRequest) request).getRequestURL();
                String uri = ((HttpServletRequest) request).getRequestURI();
                if (url.toString().startsWith("http://")&&!uri.startsWith("/backend/")) {
                    String location = "https://" + url.substring("http://".length());
                    ((HttpServletResponse) response).sendRedirect(location);
                } else {
                    chain.doFilter(request, response);
                }
            }

            public void destroy() {}
        }), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

}
