package views;

import gamesurvey.dao.MyDAOImpl;
import views.base.PublicBaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class ResetPasswordStep2View extends PublicBaseView {
    private final String token;
    private String email;

    private MyDAOImpl dao;

    public ResetPasswordStep2View(MyDAOImpl dao, String token, String email) {
        super("resetPasswordStep2.ftl");
        this.token=token;
        this.email=email;
        this.dao=dao;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }
}
