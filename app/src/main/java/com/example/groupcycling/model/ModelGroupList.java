package com.example.groupcycling.model;

public class ModelGroupList {

    private String groupName, createdBy, timestamp, goupPassword;

    public ModelGroupList(){

    }

    public ModelGroupList(String groupName, String createdBy, String timestamp, String goupPassword) {
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.timestamp = timestamp;
        this.goupPassword = goupPassword;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getGoupPassword() {
        return goupPassword;
    }

    public void setGoupPassword(String goupPassword) {
        this.goupPassword = goupPassword;
    }
}
