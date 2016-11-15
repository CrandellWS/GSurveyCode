package views;

import gamesurvey.dao.MyDAOImpl;
import objects.Survey;
import objects.User;
import views.base.BaseView;

import java.util.List;

/**
 * Created by Martin on 24.10.2015.
 */
public class ResponseListView extends BaseView {
    private  MyDAOImpl dao;

    public ResponseListView(MyDAOImpl dao, User user) {
        super("responseList.ftl",  user);
        this.dao=dao;
    }

    public List<Survey> getSurveys() {
        return dao.selectSurveys(getUser().getId());
    }

}
