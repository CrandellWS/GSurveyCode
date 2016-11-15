package resources;

import gamesurvey.GSConfiguration;
import gamesurvey.dao.MyDAOImpl;
import objects.VWVideo;
import objects.VWVideoTask;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Martin on 29.10.2015.
 */
@Singleton
@Path("/backend")
public class BackendResource {

    private final GSConfiguration config;
    private final MyDAOImpl daoImpl;

    public BackendResource(GSConfiguration configuration, MyDAOImpl daoImpl) {
        this.config=configuration;
        this.daoImpl=daoImpl;
    }

    @PUT
    @Path("/video/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addVideo(@PathParam("uuid") String uuid, VWVideo v) {
        v.setServerID(1);
        daoImpl.insertVideo(1,v);
    }

    @PUT
    @Path("/videoTask/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void videoTask(@PathParam("uuid") String uuid,
                         VWVideoTask vt) {
        daoImpl.upsertVideoTask(1,vt);
    }

    @GET
    @Path("/getDeletedVideos")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VWVideo> getDeletedVideos() {
        return daoImpl.selectDeletedVideos();
    }

    @DELETE
    @Path("/video/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public void ackDeleteVideo(@PathParam("uuid") String uuid) {
        daoImpl.deletedVideoByUUID(uuid);
    }




}
