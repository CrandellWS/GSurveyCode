package gamesurvey.dao;

import objects.*;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Martin on 26.10.2015.
 */
public interface MyDAO  {
    @SqlUpdate("DELETE FROM snapshots; DELETE FROM videos; ")
    void reset();

    @SqlUpdate("INSERT into video (id, user_id, uuid, name, status, server_id) " +
            "VALUES (DEFAULT, (SELECT id FROM gs_user WHERE email=:user_email), :uuid, :name, :status, :server_id) " +
            "ON CONFLICT (\"uuid\") DO UPDATE "+
            "SET status=:status WHERE video.uuid=:uuid "+
            "RETURNING id")
    long upsertVideo( @Bind("user_email") String user_email,
                      @Bind("uuid") String uuid,
                      @Bind("name") String name ,
                      @Bind("status") String status,
                      @Bind("server_id") int server_id
    );

    @SqlUpdate("INSERT into snapshots (videos_id, filename) values (:videos_id, :name)")
    void insertSnapshot(@Bind("videos_id") long video_id, @Bind("name") String name);

    @SqlQuery("SELECT video.id as id, uuid, name, server_id, " +
            "string_agg(videotask.status,',') as status, "+
            "sum(videotask.percent)/count(videotask.percent) as percent from video \n" +
            "INNER JOIN videotask ON videotask.video_id=video.id \n" +
            "where user_id=:user_id AND video.deleted=false \n" +
            "GROUP BY video.id, uuid,name, server_id, video.percent \n")
    @Mapper(VideoMapper.class)
    List<Video> selectVideosByUserID(@Bind("user_id") long user_id);

    @SqlQuery("SELECT uuid, name, status  from video where  deleted=true")
    @Mapper(VWVideoMapper.class)
    List<VWVideo> selectDeletedVideos(int server_id);

    @SqlQuery("SELECT id, uuid, name, server_id, status, percent  from video where id=:id")
    @Mapper(VideoMapper.class)
    Video selectVideoByID(@Bind("id") long id);

    @SqlQuery("SELECT id, uuid, name, server_id, status, percent  from video where uuid=:uuid")
    @Mapper(VideoMapper.class)
    Video selectVideoByUUID(@Bind("uuid")String uuid);


    @SqlQuery("SELECT id, name, jsondata, " +
            "extract(epoch from datecreated) as datecreated, " +
            "extract(epoch from datemodified) as datemodified "+
            "from survey where id=:id")
    @Mapper(SurveyMapper.class)
    Survey selectSurvey(@Bind("id") long id);

    @SqlQuery("SELECT id, name, jsondata, " +
            "extract(epoch from datecreated) as datecreated, " +
            "extract(epoch from datemodified) as datemodified "+
            "from survey where id=:id AND user_id=:user_id")
    @Mapper(SurveyMapper.class)
    Survey selectSurvey(@Bind("id") long survey_id, @Bind("user_id") long user_id);


    @SqlUpdate("UPDATE survey SET user_id=:user_id, jsondata=CAST(:jsondata as json), name=:name WHERE id=:id")
    void updateSurvey(@Bind("id") long id,
                      @Bind("user_id") long user_id,
                      @Bind("jsondata") String jsondata,
                      @Bind("name") String name);

    @SqlQuery("INSERT INTO survey(id, user_id, jsondata, name, datecreated, datemodified) " +
              "VALUES (DEFAULT, :user_id, CAST(:jsondata as json), " +
              ":name, " +
              "to_timestamp(:datecreated/1000), " +
              "to_timestamp(:datemodified/1000)) RETURNING id")
    long insertSurvey(@Bind("user_id") long user_id,
                      @Bind("jsondata") String jsondata,
                      @Bind("name") String name,
                      @Bind("datecreated") long dateCreated,
                      @Bind("datemodified") long dateModified);

