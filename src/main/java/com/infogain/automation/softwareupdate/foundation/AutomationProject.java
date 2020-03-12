package com.infogain.automation.softwareupdate.foundation;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail automation Services<br/>
 * Feature - automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Model class for automationProject in automation server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = AutomationSoftwareUpdateConstants.AUTOMATION_PROJECT)
public class AutomationProject {

    @XmlElement(name = AutomationSoftwareUpdateConstants.AUTOMATION_ACTION)
    List<AutomationAction> actions;
    @XmlAttribute
    String name;
    @XmlAttribute
    String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<AutomationAction> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("automationProject [actions=").append(actions).append(", name=").append(name)
                        .append(", description=").append(description).append("]");
        return builder.toString();
    }

   
}
