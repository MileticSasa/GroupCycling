package com.example.groupcycling.model;

public class ModelGroup {

    private String goupId, groupName, password, createdBy;

    public ModelGroup() {
    }

    public ModelGroup(String goupId, String groupName, String password, String createdBy) {
        this.goupId = goupId;
        this.groupName = groupName;
        this.password = password;
        this.createdBy = createdBy;
    }

    public String getGoupId() {
        return goupId;
    }

    public void setGoupId(String goupId) {
        this.goupId = goupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
