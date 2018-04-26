package org.openthos.taskmanager.app;

public class Constants {
    public static final String SP_DORMANT_APP = "dormant_app";
    public static final String SP_NON_DORMANT_APP = "non_dormant_app";
    public static final String SP_PREVENT_APP = "prevent_app";

    public static final int HEIGHT_MASK = 0x3fffffff;
    public static String ONE_OR_MORE_SPACE = "\\s+";

    public static final int App_NON_DEAL = -1;
    public static final int APP_NON_DORMANT = 0;
    public static final int APP_HAVE_DORMANT = 1;
    public static final int APP_WAIT_DORMANT = 2;
    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;

    public static int DELAY_TIME_REFRESH = 1000;
    public static int TIME_CPU_REFRESH = 2000;

    public static final int FORWARD_APP = 4;
    public static final int BACKGROUND_APP = 5;
    public static final int NO_DORMANT_APP = 6;
    public static final int NOT_RUN = 7;

}
