package views;

import objects.User;
import views.base.BaseView;

/**
 * Created by Martin on 24.10.2015.
 */
public class DebugView extends BaseView {
    public DebugView(User user) {
        super("debug.ftl", user);
    }



}
