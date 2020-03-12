package com.infogain.automation.installer.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.softwareupdate.constants.AutomationInstallerPropertyConstants;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

public class AutomationUpdatePropertiesFileAction extends AutomationSoftwareUpdateAction {
    private static final String LOCATION_TO_CHANGE = "D:/fasTest";
    private AutomationPropertyFileUpdateAction automationPropertyFileUpdateAction;
    private PropertiesConfiguration automationPropertiesSnapshot;
    private static final Logger logger = LogManager.getLogger(AutomationUpdatePropertiesFileAction.class);
    String installationLocation;

    /**
     * This method initialize the parameters required by execute method
     * 
     * @return int
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    private void init() {
        logger.traceEntry("init method of AutomationServerDeviceEnableAction class");
        Properties props;
        props = getProps();
        automationPropertyFileUpdateAction = new AutomationPropertyFileUpdateAction();
        String destination = (String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION);
        Properties properties = new Properties();
        properties.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION, destination);
        automationPropertyFileUpdateAction.setProps(properties);
        automationPropertyFileUpdateAction.setPropertyMap(Collections.emptyMap());
        automationPropertiesSnapshot = automationPropertyFileUpdateAction.getPropertiesSnapshot();
        installationLocation = props.getProperty(AutomationInstallerPropertyConstants.INSTALLATION_LOCATION);
    }

    /**
     * This method execute the Device Enable Action and updates enabled/disabled devices in the automation server
     * configuration properties file
     * 
     * @return int
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationServerDeviceEnableAction class");
        init();
        Map<String, String> propertyMap = new HashMap<>();
        Iterator<String> keys = automationPropertiesSnapshot.getKeys();
        String key;
        String value;
        while (keys.hasNext()) {
            key = keys.next();
            value = automationPropertiesSnapshot.getString(key);
            if (value.contains(LOCATION_TO_CHANGE)) {
                propertyMap.put(key, value.replace(LOCATION_TO_CHANGE, installationLocation));
            }
        }
        automationPropertyFileUpdateAction.setPropertyMap(propertyMap);
        automationPropertyFileUpdateAction.execute();
        logger.debug("Disabled property of device updated in config file");
        return logger.traceExit(0);
    }

    /**
     * This method undo the changes done by execute method to automation server configuration properties file
     * 
     * @return int
     * @since Jan 31, 2020
     * @version 0.0.1
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return automationPropertyFileUpdateAction.undoAutomationSoftwareUpdateAction();
    }

    /**
     * This method validate that automation server configuration properties are updated
     * 
     * @return boolean
     * @since Jan 31, 2020
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        return automationPropertyFileUpdateAction.validateAction();
    }

    /**
     * Unused
     * 
     * @return boolean
     * @since Jan 31, 2020
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        return 0;
    }
}
