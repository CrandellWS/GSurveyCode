package objects.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Created by Martin on 06.04.2016.
 */
public class ResultData {

    public     ResultData()
    {

    }

    @JsonProperty
    private List<VideoResult> values=new ArrayList<>();

    public List<VideoResult> getValues() {
        return values;
    }

    public void setValues(List<VideoResult> values) {
        this.values = values;
    }


    public static class VideoResult {
        private String id;
        private String value;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
