package views;

import gamesurvey.dao.MyDAOImpl;
import views.base.PublicBaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class ResetPasswordView extends PublicBaseView {
    private MyDAOImpl dao;

    public ResetPasswordView(MyDAOImpl dao) {
        super("resetPassword.ftl");
        this.dao=dao;
    }



}
