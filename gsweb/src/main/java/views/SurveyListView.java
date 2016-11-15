package views;

import com.fasterxml.jackson.databind.ObjectMapper;
import gamesurvey.dao.MyDAOImpl;
import objects.Survey;
import objects.SurveyData;
import objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.RegisterResource;
import views.base.BaseView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Martin on 24.10.2015.
 */
public class SurveyListView extends BaseView {
    private final static Logger log = LoggerFactory.getLogger(SurveyListView.class);

    private MyDAOImpl dao;

    public SurveyListView(MyDAOImpl dao, User user) {
        super("surveyList.ftl",  user);
        this.dao = dao;
    }


    public List<Survey> getSurveys()  {

        List<Survey> surveys = dao.selectSurveys(getUser().getId());
        ObjectMapper sMapper = new ObjectMapper();
        for(Survey s:surveys){
            try {
                SurveyData sd = sMapper.readValue(s.getJsonData(), SurveyData.class);
                s.setSurveyData(sd);
            }catch (IOException e){
                log.warn("Error parsing survey data",e);
            }
        }
        return surveys;
    }

}
