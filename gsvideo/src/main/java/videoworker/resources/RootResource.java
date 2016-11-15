package videoworker.resources;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import videoworker.video.VideoTask;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import videoworker.VWConfiguration;
import videoworker.objects.VWVideo;
import videoworker.vlc.VWFormat;

import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.UUID;


/**
 * Created by Martin on 24.10.2015.
 */
@Path("/")
@MultipartConfig(
        maxFileSize = 1048576000,     // 10Mb max
        fileSizeThreshold = 524288, //512 Kb before buffering to disk
        maxRequestSize = 10240      // 10Kb of meta data
)
public class RootResource {
    private final static Logger logger = LoggerFactory.getLogger(RootResource.class);

    private final VWConfiguration configuration;
    private final Client client;

    public RootResource(VWConfiguration configuration, Client client) {
        this.configuration = configuration;
        this.client = client;
    }

    @Path("/admin")
    public AdminResource getAdminResource() {
        return new AdminResource(configuration);
    }

    @Path("/videos")
    public VideoResource getVideoResource() {
        return new VideoResource(configuration);
    }

    @POST
    @Path("/uploadVideo/{jwtString}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadVideo(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @PathParam("jwtString") String jwtString) {
        try {
            Jws<Claims> jwt = Jwts.parser().setSigningKey(configuration.getJwtkey()).parseClaimsJws(jwtString);
            UUID uuid = UUID.randomUUID();
            File vidDir = new File(configuration.getVideoDirName(), uuid.toString());
            vidDir.mkdir();
            File upload = new File(vidDir, "upload.vid");
            String extension = "";
            int i = fileDetail.getFileName().lastIndexOf('.');
            if (i > 0) {
                extension = fileDetail.getFileName().substring(i + 1).toLowerCase();
            }
            if (extension.endsWith("ppt")
                    || extension.endsWith("pptx")) {
                upload = new File(vidDir, "upload." + extension);
            }
            File snapshot = new File(vidDir, "snapshot.png");

            // save it
            writeToFile(uploadedInputStream, upload);
            String output = "File uploaded to: " + upload.getAbsolutePath().toString();

            WebTarget req = client.target(configuration.getGsServiceURL());
            VWVideo v = new VWVideo();
            v.setUUID(uuid.toString());
            v.setVideoStatus("VWVideoStatus.PROCESSING");
            v.setUserEmail(jwt.getBody().getSubject());
            v.setName(fileDetail.getFileName());

            req.path("/backend/video/" + v.getUUID()).request().put(Entity.entity(v, MediaType.APPLICATION_JSON));
            VideoTask vt = new VideoTask();
            vt.transcode(configuration.getVideoDirName(), v, VWFormat.FORMAT_480P, req);
            vt.snapshot(configuration.getVideoDirName(), v);
            return Response.status(200).entity(output).build();

        } catch (SignatureException e) {
            return Response.status(403).build();

        }
    }

    private static String getUniqueFileName() {
        return new StringBuilder().append("video_")
                .append(System.currentTimeMillis()).append(UUID.randomUUID())
                .append(".").toString();
    }

    private static String getUniqueFileName(String directory, String extension) {
        return new File(directory, new StringBuilder().append("video")
                .append(System.currentTimeMillis()).append(UUID.randomUUID())
                .append(".").append(extension).toString()).getAbsolutePath();
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
                             File uploadedFile) {
        OutputStream out=null;
        try {

            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(uploadedFile);
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            logger.warn("writeToFile failed",e );
        }
        finally {
            if(out!=null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
