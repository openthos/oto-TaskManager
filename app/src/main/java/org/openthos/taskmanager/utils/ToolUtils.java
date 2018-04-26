package org.openthos.taskmanager.utils;

import org.openthos.taskmanager.app.Constants;

import java.text.DecimalFormat;

public class ToolUtils {

    /**
     * Turn the length of the long type into a String type string (Mb, Kb, b)
     *
     * @param fileSize
     * @return
     */
    public static String transformFileSize(long fileSize) {
        float formatSize;
        String unit = "b";
        if ((formatSize = fileSize / ((float) Constants.MB)) >= 1) {
            unit = "Mb";
        } else if ((formatSize = fileSize / (float) Constants.KB) >= 1) {
            unit = "Kb";
        }
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(formatSize) + unit;
    }
}