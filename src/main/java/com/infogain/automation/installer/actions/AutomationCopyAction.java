package com.infogain.automation.installer.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for copying the Folder,SubFoler and Files to predefined folder
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationCopyAction extends AutomationSoftwareUpdateAction {
    protected String source;
    protected String destination;
    protected File sourceFile;
    protected File destFile;
    protected boolean createDestinationIfNotPresent;
    protected boolean wasDestinationDirectoryCreated;
    protected boolean backUpDestinationDirectoryCreatedIfAlreadyExixts;
    protected boolean deleteSourceDirectory;
    protected AutomationPhasesErrorCode installationPhase;
    protected AutomationDeleteAction deleteAction;
    private static final Logger logger = LogManager.getLogger(AutomationCopyAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    protected void init() {
        source = (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_SRC_PATH);
        // check for destination path. if no property value has been set, set the destination to installation path
        destination = (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_DEST_PATH);
        createDestinationIfNotPresent = Boolean.valueOf(
                        (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_CREATE_DIR_NOT_PRESENT));
        deleteSourceDirectory = Boolean
                        .valueOf((String) getProps().get(AutomationSoftwareUpdateConstants.DELETE_SOURCE_DIRECTORY));
        backUpDestinationDirectoryCreatedIfAlreadyExixts =
                        Boolean.valueOf((String) getProps().get(AutomationSoftwareUpdateConstants.BACKUP_DESTINATION));
        installationPhase = this.getAutomationSoftwareUpdatePhase();
        deleteAction = new AutomationDeleteAction();
    }


    /**
     * This method execute the Copy Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationCopyAction class");
        init();
        String newFileNameAppender = "_bckp_";
        sourceFile = new File(source);
        destFile = new File(destination);
        try {
            if (sourceFile.canRead()) {
                if (destFile.exists()) {
                    if (backUpDestinationDirectoryCreatedIfAlreadyExixts) {
                        Files.move(destFile.toPath(), destFile.toPath()
                                        .resolveSibling(destFile + newFileNameAppender + new Date().getTime()));
                    }
                    if (destFile.isDirectory()) {
                        if (backUpDestinationDirectoryCreatedIfAlreadyExixts) {
                            wasDestinationDirectoryCreated = true;
                            FileUtils.forceMkdir(destFile);
                            createBackUpFile(newFileNameAppender);
                        }
                        copySourceFileIntoDestinationDirectory();
                    } else {
                        copySourceFileIntoDestinationFile(newFileNameAppender);
                    }
                    return logger.traceExit(0);
                } else if (createDestinationIfNotPresent) {
                    FileUtils.forceMkdir(destFile);
                    copySourceFileIntoDestinationDirectory();
                    return logger.traceExit(0);
                } else {
                    logger.error("destination does not exist and create destination is false {}", destFile);
                    throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                    "destination does not exist and create destination is false " + destFile, null);
                }
            } else {
                logger.error("unreadable source {} ", sourceFile);
                throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                "unreadable source " + sourceFile, null);
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new AutomationInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        }

    }

    private void copySourceFileIntoDestinationFile(String newFileNameAppender) throws IOException {
        if (sourceFile.isDirectory()) {
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "source is a directory destination is a file " + sourceFile, null);
        } else if (!sourceFile.getName().equals(destFile.getName())) {
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "source and destination file names are different " + sourceFile, null);
        }
        FileUtils.copyFile(sourceFile, destFile);
    }


    private void copySourceFileIntoDestinationDirectory() throws IOException {
        if (sourceFile.isDirectory()) {
            FileUtils.copyDirectory(sourceFile, destFile);
        } else {
            FileUtils.copyFileToDirectory(sourceFile, destFile);
        }
    }

    protected int createBackUpFile(String newFileNameAppender) throws IOException {
        logger.info("Calls the createBackUpFile method of AutomationCopyAction class");
        if (destFile.isDirectory() && destFile.canWrite()) {
            Arrays.stream(destFile.listFiles())
                            .filter(fileObj -> Arrays.asList(sourceFile.list()).contains(fileObj.getName()))
                            .forEach(fileObj -> {
                                try {
                                    FileUtils.moveFile(fileObj, new File(
                                                    fileObj.getPath() + newFileNameAppender + new Date().getTime()));
                                } catch (IOException ioException) {
                                    logger.error(ExceptionUtils.getStackTrace(ioException));
                                    throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                                    ioException.getMessage(), ioException.getCause());
                                }
                            });
            return 0;
        } else {
            logger.error("no write permissions on destination {}", destFile);
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "No write permissions on destination " + destFile, null);
        }
    }

    /**
     * This method undo the copy Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        logger.traceEntry("undoPeripheralSoftwareUpdateAction method of AutomationCopyAction class");
        deleteAction.setAutomationSoftwareUpdatePhase(installationPhase);
        try {
            if (wasDestinationDirectoryCreated) {
                logger.debug("Deleting the {} Directory when destination directory is created", destFile);
                Properties properties = new Properties();
                properties.setProperty(AutomationSoftwareUpdateConstants.DELETION_DIR_PATH, destFile.toString());
                properties.setProperty(AutomationSoftwareUpdateConstants.CREATE_BACKUP, "false");
                deleteAction.setProps(properties);
                logger.debug("Calling  the execute method of delete action");
                deleteAction.execute();
            } else if (!sourceFile.canRead() || !sourceFile.exists()) {
                logger.debug("Source file is not readable or exists {}", sourceFile);
                return 0;
            } else {
                logger.debug("Deleting the internal files of the directory");
                internalFileDelete();
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new AutomationInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        }
        return logger.traceExit(0);
    }

    protected void internalFileDelete() throws IOException {
        logger.info("Calls the internalFileDelete method of AutomationCopyAction class");
        File[] destFiles = destFile.listFiles();
        for (File destfile : destFiles) {
            File[] sorcFiles = new File(source).listFiles();
            if (sorcFiles != null) {
                for (File sourcFile : sorcFiles) {
                    if (FileUtils.contentEquals(destfile, sourcFile))
                        FileUtils.forceDelete(destfile);
                }
            } else {
                logger.error("The {} can't be deleted as it does not exist", sorcFiles);
                break;

            }
        }
    }

    /**
     * This method perform the validation of the copy action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationCopyAction class");
        boolean toReturn = true;
        try {
            logger.debug("Comparing the content of source and destination file");
            if (sourceFile.isFile() && destFile.isFile()) {
                return logger.traceExit(FileUtils.contentEquals(sourceFile, destFile));
            } else if (sourceFile.isFile() && destFile.isDirectory()) {
                File[] destFiles = new File(destFile.getAbsolutePath()).listFiles();
                for (File file : destFiles) {
                    if (file.getName().equals(sourceFile.getName()) && FileUtils.contentEquals(sourceFile, file)) {
                        return logger.traceExit(true);
                    }
                }
                return logger.traceExit(false);
            } else {
                boolean count = false;
                File[] srcFiles = new File(source).listFiles();
                toReturn = validateDirectoryFiles(toReturn, count, srcFiles);
                return logger.traceExit(toReturn);
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new AutomationInstallerException(AutomationPhasesErrorCode.VALIDATIONPHASE.getErrorCode(),
                            ioException.getMessage(), ioException.getCause());
        }
    }

    /**
     * This method perform the validation of the files present inside the directory
     * 
     * @return
     * @since Oct 4, 2019
     */
    protected boolean validateDirectoryFiles(boolean toReturn, boolean count, File[] sorcFiles) throws IOException {
        logger.traceEntry("validateDirectoryFiles method of AutomationCopyAction class");
        if (sorcFiles != null) {
            for (File sourcFile : sorcFiles) {
                File[] destFiles = destFile.listFiles();
                count = validateDirectoryAndFileContent(count, sourcFile, destFiles);
                if (!count) {
                    toReturn = false;
                    break;
                }
                count = false;
            }
        } else {
            logger.error("The {} can't be deleted as it does not exist", Arrays.toString(sorcFiles));
        }
        return logger.traceExit(toReturn);
    }


    private boolean validateDirectoryAndFileContent(boolean count, File sourcFile, File[] destFiles)
                    throws IOException {
        for (File destfile : destFiles) {
            if ((destfile.isDirectory() && destfile.exists()) || (FileUtils.contentEquals(destfile, sourcFile))) {
                count = true;
                break;
            }
        }
        return count;
    }


    /**
     * This method perform the cleanUp action
     * 
     * @return
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of AutomationCopyAction class");
        deleteAction.setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode.CLEANUP);
        if (deleteSourceDirectory && sourceFile.exists()) {
            logger.debug("Creating the object of properties object and setting the value");
            Properties properties = new Properties();
            properties.setProperty(AutomationSoftwareUpdateConstants.DELETION_DIR_PATH, source);
            properties.setProperty(AutomationSoftwareUpdateConstants.CREATE_BACKUP, "false");
            deleteAction.setProps(properties);
            logger.debug("Calling  the execute method of delete action");
            deleteAction.execute();
        }
        return logger.traceExit(0);
    }
}
