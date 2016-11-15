package views;

import com.fasterxml.jackson.databind.ObjectMapper;
import gamesurvey.GSApplication;
import gamesurvey.dao.MyDAOImpl;
import objects.Survey;
import objects.SurveyData;
import objects.SurveyStatus;
import objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import views.base.PublicBaseView;

import java.io.IOException;

/**
 * Created by Martin on 24.10.2015.
 */
public class SurveyView extends PublicBaseView {
    private final static Logger log = LoggerFactory.getLogger(SurveyView.class);

    private final Survey survey;
    private MyDAOImpl dao;
    private boolean online=false;

    public SurveyView(MyDAOImpl dao, long survey_id, User user) {
        super("takesurvey.ftl");
        this.dao=dao;
        this.survey=dao.selectSurvey(survey_id, user.getId());
        online=true;
    }

    public SurveyView(MyDAOImpl dao, long survey_id) {
        super("takesurvey.ftl");
        this.dao=dao;
        this.survey=dao.selectSurvey(survey_id);
        ObjectMapper sMapper = new ObjectMapper();

        SurveyData sd = null;
        try {
            sd = sMapper.readValue(survey.getJsonData(), SurveyData.class);
            survey.setSurveyData(sd);
            long now=System.currentTimeMillis()/1000;
            if(sd.getStatus()==null||sd.getStatus()== SurveyStatus.offline||
                    (sd.getStatus()==SurveyStatus.planned&&(sd.getStartDate()>now||sd.getEndDate()<now)))
                online=false;
            else online=true;
        } catch (IOException e) {
            log.warn("Read survey failed", e);
        }
    }

    public boolean isOnline() {
        return online;
    }

    public Survey getSurvey() {
        return survey;
    }
}
