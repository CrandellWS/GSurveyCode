package views;

import objects.User;
import views.base.BaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class AdminView extends BaseView {
    public AdminView(User user) {
        super("adminlayout.ftl",  user);
    }
}
