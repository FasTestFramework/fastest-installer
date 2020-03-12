package com.infogain.automation.softwareupdate.constants;
/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the enum class conatins the Constant for the different phases with there error codes
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public enum AutomationPhasesErrorCode {

	COPYDEPENDENCYPHASE(101, "Exception occured while copying the dependency"),
	INSTALLATIONPHASE(201, "Exception occured while installation process"),
    APPLICATION_RUN_PHASE(301,"Exception occured while starting"),
    REPORTINGPHASE(401,"Exception occured while sending mail"),
    AUTOMATION_INSTALLER_FAILURE(500,"Automation Installer Failure Occured"),
    CLEANUP(601,"Exception occured while cleanup"),
    VALIDATIONPHASE(701,"Exception occured while validating the process");
	
    private int errorCode;
    private String errorMessage;

    AutomationPhasesErrorCode(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
