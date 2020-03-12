package com.infogain.automation.softwareupdate.foundation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Model class for AutomationProperty in automation server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = AutomationSoftwareUpdateConstants.AUTOMATION_PROPERTY)
public class AutomationProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    String propname;
    @XmlAttribute
    String propvalue;

    public AutomationProperty(String propname, String propvalue) {
        this.propname = propname;
        this.propvalue = propvalue;
    }

    public AutomationProperty() {}

    public String getPropname() {
        return propname;
    }

    public String getPropvalue() {
        return propvalue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AutomationProperty [propname=").append(propname).append(", propvalue=").append(propvalue)
                        .append("]");
        return builder.toString();
    }



}
