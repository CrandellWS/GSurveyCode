package views;

import gamesurvey.dao.MyDAOImpl;
import objects.Stats;
import objects.User;
import views.base.BaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class DashboardView extends BaseView {
    private final MyDAOImpl dao;

    public DashboardView(MyDAOImpl dao, User user){
        super("dashboard.ftl",  user);
        this.dao = dao;

    }

    public Stats getStats() {
        return dao.selectStats(getUser().getId());
    }
}
