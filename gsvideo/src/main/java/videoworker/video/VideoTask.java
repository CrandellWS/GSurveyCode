package videoworker.video;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import videoworker.VWApplication;
import videoworker.objects.VWVideo;
import videoworker.objects.VWVideoTask;
import videoworker.vlc.TaskStatus;
import videoworker.vlc.VWFormat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin on 04.02.2016.
 */
public class VideoTask {
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final static Logger logger = LoggerFactory.getLogger(VideoTask.class);
    private WebTarget req;
    private static Pattern pDuration = Pattern.compile("^  Duration: (?<h>[0-9]*):(?<m>[0-9]*):(?<s>[0-9]*)\\.(?<hs>[0-9]*),.*");
    private static Pattern pFrame = Pattern.compile("^frame=\\s*(?<frame>[0-9]*)\\s+fps=\\s*(?<fps>[0-9]\\s*).*\\s+time=(?<h>[0-9]*):(?<m>[0-9]*):(?<s>[0-9]*)\\.(?<hs>[0-9]*).*");
    private static Pattern pNbFrames = Pattern.compile("^nb_frames=(?<frames>[0-9]*).*", Pattern.MULTILINE);
    private final static String ffmpeg_exe=VWApplication.getConfiguration().getFfmpeg();
    private final static String ffprobe_exe=VWApplication.getConfiguration().getFfprobe();



    public VideoTask() {
    }
    //faefbe8d-d772-4c00-9dd5-001c9db4be0d
    // ffmpeg -i upload.vid -vcodec h264 -acodec aac -strict -2 -vf scale=-1:480 FORMAT_480P.mp4
    public void transcode(String videoDirName, VWVideo video, VWFormat format, WebTarget target) {
        this.req=target;

        File videoDir = new File(videoDirName, video.getUUID());
        File sourceFile = new File(videoDir, "upload.vid");
        File destFileMpeg = new File(videoDir, format.toString() + ".mpeg");
        File destFileMp4 = new File(videoDir, format.toString() + ".mp4");

        VWVideoTask vt=new VWVideoTask();
        vt.setSourceFile(sourceFile.getAbsolutePath());
        vt.setDestFile(destFileMp4.getAbsolutePath());
        vt.setUUID(video.getUUID());
        vt.setTaskData(String.valueOf(format.getValue()));
        vt.setType("MP4");
        submitTranscode(video, vt);

        VWVideoTask vtMpeg=new VWVideoTask();
        vtMpeg.setSourceFile(sourceFile.getAbsolutePath());
        vtMpeg.setDestFile(destFileMpeg.getAbsolutePath());
        vtMpeg.setUUID(video.getUUID());
        vtMpeg.setTaskData(String.valueOf(format.getValue()));
        vtMpeg.setType("MPEG");
        submitTranscode(video, vtMpeg);
    }