    @SqlQuery("SELECT survey.id as id, survey.name as name, survey.jsondata as jsondata, " +
              "count(response.id) as responsecount, " +
              "extract(epoch from datecreated) as datecreated, " +
              "extract(epoch from datemodified) as datemodified "+
              "FROM survey "+
              "LEFT JOIN response ON response.survey_id=survey.id "+
              "where user_id=:user_id "+
                "AND deleted=false "+
              "group by survey.id, survey.name, survey.jsondata")
    @Mapper(SurveyMapper.class)
    List<Survey> selectSurveysByUserID(@Bind("user_id") long user_id);

    @SqlUpdate("UPDATE video SET deleted=true WHERE uuid=:uuid AND user_id=:user_id")
    void markVideoDeleted(@Bind("user_id") long user_id,@Bind("uuid") String uuid);

    @SqlUpdate("DELETE FROM video WHERE id=:id")
    void deleteVideoByID(@Bind("id") long id);

    @SqlUpdate("DELETE FROM videotask WHERE video_id=:video_id")
    void deleteVideoTasksByVideoID(@Bind("video_id") long video_id);


    @SqlUpdate("INSERT into videotask (id, video_id, taskname, taskdata, percent, taskresult, status) " +
            "VALUES (DEFAULT, (SELECT id FROM video WHERE uuid=:uuid), :taskname, :taskdata, :percent, :taskresult, :status) " +
            "ON CONFLICT (video_id, taskname, taskdata) DO UPDATE "+
            "SET percent=:percent, taskresult=:taskresult, status=:status " +
            "WHERE videotask.video_id=(SELECT id FROM video WHERE uuid=:uuid) AND videotask.taskname=:taskname AND videotask.taskdata=:taskdata "+
            "RETURNING id")
    long upsertVideoTask(
            @Bind("uuid") String video_uuid,
            @Bind("taskname") String taskname,
            @Bind("taskdata") String taskdata,
            @Bind("percent") int percent,
            @Bind("status") String status,
            @Bind("taskresult") String taskresult);

    @SqlQuery("SELECT id, url from server WHERE id=:id")
    @Mapper(ServerMapper.class)
    Server selectServerByID(@Bind("id") int id);

    @SqlQuery("SELECT id, survey_id, EXTRACT(EPOCH FROM date) as date, responsedata from response WHERE survey_id=:survey_id")
    @Mapper(ResponseMapper.class)
    List<Response> selectResponseListByServeyID(@Bind("survey_id")long servey_id);

    @SqlQuery("INSERT INTO response(id, survey_id, responsedata) " +
              "VALUES (DEFAULT, :survey_id, CAST(:responsedata as json)) " +
              "RETURNING id")
    long insertResponse(@Bind("survey_id") long survey_id, @Bind("responsedata") String response);


    @SqlQuery("SELECT user_id, salt, password FROM password WHERE user_id=(SELECT id FROM gs_user WHERE email=:email)")
    @Mapper(PasswordMapper.class)
    PasswordData getEncryptedPassword(@Bind("email") String email);

    @SqlUpdate("INSERT INTO password(user_id, salt, password) " +
            "VALUES(:user_id, :salt, :password) ")
    void insertPassword(@Bind("user_id") long user_id, @Bind("salt") byte[] salt, @Bind("password") byte[] password);

    @SqlUpdate("UPDATE password set salt=:salt, password=:password WHERE user_id=:user_id")
    void updatePassword(@Bind("user_id") long user_id, @Bind("salt") byte[] salt, @Bind("password") byte[] password);

    @SqlQuery("SELECT id, name, ts_create, email FROM gs_user")
    @Mapper(UserMapper.class)
    List<User> selectUserList();

    @SqlQuery("SELECT (SELECT count(id) FROM survey WHERE user_id=:user_id) as surveycount, " +
              "(SELECT count(survey.id) FROM response " +
              "INNER JOIN survey ON response.survey_id=survey.id " +
              "WHERE user_id=:user_id) as responsecount")
    @Mapper(StatsMapper.class)
    Stats selectUserStats(@Bind("user_id") long user_id);

