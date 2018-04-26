package com.openthos.taskmanager.piebridge.prevent.ui;

import java.util.Set;
import java.util.TreeSet;

public class PreventList extends PreventFragment {

    @Override
    protected Set<String> getPreventPkgNames(PreventActivity activity) {
        return new TreeSet<>(activity.getPreventPackages().keySet());
    }

/*    @Override
    protected int getQueryHint() {
        return R.string.query_hint;
    }

    @Override
    protected String getDefaultQuery() {
        return null;
    }

    @Override
    protected boolean canSelectAll() {
        return true;
    }*/

}