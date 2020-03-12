package com.infogain.automation.installer.actions;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.softwareupdate.constants.AutomationInstallerPropertyConstants;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br>
 * <br>
 * 
 * Theme - Core Retail Automation Services<br>
 * Feature - Automation Services - Implement application performance monitoring<br>
 * Description - This is the Action class used for initializing the automation-server installation process
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 17-Oct-2019
 */
public class AutomationInstallationInitializationAction extends AutomationSoftwareUpdateAction {

    private String serviceId;
    private AutomationPhasesErrorCode installationPhase;
    private String xmlDestPath;
    private String jarLocation;
    private String fullBuildXmlLocInJar;
    private String incrementalBuildXmlLocInJar;
    private String jarName;
    private String configFileLocation;
    AutomationCopyResourceFromJarAction automationCopyResourceFromJarAction;

    private static final String JAR_LOC_PROP = "jarLoc";
    private static final String DLL_LOCINJAR_PROP = "dllLocInJar";
    private static final String DEST_PATH_PROP = "destPath";
    private static final String ZIPPED_LOC_PROP = "zippedLoc";
    private static final String JAR_NAME_PROP = "jarName";

    private FileSystem fileSystem = FileSystems.getDefault();
    private static final Logger logger = LogManager.getLogger(AutomationInstallationInitializationAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        serviceId = (String) getProps().get(AutomationSoftwareUpdateConstants.SERVICE_ID);
        xmlDestPath = (String) getProps().get(AutomationSoftwareUpdateConstants.XML_DEST_PATH);
        jarLocation = (String) getProps().get(AutomationSoftwareUpdateConstants.JAR_LOCATION);
        jarName = (String) getProps().get(AutomationSoftwareUpdateConstants.JAR_NAME);
        fullBuildXmlLocInJar = (String) getProps().get(AutomationSoftwareUpdateConstants.FULLBUILD_XML_LOC_IN_JAR);
        incrementalBuildXmlLocInJar =
                        (String) getProps().get(AutomationSoftwareUpdateConstants.INCREMENTAL_XML_LOC_IN_JAR);
        installationPhase = this.getAutomationSoftwareUpdatePhase();
        automationCopyResourceFromJarAction = new AutomationCopyResourceFromJarAction();
    }

