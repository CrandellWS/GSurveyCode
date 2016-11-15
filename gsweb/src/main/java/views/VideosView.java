package views;

import gamesurvey.dao.MyDAOImpl;
import objects.User;
import objects.Video;
import views.base.BaseView;

import java.util.List;

/**
 * Created by Martin on 24.10.2015.
 */
public class VideosView extends BaseView {
    private final MyDAOImpl dao;

    public VideosView(MyDAOImpl dao, User user) {
        super("videos.ftl",  user);
        this.dao=dao;
    }

    public List<Video> getVideos() {
        return dao.selectVideos(getUser());
    }

}
