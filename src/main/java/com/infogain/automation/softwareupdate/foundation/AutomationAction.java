package com.infogain.automation.softwareupdate.foundation;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Model class for AutomationAction in automation server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = AutomationSoftwareUpdateConstants.AUTOMATION_ACTION)
public class AutomationAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = AutomationSoftwareUpdateConstants.AUTOMATION_PROPERTY)
    List<AutomationProperty> property;
    @XmlAttribute(name = AutomationSoftwareUpdateConstants.NAME, required = true)
    String name;
    @XmlAttribute
    String description;
    @XmlAttribute(name = AutomationSoftwareUpdateConstants.CLASS, required = true)
    String className;
    @XmlAttribute
    String rollback;
    @XmlAttribute(name = AutomationSoftwareUpdateConstants.ACTIVE, required = true)
    String active;
    @XmlAttribute(name = AutomationSoftwareUpdateConstants.EXIT_ON_FAIL)
    String exitOnFail;
    @XmlAttribute(name = AutomationSoftwareUpdateConstants.PHASE)
    AutomationPhasesErrorCode automationSoftwareUpdatePhase;
    @XmlAttribute(name = AutomationSoftwareUpdateConstants.RETRY_COUNT)
    int retryCount;

    public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public boolean isExitOfFail() {
        return Boolean.valueOf(exitOnFail);
    }

    public boolean isActive() {
        return Boolean.valueOf(this.active);
    }

    public String getName() {
        return name;
    }

    public List<AutomationProperty> getProperty() {
        return property;
    }


    public String getDescription() {
        return description;
    }


    public String getClassName() {
        return className;
    }

    public boolean isRollback() {
        return Boolean.valueOf(this.rollback);
    }
    
    public AutomationPhasesErrorCode getAutomationSoftwareUpdatePhase() {
        return automationSoftwareUpdatePhase;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AutomationAction [property=").append(property).append(", name=").append(name)
                        .append(", description=").append(description).append(", className=").append(className)
                        .append(", rollback=").append(rollback).append(", active=").append(active)
                        .append(", exitOnFail=").append(exitOnFail).append(", automationSoftwareUpdatePhase=")
                        .append(automationSoftwareUpdatePhase).append(", retryCount=")
                        .append(retryCount).append("]");
        return builder.toString();
    }

   
}
