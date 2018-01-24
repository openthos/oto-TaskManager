package com.openthos.greenify.app;

public class Constants {
    public static final String SP_DORMANT_APP = "dormant_app";
    public static final String SP_NON_DORMANT_APP = "non_dormant_app";

    public static final int HEIGHT_MASK = 0x3fffffff;

    public static final int App_NON_DEAL = -1;
    public static final int APP_NON_DORMANT = 0;
    public static final int APP_HAVE_DORMANT = 1;
    public static final int APP_WAIT_DORMANT = 2;
    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;
}