package com.infogain.automation.installer.actions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for updating the Property file
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationUpdateHostNameAction extends AutomationSoftwareUpdateAction {

    private String source;
    private String propertyName;
    private AutomationPhasesErrorCode installationPhase;
    private AutomationPropertyFileUpdateAction automationPropertyFileUpdateAction;
    private static final Logger logger = LogManager.getLogger(AutomationUpdateHostNameAction.class);

    public void init() {
        installationPhase = this.getAutomationSoftwareUpdatePhase();
        automationPropertyFileUpdateAction = new AutomationPropertyFileUpdateAction();
        source =(String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION);
        propertyName = (String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_NAME);
    }

    /**
     * This method returns local host properties
     * 
     * @return InetAddress
     * @since 20-Sep-2019
     * @version 1.0.0
     */
    private InetAddress getLocalHost() {
        logger.traceEntry("getLocalHost method of AutomationUpdateHostNameAction class");
        InetAddress localHost;
        try {
            logger.debug("Reteriving the ip address using the getLocalHost method of InetAddress");
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException unknownHostException) {
            logger.error("An exception while fetching hostname has occured", unknownHostException.getMessage());
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            unknownHostException.getMessage(), unknownHostException.getCause());
        }
        return logger.traceExit(localHost);
    }

    /**
     * This method returns the hostName of automation server
     * 
     * @return String
     * @since 20-Sep-2019
     * @version 1.0.0
     */
    private String getAutomationServerHostName() {
        logger.debug("Reteriving the host name using the getCanonicalHostName method of InetAddress");
        return getLocalHost().getCanonicalHostName();
    }

    /**
     * This method execute the AutomationUpdateHostNameAction Action
     * 
     * @param fullClassName
     * @return Object
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationUpdateHostNameAction class");
        init();
        automationPropertyFileUpdateAction.setAutomationSoftwareUpdatePhase(installationPhase);
        logger.debug("Creating the object of automationPropertyFileUpdateAction");
        Properties properties = new Properties();
        logger.debug("Setting the property value");
        properties.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION, source);
        properties.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_NAME, propertyName);
        properties.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_VALUE, getAutomationServerHostName());
        automationPropertyFileUpdateAction.setProps(properties);
        logger.debug("Calling  the execute method of delete action");
        automationPropertyFileUpdateAction.execute();
        return logger.traceExit(0);
    }

    /**
     * This method is the undo the AutomationUpdateHostNameAction Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */

    @Override
    public int undoAutomationSoftwareUpdateAction() {
        logger.debug("calling the undo action of AutomationUpdateHostNameAction class");
        return automationPropertyFileUpdateAction.undoAutomationSoftwareUpdateAction();
    }

    /**
     * This method is the validateAction the AutomationUpdateHostNameAction Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.debug("calling the  validateAction of AutomationUpdateHostNameAction class");
        return automationPropertyFileUpdateAction.validateAction();
    }

    /**
     * This method is the cleanUp the AutomationUpdateHostNameAction Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        return 0;
    }

}
