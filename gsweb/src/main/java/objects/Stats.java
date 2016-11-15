package objects;

/**
 * Created by Martin on 04.08.2016.
 */
public class Stats {
    private long surveyCount;
    private long bounceRate;
    private long responseCount;


    public long getSurveyCount() {
        return surveyCount;
    }

    public void setSurveyCount(long surveyCount) {
        this.surveyCount = surveyCount;
    }

    public long getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(long bounceRate) {
        this.bounceRate = bounceRate;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(long responseCount) {
        this.responseCount = responseCount;
    }
}
