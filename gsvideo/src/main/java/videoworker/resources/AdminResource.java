package videoworker.resources;

import videoworker.VWConfiguration;
import videoworker.views.AdminView;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by Martin on 29.10.2015.
 */
@Singleton
public class AdminResource {

    private final VWConfiguration config;

    public AdminResource(VWConfiguration configuration) {
        this.config=configuration;
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    public AdminView getIndex() {
        return new AdminView();
    }



    @POST
    @Path("/resync")
    @Produces("text/html")
    public Response reset() {
        File f=new File(config.getVideoDirName());
        for(File file: f.listFiles())
            if(file.getName().endsWith("mp4")||file.getName().endsWith("png"))
                file.delete();

        return Response.ok().status(200).entity("OK").build();
    }

}
