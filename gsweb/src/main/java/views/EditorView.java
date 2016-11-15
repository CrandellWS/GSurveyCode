package views;

import gamesurvey.dao.MyDAOImpl;
import objects.Survey;
import objects.User;
import objects.Video;
import views.base.BaseView;

import java.util.List;

/**
 * Created by Martin on 24.10.2015.
 */
public class EditorView extends BaseView {
    private MyDAOImpl dao;
    private final long survey_id;

    public EditorView(MyDAOImpl dao, long survey_id, User user) {
        super("editor.ftl", user);
        this.dao=dao;
        this.survey_id = survey_id;
    }

    public Survey getSurvey() {
        return dao.selectSurvey(survey_id);
    }

    public List<Video> getVideos(){
        return dao.selectVideos(getUser());
    }

    private final static String[] kelly_colors={
        "#000000", //# No Color
        "#FFB300", //# Vivid Yellow
        "#803E75", //# Strong Purple
        "#FF6800", //# Vivid Orange
        "#A6BDD7", //# Very Light Blue
        "#C10020", //# Vivid Red
        "#CEA262", // # Grayish Yellow
        "#817066" //# Medium Gray
    };
    public String[] getColors()
    {
        return kelly_colors;
    }

}
