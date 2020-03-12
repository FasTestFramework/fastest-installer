package com.infogain.automation.installer.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationBuildType;
import com.infogain.automation.softwareupdate.constants.AutomationInstallerPropertyConstants;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for creating the backup of folder based on build type
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationBuildInitializationAction extends AutomationSoftwareUpdateAction {

    private String buildType;
    private int countFileExits = 0;
    private AutomationPhasesErrorCode installationPhase;
    private Map<File, File> backupFileList;
    private AutomationCopyAction automationCopyAction;
    private AutomationDeleteAction automationDeleteAction;
    private AutomationStopAction automationStopAction;
    private Properties properties;
    private AutomationServerStartAction automationServerStartAction;
    private String serviceId;
    private static final Logger logger = LogManager.getLogger(AutomationBuildInitializationAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void init() {
        buildType = (String) getProps().get(AutomationSoftwareUpdateConstants.BUILD_TYPE);
        serviceId = (String) getProps().get(AutomationSoftwareUpdateConstants.SERVICE_ID);
        installationPhase = this.getAutomationSoftwareUpdatePhase();
        automationCopyAction = new AutomationCopyAction();
        automationDeleteAction = new AutomationDeleteAction();
        automationStopAction = new AutomationStopAction();
        properties = getProps();
        automationServerStartAction = new AutomationServerStartAction();
    }

    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationBuildInitializationAction class");
        try {
            init();
            /*
             * If build type is incremental, then stop the service and create backup for files and folders whose
             * location is present in propertyconfig file
             */
            if (StringUtils.equalsIgnoreCase(AutomationBuildType.INCREMENTBUILD.name(), buildType)) {
                // get properties from external installer property location
                automationStopAction.setAutomationSoftwareUpdatePhase(installationPhase);
                properties.setProperty(AutomationSoftwareUpdateConstants.WINSW_EXE_LOC, properties
                                .getProperty(AutomationInstallerPropertyConstants.EXECUTABLE_JAR_LOCATION));
                properties.setProperty(AutomationSoftwareUpdateConstants.SERVICE_ID, serviceId);
                automationStopAction.setProps(properties);
                automationStopAction.execute();
                createBackup();
            }
        } catch (Exception exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new AutomationInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                            exception.getCause());
        }
        return logger.traceExit(0);
    }

    /**
     * This method created the backup of all the file and folder whose location is present in property file
     * 
     * @throws ConfigurationException
     * @return void
     * @since Oct 14, 2019
     * @version 0.0.1
     */
    private void createBackup() {
        logger.traceEntry("createBackup method of AutomationBuildInitializationAction class");
        backupFileList = new HashMap<>();
        String propertiesValue =
                        properties.getProperty(AutomationInstallerPropertyConstants.FILE_FOLDER_BACKUP_LIST);
        List<String> propertyValue = Arrays.asList(propertiesValue.split(","));
        propertyValue.forEach(fileLoc -> {
            File sourceFile = new File(fileLoc);
            if (sourceFile.exists()) {
                if (sourceFile.isDirectory()) {
                    createFolderBackup(sourceFile);
                } else {
                    createFileBackup(sourceFile);
                }

            } else {
                logger.error("Source file doesn't exists at defined location {} ", sourceFile);
                throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                "Source file not exists " + sourceFile, null);
            }

        });
        logger.trace("Exit createBackup()");
    }

    private void createFileBackup(File sourceFile) {
        logger.traceEntry("createFileBackup method of AutomationBuildInitializationAction class");
        File backUpFile = new File(sourceFile.getPath() + "_bckp_" + new Date().getTime());
        if (sourceFile.renameTo(backUpFile)) {
            backupFileList.put(backUpFile, sourceFile);
        } else {
            logger.error("Cannot create backup for source {}", sourceFile);
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "Can't create the backup " + sourceFile, null);
        }
        logger.trace("Exit createFileBackup()");
    }

    private void createFolderBackup(File sourceFile) {
        logger.traceEntry("createFolderBackup method of AutomationBuildInitializationAction class");
        try {
            String backupFileName = sourceFile.getPath() + "_bckp_" + new Date().getTime();
            Files.move(sourceFile.toPath(), sourceFile.toPath().resolveSibling(backupFileName));
            backupFileList.put(new File(backupFileName), sourceFile);
        } catch (IOException ioException) {
            logger.error("Cannot create backup for source {}", sourceFile);
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "Can't create the backup " + sourceFile, null);
        }
        logger.trace("Exit createFolderBackup()");
    }


    /**
     * This method used to perform undo action of AutomationBuildInstallationAction
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */

    @Override
    public int undoAutomationSoftwareUpdateAction() {
        logger.traceEntry("undoAutomationSoftwareUpdateAction method of AutomationBuildInitializationAction class");
        if (StringUtils.equalsIgnoreCase(AutomationBuildType.INCREMENTBUILD.name(), buildType)) {
            if (backupFileList != null) {
                backupFileList.forEach((destBackUpFile, sourceFileToBeBackuped) -> {
                    if (destBackUpFile.exists() && destBackUpFile.isDirectory()) {
                        properties.setProperty(AutomationSoftwareUpdateConstants.COPY_SRC_PATH,
                                        destBackUpFile.toString());
                        properties.setProperty(AutomationSoftwareUpdateConstants.COPY_DEST_PATH,
                                        sourceFileToBeBackuped.toString());
                        properties.setProperty(AutomationSoftwareUpdateConstants.COPY_CREATE_DIR_NOT_PRESENT, "true");
                        properties.setProperty(AutomationSoftwareUpdateConstants.DELETE_SOURCE_DIRECTORY, "false");
                        properties.setProperty(AutomationSoftwareUpdateConstants.BACKUP_DESTINATION, "false");
                        automationCopyAction.setAutomationSoftwareUpdatePhase(installationPhase);
                        automationCopyAction.setProps(properties);
                        automationCopyAction.execute();
                        automationDeleteAction.setAutomationSoftwareUpdatePhase(installationPhase);
                        properties.setProperty(AutomationSoftwareUpdateConstants.DELETION_DIR_PATH,
                                        destBackUpFile.toString());
                        automationDeleteAction.setProps(properties);
                        automationDeleteAction.execute();
                    } else if (destBackUpFile.exists() && destBackUpFile.isFile()
                                    && destBackUpFile.renameTo(sourceFileToBeBackuped)) {
                        logger.debug("Source file rename to {} ", sourceFileToBeBackuped);
                    } else {
                        logger.error("Source file not exists {} ", destBackUpFile);
                    }

                });
            }

            automationServerStartAction.setAutomationSoftwareUpdatePhase(installationPhase);
            properties.setProperty(AutomationSoftwareUpdateConstants.WINSW_EXE_LOC, properties
                            .getProperty(AutomationInstallerPropertyConstants.EXECUTABLE_JAR_LOCATION));
            properties.setProperty(AutomationSoftwareUpdateConstants.SERVICE_ID, serviceId);
            automationServerStartAction.setInstallationPath(getInstallationPath());
            automationServerStartAction.setProps(properties);
            automationServerStartAction.execute();
        }
        return logger.traceExit(0);
    }

    /**
     * This method used to perform validation Action AutomationBuildInstallationAction
     * 
     * @return
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationBuildInitializationAction class");
        if (StringUtils.equalsIgnoreCase(AutomationBuildType.INCREMENTBUILD.name(), buildType)) {
            backupFileList.forEach((destBackUpFile, sourceFileToBeBackuped) -> {
                if (destBackUpFile.exists()) {
                    countFileExits++;
                } else {
                    logger.error("Source file doesn't exists {} ", destBackUpFile);
                }

            });

            return logger.traceExit(backupFileList.size() == countFileExits);
        }
        return logger.traceExit(true);
    }

    /**
     * This method perform the cleanUp Action
     * 
     * @return
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of AutomationBuildInitializationAction class");
        // only do clean up when it is an incremental build
        if (StringUtils.equalsIgnoreCase(AutomationBuildType.INCREMENTBUILD.name(), buildType)
                        && backupFileList != null) {
            backupFileList.forEach((destBackUpFile, sourceFileToBeBackuped) -> {
                if (destBackUpFile.exists()) {
                    automationDeleteAction.setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode.CLEANUP);
                    properties.setProperty(AutomationSoftwareUpdateConstants.DELETION_DIR_PATH,
                                    destBackUpFile.toString());
                    automationDeleteAction.setProps(properties);
                    automationDeleteAction.execute();
                } else {
                    logger.error("Source file doesn't exists {} ", destBackUpFile);
                }

            });
        }
        return logger.traceExit(0);

    }


}
