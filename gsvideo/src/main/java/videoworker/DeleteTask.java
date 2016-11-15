package videoworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import videoworker.objects.VWVideo;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by Martin on 21.02.2016.
 */
public class DeleteTask implements Runnable {
    private final WebTarget req;
    private final static Logger logger = LoggerFactory.getLogger(DeleteTask.class);
    private final VWConfiguration config;

    public DeleteTask(WebTarget req, VWConfiguration config) {
        this.req=req;
        this.config=config;
    }

    @Override
    public void run() {
        try {
            Response response = req.path("/backend/getDeletedVideos").request(MediaType.APPLICATION_JSON_TYPE).get();
            List<VWVideo> videos = response.readEntity(new GenericType<List<VWVideo>>() {});
            for(VWVideo v:videos)
            {
                if(v.getUUID()==null||v.getUUID().length()==0||(!v.getUUID().contains("-")))
                    continue;
                File f=new File(config.getVideoDirName(),v.getUUID());
                if(f.exists()) {
                    System.out.println("deleting " + f.getAbsolutePath());
                    deleteRecursive(f);
                }
                req.path("/backend/video/"+v.getUUID()).request().delete();
            }
        }catch (Exception e)
        {
            logger.error("run",e);
        }
    }

    private void deleteRecursive(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteRecursive(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
