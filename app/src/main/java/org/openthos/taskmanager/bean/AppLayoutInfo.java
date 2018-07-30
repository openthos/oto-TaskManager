package org.openthos.taskmanager.bean;

import java.util.ArrayList;
import java.util.List;

public class AppLayoutInfo {
    private String type;
    private List<AppInfo> appInfos;

    public AppLayoutInfo() {

    }

    public AppLayoutInfo(String type, List<AppInfo> appInfos) {
        this.type = type;
        this.appInfos = appInfos;
    }

    public AppLayoutInfo(String type) {
        this.type = type;
        this.appInfos = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AppInfo> getAppInfos() {
        return appInfos;
    }

    public void setAppInfos(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }
}
