package objects;

import java.util.List;

/**
 * Created by Martin on 24.10.2015.
 */
public class SurveyData {

    private String name;
    private long dateCreated;
    private long dateModified;
    private String description;
    private VNode startNode = new VNode(1);
    private VNode endNode = new VNode(2);
    private List<VNode> nodeList;
    private int seq = 1;
    private String contact;
    private SurveyStatus status = SurveyStatus.offline;
    private long startDate = System.currentTimeMillis()/1000;
    private long endDate = System.currentTimeMillis()/1000;
    public SurveyData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public List<VNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<VNode> nodeList) {
        this.nodeList = nodeList;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public VNode getStartNode() {
        return startNode;
    }

    public void setStartNode(VNode startNode) {
        this.startNode = startNode;
    }

    public VNode getEndNode() {
        return endNode;
    }

    public void setEndNode(VNode endNode) {
        this.endNode = endNode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public void setStatus(SurveyStatus status) {
        this.status = status;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
}
