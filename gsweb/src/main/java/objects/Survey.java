package objects;

/**
 * Created by Martin on 24.10.2015.
 */
public class Survey {
    private String name;
    private long dateCreated;
    private long dateModified;
    private long id;
    private long responsecount;
    private String status;
    private SurveyData surveyData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    private String jsonData;


    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public long getResponsecount() {
        return responsecount;
    }

    public void setResponsecount(long responsecount) {
        this.responsecount = responsecount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSurveyData(SurveyData surveyData) {
        this.surveyData = surveyData;
    }

    public SurveyData getSurveyData() {
        return surveyData;
    }
}