    private void submitTranscode(final VWVideo video, final VWVideoTask vt) {
        executorService.submit(new Runnable() {
            public void run() {
                //ffmpeg -i in.mp4
                // -filter:v "scale=iw*min($width/iw\,$height/ih):ih*min($width/iw\,$height/ih), pad=$width:$height:($width-iw*min($width/iw\,$height/ih))/2:($height-ih*min($width/iw\,$height/ih))/2" out.mp4
                // ffmpeg -i upload.vid -vcodec h264 -acodec aac -strict -2 -vf scale=-1:480 FORMAT_480P.mp4
                String[] list={};
                if(vt.getType()=="MP4"){
                    String[] tmpList = {
                            ffmpeg_exe,
                            "-i", vt.getSourceFile(),
                            "-vcodec", "h264",
                            "-acodec", "aac",
                            "-strict", "-2",
                            "-vf",//"scale=-2:480",
                            MessageFormat.format("scale=iw*min({0}/iw\\,{1}/ih):ih*min({0}/iw\\,{1}/ih), "+
                                            "pad={0}:{1}:({0}-iw*min({0}/iw\\,{1}/ih))/2:({1}-ih*min({0}/iw\\,{1}/ih))/2",
                                    640,480),
                            vt.getDestFile()};
                    list=tmpList;
                }
                else if(vt.getType()=="MPEG"){
                    String[] tmpList = {
                            ffmpeg_exe,
                            "-i", vt.getSourceFile(),
                            "-f", "mpeg1video",
                            "-vf",//"scale=-2:480",
                            MessageFormat.format("scale=iw*min({0}/iw\\,{1}/ih):ih*min({0}/iw\\,{1}/ih), "+
                                            "pad={0}:{1}:({0}-iw*min({0}/iw\\,{1}/ih))/2:({1}-ih*min({0}/iw\\,{1}/ih))/2",
                                    640,480),
                            vt.getDestFile()};
                    list=tmpList;
                }

                ProcessBuilder pb = new ProcessBuilder(list);

                logger.info("Executing: "+Arrays.toString(list));
                try {
                    try {
                        vt.setTaskStatus(TaskStatus.INIT.toString());
                        vt.setProgressPercent(0);
                        req.path("/backend/videoTask/"+
                                vt.getUUID())
                                .request()
                                .put(Entity.entity(vt, MediaType.APPLICATION_JSON));
                    }
                    catch (Exception e)
                    {
                        logger.warn("pos: ",e);
                    }

                    Process p = pb.start();
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    int seconds=-1;
                    StringBuilder sb=new StringBuilder();
                    while ( (line = reader.readLine()) != null) {
                        sb.append(line);
                        if(seconds<0) {
                            Matcher matcher = pDuration.matcher(line);
                            if(matcher.matches()) {
                                int h = Integer.parseInt(matcher.group("h"));
                                int m = Integer.parseInt(matcher.group("m"));
                                int s = Integer.parseInt(matcher.group("s"));
                                seconds = h * 60 * 60 + m * 60 + s;
                                System.out.println("Duration: " + seconds);
                            }
                        }
                        else {
                            Matcher matcher = pFrame.matcher(line);
                            if(matcher.matches()) {
                                int h = Integer.parseInt(matcher.group("h"));
                                int m = Integer.parseInt(matcher.group("m"));
                                int s = Integer.parseInt(matcher.group("s"));
                                int elapsed = h * 60 * 60 + m * 60 + s;
                                System.out.println("Elapsed: " + elapsed);
                                try {
                                    vt.setTaskStatus(TaskStatus.RUNNING.toString());
                                    vt.setProgressPercent((int) ((float)elapsed/seconds*100f));
                                    req.path("/backend/videoTask/"+
                                            vt.getUUID())
                                            .request()
                                            .put(Entity.entity(vt, MediaType.APPLICATION_JSON));
                                }
                                catch (Exception e)
                                {
                                    logger.warn("pos: ",e);
                                }
                            }
                        }
                    }
                    int res= 0;
                    try {

                        res = p.waitFor();
                        if(res==0) {
                            vt.setTaskStatus(TaskStatus.SUCCESS.toString());
                            vt.setProgressPercent(100);
                            req.path("/backend/videoTask/" +
                                    vt.getUUID())
                                    .request()
                                    .put(Entity.entity(vt, MediaType.APPLICATION_JSON));
                        }
                        else {
                            sb.append("\nReturned:"+res);
                            vt.setTaskResult(pb.toString());
                            logger.warn(String.format("Transcode failed: %s", sb.toString()));
                            System.out.println(sb.toString());
                            vt.setTaskStatus(TaskStatus.FAILED.toString());
                            vt.setProgressPercent(100);

                            req.path("/backend/videoTask/" +
                                    vt.getUUID())
                                    .request()
                                    .put(Entity.entity(vt, MediaType.APPLICATION_JSON));

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                } catch (IOException ex) {
                    logger.error("submitTranscode: ",ex);
                }
            }
        });
    }

    public void snapshot(String videoDirName, VWVideo video) {
        File videoDir = new File(videoDirName, video.getUUID());
        File sourceFile = new File(videoDir, "upload.vid");
        File destFileFirst = new File(videoDir, "snap_first.png");
        File destFileLast = new File(videoDir, "snap_last.png");

        VWVideoTask vtFirst=new VWVideoTask();
        vtFirst.setSourceFile(sourceFile.getAbsolutePath());
        vtFirst.setDestFile(destFileFirst.getAbsolutePath());
        vtFirst.setUUID(video.getUUID());
        submitSnapshot(video, vtFirst,0f);

        VWVideoTask vtLast=new VWVideoTask();
        vtLast.setSourceFile(sourceFile.getAbsolutePath());
        vtLast.setDestFile(destFileLast.getAbsolutePath());
        vtLast.setUUID(video.getUUID());
        submitSnapshot(video, vtLast,1f);
    }

    private void submitSnapshot(final VWVideo video, final VWVideoTask vt, float time) {
        executorService.submit(new Runnable() {
            public void run() {
                String[] list = {ffprobe_exe,
                        "-show_streams",
                        "-select_streams", "v:0",
                        "-show_entries", "stream=nb_frames",
                        vt.getSourceFile()};

                ProcessBuilder pb = new ProcessBuilder(list);
                int frames=-1;
                try {
                    Process p = pb.start();
                    String content = CharStreams.toString(new InputStreamReader(p.getInputStream(), Charsets.UTF_8));
                    Matcher matcher = pNbFrames.matcher(content);
                    if(matcher.find()) {
                        frames= Integer.parseInt(matcher.group("frames"));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                String[] fflist = {ffmpeg_exe,
                        "-i", vt.getSourceFile(),
                        "-vf", "select='eq(n, "+((frames-1)*time)+")',"+
                        MessageFormat.format("scale=iw*min({0}/iw\\,{1}/ih):ih*min({0}/iw\\,{1}/ih), "+
                                        "pad={0}:{1}:({0}-iw*min({0}/iw\\,{1}/ih))/2:({1}-ih*min({0}/iw\\,{1}/ih))/2",
                                640,480),
                        "-vframes", "1",
                        vt.getDestFile()};
                ProcessBuilder pbFF = new ProcessBuilder(fflist);
                try {
                    Process p = pbFF.start();
                    String content = CharStreams.toString(new InputStreamReader(p.getInputStream(), Charsets.UTF_8));
                    p.waitFor();
                    vt.setTaskStatus(TaskStatus.SUCCESS.toString());
                    vt.setProgressPercent(100);
                    req.path("/backend/videoTask/" +
                            vt.getUUID())
                            .request()
                            .put(Entity.entity(vt, MediaType.APPLICATION_JSON));

                } catch (IOException|InterruptedException e) {
                    logger.warn("Snapshot failed",e);
            }
            }
        });
    }


}