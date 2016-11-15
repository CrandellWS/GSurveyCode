package resources;

import gamesurvey.GSConfiguration;
import gamesurvey.dao.MyDAOImpl;
import views.su.SUView;
import views.su.UserListView;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by Martin on 29.10.2015.
 */
@Singleton
public class SUResource {

    private final GSConfiguration config;
    private final MyDAOImpl dao;

    public SUResource(GSConfiguration configuration, MyDAOImpl dao) {
        this.config=configuration;
        this.dao=dao;
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public SUView getSUView() {
        return new SUView(dao);
    }

    @GET
    @Path("/users")
    @Produces(MediaType.TEXT_HTML)
    public UserListView getUserListView(@PathParam("id") String id) {
        return new UserListView(dao);
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
