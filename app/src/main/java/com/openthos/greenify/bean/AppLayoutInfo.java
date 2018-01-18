package com.openthos.greenify.bean;

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