    @SqlQuery("INSERT INTO gs_user(id, name, email) " +
            "VALUES(DEFAULT, :name, :email) "+
            "RETURNING id")
    long insertUser(@Bind("name") String name, @Bind("email") String email);

    @SqlQuery("SELECT id, name, ts_create, email FROM gs_user WHERE email=:email")
    @Mapper(UserMapper.class)
    User selectUserByEmail(@Bind("email") String email);

    @SqlQuery("SELECT id, survey_id, EXTRACT(EPOCH FROM date) as date, responsedata from response WHERE id=:id")
    @Mapper(ResponseMapper.class)
    Response selectResponseByID(@Bind("id") long response_id);

    //ALTER TABLE public.survey
    //ADD COLUMN deleted boolean NOT NULL DEFAULT false;
    @SqlUpdate("UPDATE survey SET deleted=true WHERE id=:id AND user_id=:user_id")
    void markSurveyDeleted(@Bind("user_id")long id, @Bind("id") long id1);


    class SurveyMapper implements ResultSetMapper<Survey>
    {
        public Survey map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            Survey s=new Survey();
            s.setName(resultSet.getString("name"));
            s.setDateCreated(resultSet.getLong("datecreated")*1000);
            s.setDateModified(resultSet.getLong("datemodified")*1000);
            s.setId(resultSet.getLong("id"));
            s.setJsonData(resultSet.getString("jsondata"));
            if(hasColumn(resultSet,"responsecount"))
                s.setResponsecount(resultSet.getLong("responsecount"));
            return s;
        }
    }

    class VideoMapper implements ResultSetMapper<Video>
    {
        public Video map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            Video v=new Video();
            v.setName(resultSet.getString("name"));
            v.setUUID(resultSet.getString("uuid"));
            v.setId(resultSet.getLong("id"));
            v.setServerId(resultSet.getInt("server_id"));
            v.setVideoStatus(resultSet.getString("status"));
            v.setProgressPercent(resultSet.getInt("percent"));
            return v;
        }
    }

    class VWVideoMapper implements ResultSetMapper<VWVideo>
    {
        public VWVideo map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            VWVideo v=new VWVideo();
            v.setName(resultSet.getString("name"));
            v.setUUID(resultSet.getString("uuid"));
            v.setVideoStatus(resultSet.getString("status"));
            return v;
        }
    }

    class ServerMapper implements ResultSetMapper<Server>
    {
        public Server map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            Server s=new Server();
            s.setId(resultSet.getInt("id"));
            s.setUrl(resultSet.getString("url"));
            return s;
        }
    }

    class ResponseMapper implements ResultSetMapper<Response>
    {
        public Response map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            Response s=new Response();
            s.setSurvey_id(resultSet.getLong("survey_id"));
            s.setId(resultSet.getLong("id"));
            s.setTimeStamp(resultSet.getLong("date")*1000);
            s.setResponse(resultSet.getString("responsedata"));
            return s;
        }
    }

    class PasswordMapper implements ResultSetMapper<PasswordData>
    {
        public PasswordData map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            PasswordData s=new PasswordData();
            s.setEncryptedPassword(resultSet.getBytes("password"));
            s.setSalt(resultSet.getBytes("salt"));
            return s;
        }
    }

    class UserMapper implements ResultSetMapper<User>
    {
        public User map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            User u=new User();
            u.setId(resultSet.getLong("id"));
            u.setName(resultSet.getString("name"));
            u.setEmail(resultSet.getString("email"));
            return u;
        }
    }

    class StatsMapper implements ResultSetMapper<Stats>
    {
        public Stats map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            Stats s=new Stats();
            s.setResponseCount(resultSet.getLong("responsecount"));
            s.setSurveyCount(resultSet.getLong("surveycount"));
            return s;
        }
    }

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }
}

