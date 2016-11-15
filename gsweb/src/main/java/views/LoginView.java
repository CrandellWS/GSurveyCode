package views;

import gamesurvey.GSApplication;
import gamesurvey.dao.MyDAOImpl;
import views.base.PublicBaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class LoginView extends PublicBaseView {
    private MyDAOImpl dao;

    public LoginView(MyDAOImpl dao) {
        super("login.ftl");
        this.dao=GSApplication.getDao();
    }


    public String login(String username, String password)
    {
        return "";
    }

}
