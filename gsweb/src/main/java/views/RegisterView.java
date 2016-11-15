package views;

import gamesurvey.dao.MyDAOImpl;
import views.base.PublicBaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class RegisterView extends PublicBaseView {
    private MyDAOImpl dao;

    public RegisterView(MyDAOImpl dao) {
        super("register.ftl");
        this.dao=dao;
    }



}
