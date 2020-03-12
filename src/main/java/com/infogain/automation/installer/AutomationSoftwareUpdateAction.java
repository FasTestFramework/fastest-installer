
package com.infogain.automation.installer;

import java.util.Properties;

import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the abstract class used for defining the method for executing the action
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public abstract class AutomationSoftwareUpdateAction {


    private String name;
    private String description;
    private String className;
    private Properties props;
    private boolean rollback = true;
    private boolean isActive = true;
    private boolean isExitOnFail = true;
    private int retryCount;
    private String installationPath;

    private AutomationPhasesErrorCode automationSoftwareUpdatePhase;

    /**
     * This method performs cleanUp for the intended action
     * 
     * @return int
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public abstract int cleanUp();

    /**
     * This method executes the intended action
     * 
     * @return int
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public abstract int execute();

    /**
     * This method undoes an action if any error occurs
     * 
     * @return int
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public abstract int undoAutomationSoftwareUpdateAction();

    /**
     * This method validates whether an action has executed successfully
     * 
     * @return boolean
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public abstract boolean validateAction();

    /**
     * This method is used to get exitOnFail flag of an action
     * 
     * @return boolean
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public boolean isExitOnFail() {
        return isExitOnFail;
    }

    /**
     * This method is used to set exitOnFail flag of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setExitOnFail(boolean isExitOnFail) {
        this.isExitOnFail = isExitOnFail;
    }

    /**
     * This method is used to get active flag of an action
     * 
     * @return boolean
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * This method is used to set active flag of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * This method is used to get name of an action
     * 
     * @return String
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public String getName() {
        return name;
    }

    /**
     * This method is used to set name of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method is used to get description of an action
     * 
     * @return String
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used to set description of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method is used to get className of an action
     * 
     * @return String
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public String getClassName() {
        return className;
    }

    /**
     * This method is used to set className of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * This method is used to get properties of an action
     * 
     * @return Properties
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public Properties getProps() {
        return props;
    }

    /**
     * This method is used to set properties of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setProps(Properties props) {
        this.props = props;
    }

    /**
     * This method is used to get rollback flag of an action
     * 
     * @return boolean
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public boolean isRollback() {
        return rollback;
    }


    /**
     * This method is used to set rollback flag of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }

    /**
     * This method is used to get retry count of an action
     * 
     * @return int
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * This method is used to set retryCount of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * This method is used to get error code for installation phase of an action
     * 
     * @return AutomationSoftwarePhasesErrorCode
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public AutomationPhasesErrorCode getAutomationSoftwareUpdatePhase() {
        return automationSoftwareUpdatePhase;
    }

    /**
     * This method is used to set error code for installation phase of an action
     * 
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    public void setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode automationSoftwareUpdatePhase) {
        this.automationSoftwareUpdatePhase = automationSoftwareUpdatePhase;
    }

    /**
     * This method is used to get installation path provided
     * 
     * @return
     */
    public String getInstallationPath() {
        return installationPath;
    }

    /**
     * This method is used to set instalation path provided
     * 
     * @param installationPath
     */
    public void setInstallationPath(String installationPath) {
        this.installationPath = installationPath;
    }

    @Override
    public String toString() {
        return "AutomationSoftwareUpdateAction [name=" + name + ", description=" + description + ", className="
                        + className + ", props=" + props + ", rollback=" + rollback + ", isActive=" + isActive
                        + ", isExitOnFail=" + isExitOnFail + ", retryCount=" + retryCount + ", installationPath="
                        + installationPath + ", automationSoftwareUpdatePhase=" + automationSoftwareUpdatePhase + "]";
    }

}
