package org.openthos.taskmanager.listener;

import org.openthos.taskmanager.bean.AppInfo;

import java.util.List;

public interface OnTaskCallBack {
    void callBack(List<AppInfo> appInfos);
}