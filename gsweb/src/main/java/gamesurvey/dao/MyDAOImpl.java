package gamesurvey.dao;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import objects.*;
import org.skife.jdbi.v2.sqlobject.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Created by Martin on 19.11.2015.
 */
public class MyDAOImpl {
    private  MyDAO dao;
    public MyDAOImpl(MyDAO dao)
    {
        this.dao=dao;
    }

    public long insertVideo(int server_id, VWVideo v) {
        return dao.upsertVideo(v.getUserEmail(), v.getUUID(), v.getName(), v.getVideoStatus(), v.getServerID());
    }

    public void insertSnapshot(UUID vid, String fileName) {
    }

    public List<Video> selectVideos(User user)
    {
        return dao.selectVideosByUserID(user.getId());
    }
    public List<VWVideo> selectDeletedVideos()
    {
        return dao.selectDeletedVideos(1);
    }
    public Video selectVideo(long video_id)
    {
        return dao.selectVideoByID(video_id);
    }
    public Video selectVideoByUUID(String uuid) {
        return dao.selectVideoByUUID(uuid);
    }

    public Survey selectSurvey(long survey_id) {
        return dao.selectSurvey(survey_id);
    }

    public Survey selectSurvey(long survey_id, long user_id) {
        return dao.selectSurvey(survey_id, user_id);
    }

    public void updateSurvey(long id, long user_id, String in, String name) {
        dao.updateSurvey(id, user_id, in, name);
    }

    public long insertSurvey(User user, String in, String name) {
        return dao.insertSurvey(user.getId(), in, name, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public List<Survey> selectSurveys(long user_id) {
        return dao.selectSurveysByUserID(user_id);
    }

    public void markVideoDeleted(User user, String uuid) {
        dao.markVideoDeleted(user.getId(), uuid);
    }

    @Transaction
    public void deletedVideoByUUID(String uuid) {
        Video v=dao.selectVideoByUUID(uuid);
        dao.deleteVideoTasksByVideoID(v.getId());
        dao.deleteVideoByID(v.getId());
    }

    public long upsertVideoTask(int server_id, VWVideoTask v) {
        return dao.upsertVideoTask(v.getUUID(),
                v.getTask(),
                v.getTaskData(),
                v.getProgressPercent(),
                v.getTaskStatus(),
                v.getTaskResult());
    }

    public Server selectServerByID(int server_id)
    {
        return dao.selectServerByID(server_id);
    }

    public List<Response> selectResponseListByServeyID(long servey_id) {
        return dao.selectResponseListByServeyID(servey_id);
    }


    public long insertResponse(long survey_id, String in) {
        return dao.insertResponse(survey_id,in);
    }

    public Response selectResponseByID(long response_id) {
        return dao.selectResponseByID(response_id);
    }
    public PasswordData getEncryptedPassword(String username) {
        return dao.getEncryptedPassword(username);
    }


    public User selectUserByEmail(String email) {
        return dao.selectUserByEmail(email);
    }

    public List<User> selectUserList() {
        return dao.selectUserList();
    }

    public Stats selectStats(long id) {return dao.selectUserStats(id); }

    @Transaction
    public void insertUser(String name, String email, byte[] salt, byte[] encPassword) {
        long uid=dao.insertUser(name, email);
        dao.insertPassword(uid, salt, encPassword);
    }

    public void updatePassword(String email, byte[] salt, byte[] encPassword) {
        User user=dao.selectUserByEmail(email);
        dao.updatePassword(user.getId(), salt, encPassword);
    }

    public void reset() {

    }


    public void markSurveyDeleted(User user, long id) {
            dao.markSurveyDeleted(user.getId(), id);
    }


}
