package org.openthos.taskmanager.piebridge.prevent.xposed;

import android.app.ActivityThread;
import android.util.Log;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;

public class XposedMod implements IXposedHookZygoteInit {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        /**
         * hook系统中的 ActivityThread 中的 systemMain 方法；
         * 具体在SystemServiceHook中；
         * */
        Log.i("initZygote:::", "Smaster");
        XposedBridge.hookAllMethods(ActivityThread.class, "systemMain", new SystemServiceHook());
    }
}
