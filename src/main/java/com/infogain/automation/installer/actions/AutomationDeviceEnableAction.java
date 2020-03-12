package com.infogain.automation.installer.actions;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

public class AutomationDeviceEnableAction extends AutomationSoftwareUpdateAction {

    private static final String AUTOMATION_LIST_INDEXED_KEY = "automation.automationsList[{0}].{1}";
    private Properties props;
    private AutomationPropertyFileUpdateAction automationPropertyFileUpdateAction;
    private PropertiesConfiguration automationServerPropertiesSnapshot;
    private static final Logger logger = LogManager.getLogger(AutomationDeviceEnableAction.class);
    private String[] enabledDevicesArray;

    /**
     * This method initialize the parameters required by execute method
     * 
     * @return int
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    private void init() {
        logger.traceEntry("init method of AutomationServerDeviceEnableAction class");
        props = getProps();
        automationPropertyFileUpdateAction = new AutomationPropertyFileUpdateAction();
        String destination = (String) getProps().get(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION);
        Properties properties = new Properties();
        properties.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION, destination);
        automationPropertyFileUpdateAction.setProps(properties);
        automationPropertyFileUpdateAction.setPropertyMap(Collections.emptyMap());
        automationServerPropertiesSnapshot = automationPropertyFileUpdateAction.getPropertiesSnapshot();
        String enabledDevices = props.getProperty(AutomationSoftwareUpdateConstants.AUTOMATION_ENABLED_DEVICES);
        if (enabledDevices != null && enabledDevices.length() > 0) {
            enabledDevicesArray = enabledDevices.split(",");
        }
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
        final String AUTOMATION_LOGICAL_NAME_KEY = "automationLogicalName";
        final String AUTOMATION_TYPE_KEY = "automationType";
        String automationLogicalNameValue = "";
        String automationTypeValue = "";

        Map<String, String> propertyMap = new HashMap<>();
        boolean isListCorrect = false;
        if (enabledDevicesArray != null) {
            logger.debug("List of enabled device created");
            for (int i = 0; automationServerPropertiesSnapshot.containsKey(
                            MessageFormat.format(AUTOMATION_LIST_INDEXED_KEY, i, AUTOMATION_TYPE_KEY)); i++) {
                automationLogicalNameValue = automationServerPropertiesSnapshot.getString(
                                MessageFormat.format(AUTOMATION_LIST_INDEXED_KEY, i, AUTOMATION_LOGICAL_NAME_KEY));
                automationTypeValue = automationServerPropertiesSnapshot
                                .getString(MessageFormat.format(AUTOMATION_LIST_INDEXED_KEY, i, AUTOMATION_TYPE_KEY));
                if (!isDeviceEnabled(automationLogicalNameValue, automationTypeValue)) {
                    propertyMap.put(MessageFormat.format(AUTOMATION_LIST_INDEXED_KEY, i, "disabled"),
                                    Boolean.toString(true));
                } else {
                    isListCorrect = true;
                }
            }
            if (isListCorrect) {
                automationPropertyFileUpdateAction.setPropertyMap(propertyMap);
                automationPropertyFileUpdateAction.execute();
                logger.debug("Disabled property of device updated in config file");
            }
        }
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

    /**
     * Returns true if the device's type or logical name is in enabled device list
     * 
     * @return boolean
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    private boolean isDeviceEnabled(String automationLogicalName, String automationType) {
        final String AUTOMATION_TYPE_LABEL_PRINTER = "LABEL_PRINTER";
        final String AUTOMATION_TYPE_LABEL_PRINTER_ZEBRA = "ZEBRA";
        if (automationType.equalsIgnoreCase(AUTOMATION_TYPE_LABEL_PRINTER)) {
            for (int i = 0; i < enabledDevicesArray.length; i++) {
                if (enabledDevicesArray[i].equalsIgnoreCase(AUTOMATION_TYPE_LABEL_PRINTER_ZEBRA)) {
                    if (automationLogicalName.toUpperCase().contains(enabledDevicesArray[i].toUpperCase())) {
                        return true;
                    }
                } else {
                    if (automationLogicalName.equalsIgnoreCase(enabledDevicesArray[i])) {
                        return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < enabledDevicesArray.length; i++) {
                if (automationType.equalsIgnoreCase(enabledDevicesArray[i])) {
                    return true;
                }
            }
        }
        return false;
    }

}
