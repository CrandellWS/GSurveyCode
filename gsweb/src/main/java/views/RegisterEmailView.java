package views;

import gamesurvey.dao.MyDAOImpl;
import views.base.PublicBaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class RegisterEmailView extends PublicBaseView {
    private final String name;
    private final String email;

    private MyDAOImpl dao;

    public RegisterEmailView(MyDAOImpl dao, String name, String email) {
        super("registerEmail.ftl");
        this.dao=dao;
        this.name=name;
        this.email=email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }


}
