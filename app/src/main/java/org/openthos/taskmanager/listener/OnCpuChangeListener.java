package org.openthos.taskmanager.listener;

public interface OnCpuChangeListener {
    void cpuUse(double cpuUse);
    void loadComplete();
}
