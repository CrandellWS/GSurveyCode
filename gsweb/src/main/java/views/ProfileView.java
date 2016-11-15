package views;

import gamesurvey.dao.MyDAOImpl;
import objects.User;
import views.base.BaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class ProfileView extends BaseView {
    private final MyDAOImpl dao;

    public ProfileView(MyDAOImpl dao, User user) {
        super("profile.ftl", user);
        this.dao=dao;
    }




}