    /**
     * This method execute action to determine whether full build or incremental build is required
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationInstallationInitializationAction class");
        init();
        logger.info("Checking if automation-server is already installed to determine whether full build or incremental build is required");

        String serviceStatus;
        int statusCode = 0;

        AutomationStatusAction automationStatusAction =
                        new AutomationStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        automationStatusAction.setInstallationPhase(installationPhase);
        automationStatusAction.setProps(properties);
        automationStatusAction.execute();

        serviceStatus = automationStatusAction.getServiceStatus();

        if (serviceStatus == null) {

            Properties props = new Properties();
            props.setProperty(JAR_LOC_PROP, jarLocation);
            props.setProperty(DLL_LOCINJAR_PROP, fullBuildXmlLocInJar);
            props.setProperty(DEST_PATH_PROP, xmlDestPath);
            props.setProperty(ZIPPED_LOC_PROP, xmlDestPath);
            props.setProperty(JAR_NAME_PROP, jarName);
            automationCopyResourceFromJarAction.setProps(props);
            automationCopyResourceFromJarAction.setAutomationSoftwareUpdatePhase(installationPhase);
            automationCopyResourceFromJarAction.execute();
            statusCode = AutomationSoftwareUpdateConstants.FULL_INSTALLATION_BUILD;
            AutomationPropertyFileUpdateAction automationPropertyFileUpdateAction =
                            new AutomationPropertyFileUpdateAction();
            Properties propertiesToUpdateConfigPropertyFile = new Properties();
            propertiesToUpdateConfigPropertyFile.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION,
                            configFileLocation);
            propertiesToUpdateConfigPropertyFile.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_NAME,
                            AutomationInstallerPropertyConstants.INSTALLATION_LOCATION);
            propertiesToUpdateConfigPropertyFile.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_VALUE,
                            getInstallationPath());
            automationPropertyFileUpdateAction.setProps(propertiesToUpdateConfigPropertyFile);
            automationPropertyFileUpdateAction.execute();
            logger.info("Automation server is not installed on the system Full Build will be triggered");
        } else if (serviceStatus.contains("RUNNING") || serviceStatus.contains("STOPPED")) {
            Properties props = new Properties();
            props.setProperty(JAR_LOC_PROP, jarLocation);
            props.setProperty(DLL_LOCINJAR_PROP, incrementalBuildXmlLocInJar);
            props.setProperty(ZIPPED_LOC_PROP, xmlDestPath);
            props.setProperty(JAR_NAME_PROP, jarName);
            automationCopyResourceFromJarAction.setProps(props);
            automationCopyResourceFromJarAction.setAutomationSoftwareUpdatePhase(installationPhase);
            automationCopyResourceFromJarAction.execute();
            statusCode = AutomationSoftwareUpdateConstants.INCREMENTAL_INSTALLATION_BUILD;
            AutomationReadEnviromentVariableAction automationReadEnviromentVariableAction =
                            new AutomationReadEnviromentVariableAction();
            Properties readEnviromentVariableProperties = new Properties();
            readEnviromentVariableProperties.setProperty(
                            AutomationSoftwareUpdateConstants.AUTOMATION_INSTALLER_ENVIROMENT_VARIABLE,
                            AutomationSoftwareUpdateConstants.AUTOMATION_INSTALLER_LOCATION_ENVIROMENT_VARIABLE_NAME);
            automationReadEnviromentVariableAction.setProps(readEnviromentVariableProperties);
            automationReadEnviromentVariableAction.execute();

            AutomationPropertyFileUpdateAction automationPropertyFileUpdateAction =
                            new AutomationPropertyFileUpdateAction();
            Properties propertiesToUpdateConfigPropertyFile = new Properties();
            propertiesToUpdateConfigPropertyFile.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_FILE_LOCATION,
                            configFileLocation);
            propertiesToUpdateConfigPropertyFile.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_NAME,
                            AutomationInstallerPropertyConstants.INSTALLATION_LOCATION);
            propertiesToUpdateConfigPropertyFile.setProperty(AutomationSoftwareUpdateConstants.PROPERTY_VALUE,
                            automationReadEnviromentVariableAction.getEnviromentVariableValue());
            automationPropertyFileUpdateAction.setProps(propertiesToUpdateConfigPropertyFile);
            automationPropertyFileUpdateAction.execute();
            logger.info("Automation server is installed on the system Incremental Build will be triggered");
        }
        return logger.traceExit(statusCode);
    }

    /**
     * This method cleans up the intermediate file/folders created while performing
     * AutomationInstallationInitializationAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of AutomationInstallationInitializationAction class");
        Properties props = new Properties();
        props.setProperty(JAR_LOC_PROP, jarLocation);
        props.setProperty(DLL_LOCINJAR_PROP, fullBuildXmlLocInJar);
        props.setProperty(DEST_PATH_PROP, xmlDestPath);
        props.setProperty(ZIPPED_LOC_PROP, xmlDestPath);
        props.setProperty(JAR_NAME_PROP, jarName);
        automationCopyResourceFromJarAction.setProps(props);
        automationCopyResourceFromJarAction.setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode.CLEANUP);
        automationCopyResourceFromJarAction.cleanUp();
        return logger.traceExit(0);
    }

    /**
     * This method undo the AutomationInstallationInitializationAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method perform the validation of AutomationInstallationInitializationAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationInstallationInitializationAction class");
        Path xmlDestinationPath = fileSystem.getPath(xmlDestPath);
        logger.debug("Checking destination file {} exists", xmlDestPath);
        return logger.traceExit(xmlDestinationPath.toFile().exists());
    }

    public String getConfigFileLocation() {
        return configFileLocation;
    }

    public void setConfigFileLocation(String configFileLocation) {
        this.configFileLocation = configFileLocation;
    }

}
