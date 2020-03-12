package com.infogain.automation.installer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.foundation.AutomationAction;
import com.infogain.automation.softwareupdate.foundation.AutomationProperty;
import com.infogain.automation.softwareupdate.utilities.AutomationSoftwareUpdateUtilities;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for loading the action in automation server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationSoftwareActionLoader {

    private static final Logger logger = LogManager.getLogger(AutomationSoftwareActionLoader.class);

    /**
     * This method return the list of the AutomationSoftwareUpdateAction having all the action defined in the XML.
     * 
     * @param list
     * @return
     * @return List<AutomationSoftwareUpdateAction>
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param automationSoftwareLoadProperties
     * @param argInstallationPath
     */
    public List<AutomationSoftwareUpdateAction> buildActions(List<AutomationAction> list,
                    AutomationSoftwareLoadProperties automationSoftwareLoadProperties, String argInstallationPath) {
        logger.traceEntry("buildActions method of AutomationSoftwareActionLoader class");
        List<AutomationSoftwareUpdateAction> automationActions = new ArrayList<>();
        AutomationSoftwareUpdateUtilities automationSoftwareUtiity = new AutomationSoftwareUpdateUtilities();
        try {
            logger.debug("create the object of automationsoftwareupdateAction for all the action defined in XML");
            for (AutomationAction automationAction : list) {
                logger.info("Automation Action {} , {} has been loaded", automationAction.getName(),automationAction.getDescription());
                AutomationSoftwareUpdateAction automationsoftwareupdateAction =
                                (AutomationSoftwareUpdateAction) automationSoftwareUtiity
                                                .createClass(automationAction.getClassName());
                automationsoftwareupdateAction.setName(automationAction.getName());
                automationsoftwareupdateAction.setDescription(automationAction.getDescription());
                automationsoftwareupdateAction.setRollback(automationAction.isRollback());
                automationsoftwareupdateAction.setActive(automationAction.isActive());
                automationsoftwareupdateAction.setExitOnFail(automationAction.isExitOfFail());
                automationsoftwareupdateAction.setInstallationPath(argInstallationPath);
                automationsoftwareupdateAction.setProps(buildPropertiesForAction(automationAction.getProperty(),
                                automationSoftwareLoadProperties.getProperty()));
                automationsoftwareupdateAction
                                .setAutomationSoftwareUpdatePhase(automationAction.getAutomationSoftwareUpdatePhase());
                automationsoftwareupdateAction.setRetryCount(automationAction.getRetryCount());
                automationActions.add(automationsoftwareupdateAction);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new AutomationInstallerException(
                            AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorCode(),
                            exception.getMessage(), exception.getCause());
        }
        logger.traceExit();
        return automationActions;
    }

    /**
     * This method is used to build properties for the action
     * 
     * @param property
     * @return
     * @return Properties
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param properties
     */
    public Properties buildPropertiesForAction(List<AutomationProperty> property, Properties properties) {
        logger.traceEntry("buildPropertiesForAction method of AutomationSoftwareActionLoader class");
        Properties props = new Properties();
        for (AutomationProperty automationProperty : property) {
            if (automationProperty.getPropvalue().contains("$")) {
                // substring the first character from the property value
                props.put(automationProperty.getPropname(), resolveProperty(properties,automationProperty.getPropvalue()));
            } else {
                props.put(automationProperty.getPropname(), automationProperty.getPropvalue());
            }

        }
        Set<Object> propKeys = properties.keySet();
        for (Object prop : propKeys) {
            props.put(prop,resolveProperty(properties, properties.getProperty((String)prop)));
        }
        logger.traceExit();
        return props;
    }

    private String resolveProperty(Properties properties, String propertyKey) {
        while (propertyKey.contains("$")) {
            String referencedProperty = propertyKey.substring(propertyKey.indexOf("${") + 2, propertyKey.indexOf('}'));
            propertyKey = propertyKey.replace("${" + referencedProperty + '}',
                            properties.getProperty(referencedProperty));
        }
        return propertyKey;
    }
}
