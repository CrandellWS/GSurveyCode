package views.su;

import gamesurvey.dao.MyDAOImpl;
import objects.User;
import views.base.BaseView;

import java.util.List;

/**
 * Created by Martin on 24.10.2015.
 */
public class SUView extends BaseView {
    private final MyDAOImpl dao;

    public SUView(MyDAOImpl dao) {
        super("su.ftl",  new User(1,"userName"));
        this.dao = dao;
    }
    public List<User> getUsers() {
        return dao.selectUserList();
    }

}
