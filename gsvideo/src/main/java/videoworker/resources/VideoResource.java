package videoworker.resources;

import videoworker.MediaStreamer;
import videoworker.VWConfiguration;
import videoworker.vlc.VWFormat;


import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

/**
 * Created by Martin on 29.10.2015.
 */
public class VideoResource {

    final int chunk_size = 1024 * 1024; // 1MB chunks
    private final VWConfiguration config;

    public VideoResource(VWConfiguration config) {
        this.config=config;
    }

    //A simple way to verify if the server supports range headers.
    @HEAD
    @Path("/stream/{id}")
    @Produces("video/mp4")
    public Response header(@PathParam("id") String id) {
        File f=new File(config.getVideoDirName(),id);
        return Response.ok().status(206).header(HttpHeaders.CONTENT_LENGTH, f.length()).build();
    }

    @GET
    @Path("/stream/{uuid}")
    @Produces("video/mp4")
    public Response streamVideo(@HeaderParam("Range") String range, @PathParam("uuid") String uuid) throws Exception {
        File f=new File(config.getVideoDirName(), uuid);
        f=new File(f,VWFormat.FORMAT_720P.toString()+".mp4");
        return buildStream(f, range);
    }


    /**
     * Adapted from http://stackoverflow.com/questions/12768812/video-streaming-to-ipad-does-not-work-with-tapestry5/12829541#12829541
     *
     * @param asset Media file
     * @param range range header
     * @return Streaming output
     * @throws Exception IOException if an error occurs in streaming.
     */
    private Response buildStream(final File asset, final String range) throws Exception {
        // range not requested : Firefox, Opera, IE do not send range headers
        if (range == null) {
            StreamingOutput streamer = new StreamingOutput() {
                public void write(final OutputStream output) throws IOException, WebApplicationException {

                    final FileChannel inputChannel = new FileInputStream(asset).getChannel();
                    final WritableByteChannel outputChannel = Channels.newChannel(output);
                    try {
                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                    } finally {
                        // closing the channels
                        inputChannel.close();
                        outputChannel.close();
                    }
                }
            };
            return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
        }

        String[] ranges = range.split("=")[1].split("-");
        final int from = Integer.parseInt(ranges[0]);
        /**
         * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
         */
        int to = chunk_size + from;
        if (to >= asset.length()) {
            to = (int) (asset.length() - 1);
        }
        if (ranges.length == 2) {
            to = Integer.parseInt(ranges[1]);
        }

        final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
        final RandomAccessFile raf = new RandomAccessFile(asset, "r");
        raf.seek(from);

        final int len = to - from + 1;
        final MediaStreamer streamer = new MediaStreamer(len, raf);
        Response.ResponseBuilder res = Response.ok(streamer).status(206)
                .header("Accept-Ranges", "bytes")
                .header("Content-Range", responseRange)
                .header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
                .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
        return res.build();
    }

}
