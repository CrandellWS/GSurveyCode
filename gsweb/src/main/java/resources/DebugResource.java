package resources;

import gamesurvey.GSConfiguration;
import gamesurvey.dao.MyDAOImpl;
import io.dropwizard.auth.Auth;
import objects.User;
import views.DebugView;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by Martin on 29.10.2015.
 */
@Singleton
public class DebugResource {

    private final GSConfiguration config;
    MyDAOImpl dao;

    public DebugResource(GSConfiguration configuration) {
        this.config=configuration;
    }
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public DebugView getDebugEditor(@Auth User user) {
        return new DebugView(user);
    }



    @POST
    @Path("/reset")
    @Produces("text/html")
    public Response reset() {
        File f=new File(config.getVideoDirName());
        for(File file: f.listFiles())
            if(file.getName().endsWith("mp4")||file.getName().endsWith("png"))
                file.delete();

        dao.reset();
        return Response.ok().status(200).entity("OK").build();
    }

}
