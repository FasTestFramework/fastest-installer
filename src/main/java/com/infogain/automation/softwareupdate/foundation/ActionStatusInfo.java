package com.infogain.automation.softwareupdate.foundation;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br>
 * <br>
 * Theme - Core Retail Automation Services<br>
 * Feature - Automation Services - Build and package for phased retail deployment<br>
 * Description - This is the Model class for PDFReportPhaseInfo in automation server installer
 * 
 * @author Namit Jain [3696360]
 * @version 0.0.1
 * @since 18-Oct-2019
 */
public class ActionStatusInfo {

    private String actionName;
    private String actionDescription;
    private boolean actionStatus;


    public ActionStatusInfo() {}

    public ActionStatusInfo(String actionName, String actionDescription, boolean actionStatus) {

        this.actionName = actionName;
        this.actionDescription = actionDescription;
        this.actionStatus = actionStatus;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public boolean getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(boolean actionStatus) {
        this.actionStatus = actionStatus;
    }

    @Override
    public String toString() {
        return "PDFPhaseInfo [actionName=" + actionName + ", actionDescription=" + actionDescription + ", actionStatus="
                        + actionStatus + "]";
    }

}
