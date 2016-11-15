package videoworker.objects;


/**
 * Created by Martin on 22.02.2016.
 */
public class VWVideoTask {
    private String UUID;
    private String taskStatus="";
    private int progressPercent;
    private String sourceFile;
    private String destFile;
    private String task="";
    private String taskData="";
    private String taskResult="";
    private String type="";

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setDestFile(String destFile) {
        this.destFile = destFile;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public void setTaskData(String taskData) {
        this.taskData = taskData;
    }

    public String getDestFile() {
        return destFile;
    }

    public String getTaskData() {
        return taskData;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
