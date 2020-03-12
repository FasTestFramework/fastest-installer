package com.infogain.automation.installer;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for loading the automationServerInstaller.properties file
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 16-Oct-2019
 */
public class AutomationSoftwareLoadProperties {

    private static final Logger logger = LogManager.getLogger(AutomationSoftwareLoadProperties.class);

    private Properties property = new Properties();

    /**
     * @param configPropertyPath
     */
    public AutomationSoftwareLoadProperties(String configPropertyPath) {
        loadConfigProperties(configPropertyPath);
    }

    /**
     *  Default constructor
     */
    public AutomationSoftwareLoadProperties() {}

    /**
     * @param configPropertyPath
     */
    private void loadConfigProperties(String configPropertyPath) {
        try(FileInputStream propsIn = new FileInputStream(configPropertyPath)) {
            property.load(propsIn);
        } catch (Exception exception) {
            logger.error(AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorMessage(),
                            ExceptionUtils.getStackTrace(exception));
            throw new AutomationInstallerException(
                            AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorCode(),
                            "Error Occurred while loading automation config properties", exception);
        } 
    }

    /**
     * @return
     */
    public Properties getProperty() {
        return property;
    }

    /**
     * @param property
     */
    public void setProperty(Properties property) {
        this.property = property;
    }
}
