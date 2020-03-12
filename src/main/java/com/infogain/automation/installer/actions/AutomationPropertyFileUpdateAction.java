package com.infogain.automation.installer.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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
 * Description - This Action class is used to update the configuration property file placed in predefined location.
 * 
 * @author Naincy Gupta [377204]
 * @version 0.0.1
 * @since Oct 10, 2019
 */
public class AutomationPropertyFileUpdateAction extends AutomationSoftwareUpdateAction {

    private String source;
    private PropertiesConfiguration config;
    private Map<String, String> propertyMap;
    private Map<String, String> prevPropertyMap = new HashMap<>();
    private AutomationPhasesErrorCode installationPhase;
    private static final Logger logger = LogManager.getLogger(AutomationPropertyFileUpdateAction.class);


    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @return int
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public void init() {
        installationPhase = this.getAutomationSoftwareUpdatePhase();
        String propertyName = (String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_NAME);
        String propertyValue = (String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_VALUE);
        if (propertyMap == null) {
            propertyMap = new HashMap<>();
        }
        if (propertyName != null) {
            propertyMap.put(propertyName, propertyValue);
        }
        config = config == null ? getPropertiesSnapshot() : config;
    }

    /**
     * This method reads the properties file at location provided through properties and return PropertiesConfiguration
     * 
     * @return PropertiesConfiguration instance of properties present in the properties file at the provided location
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    public PropertiesConfiguration getPropertiesSnapshot() {
        source = (String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION);
        try {
            return new PropertiesConfiguration(source);
        } catch (ConfigurationException configurationException) {
            logger.error("An exception while fetching hostname has occured {}", configurationException.getMessage());
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            configurationException.getMessage(), configurationException.getCause());
        }
    }

    /**
     * This method used to execute the AutomationPropertyFileUpdateAction
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationPropertyFileUpdateAction class");
        init();
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            prevPropertyMap.put(key, config.getString(key));
        }
        for (Map.Entry<String, String> property : propertyMap.entrySet()) {
            config.setProperty(property.getKey(), property.getValue());
        }
        try {
            config.save();
        } catch (ConfigurationException configurationException) {
            logger.error("An exception while loading properties {}", configurationException.getMessage());
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            configurationException.getMessage(), configurationException.getCause());
        }
        return logger.traceExit(0);
    }

    /**
     * This method used to perform the undo action for the AutomationPropertyFileUpdateAction
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        logger.traceEntry("undoAutomationSoftwareUpdateAction method of AutomationPropertyFileUpdateAction class");
        if (prevPropertyMap != null && !prevPropertyMap.isEmpty()) {
            try {
                config.clear();
                config.save();
                for (Map.Entry<String, String> prevProperty : prevPropertyMap.entrySet()) {
                    config.setProperty(prevProperty.getKey(), prevProperty.getValue());
                }
                config.save();
            } catch (ConfigurationException configurationException) {
                logger.error("An exception while performing undo action {}", configurationException.getMessage());
                throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                configurationException.getMessage(), configurationException.getCause());
            }
        } else {
            logger.error("Previous properties are not present undo action not executed");
        }
        return logger.traceExit(0);
    }

    /**
     * This method used to validate whether AutomationPropertyFileUpdateAction is performed or not
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationPropertyFileUpdateAction class");
        try {
            config = new PropertiesConfiguration(source);
            for (Map.Entry<String, String> property : propertyMap.entrySet()) {
                if (!property.getValue().equals(config.getString(property.getKey()))) {
                    return logger.traceExit(false);
                }
            }
        } catch (ConfigurationException configurationException) {
            logger.error("An exception while performing the validation action {}", configurationException.getMessage());
            throw new AutomationInstallerException(
                            AutomationPhasesErrorCode.VALIDATIONPHASE.getErrorCode(),
                            "An exception while performing the validation action", configurationException.getCause());
        }
        return logger.traceExit(true);
    }

    /**
     * This method used perform cleanUp
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }


}
