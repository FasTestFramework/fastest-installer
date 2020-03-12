package com.infogain.automation.softwareupdate.constants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the final class conatins the Constant required in automation server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public final class AutomationSoftwareUpdateConstants {


    public static final String EOL = System.getProperty("line.separator");
    public static final String COPY_SRC_PATH = "srcPath";
    public static final String FILE_SRC_PATH = "searchLocation";
    public static final String FILE_SEARCH_TERM = "fileSearchTerm";
    public static final String FILE_SEARCH_EXTENSION = "fileSearchExtension";
    public static final String COPY_DEST_PATH = "destPath";
    public static final String CREATE_DEST_PATH = "destination";
    public static final String COPY_FILE_NAME = "filename";
    public static final String COPY_CREATE_DIR_NOT_PRESENT = "createDirIfNotPresent";
    public static final String JAR_LOCATION = "jarLoc";
    public static final String DLL_LOCATION_IN_JAR = "dllLocInJar";
    public static final String DEST_ZIPPED_LOC = "zippedLoc";
    public static final String DEST_UNZIPPED_LOC = "unZippedLoc";
    public static final String DELETION_DIR_PATH = "delSource";
    public static final String DELETE_SOURCE_DIRECTORY = "deleteSourceDirectory";
    public static final String CREATE_BACKUP = "createBackup";
    public static final String PROPERTY_FILE_LOCATION = "proFileLoc";
    public static final String PROPERTY_NAME = "proName";
    public static final String PROPERTY_VALUE = "proValue";
    public static final String BACKUP_DESTINATION = "backUpDestination";
    public static final String BUILD_TYPE = "buildType";
    public static final String AUTOMATION_ACTION = "AutomationAction";
    public static final String AUTOMATION_PROPERTY = "AutomationProperty";
    public static final String AUTOMATION_PROJECT = "AutomationProject";
    public static final String NAME = "name";
    public static final String EXIT_ON_FAIL = "exitOnFail";
    public static final String ACTIVE = "active";
    public static final String CLASS = "class";
    public static final String FILE_LOC = "fileLoc";
    public static final String PHASE = "automationSoftwareUpdatePhase";
    public static final String WINSW_EXE_LOC = "winswExeLoc";
    public static final String RETRY_COUNT = "retryCount";
    public static final String SERVICE_ID = "serviceId";
    public static final String JAR_NAME = "jarName";
    public static final int FULL_INSTALLATION_BUILD = 2;
    public static final int INCREMENTAL_INSTALLATION_BUILD = 3;
    public static final String AUTOMATION_INSTALLER_LOCATION_ENVIROMENT_VARIABLE_NAME = "AUTOMATION-SERVER-INSTALLED-LOCATION";
    public static final String AUTOMATION_INSTALLER_ENVIROMENT_VARIABLE = "automationInstallerEnviromentVariable";
    public static final String AUTOMATION_ENABLED_DEVICES = "automation.enabledDevices";


    // Constants related to email notification action
    public static final String EMAIL_FROM = "emailFrom";
    public static final String PORT = "port";
    public static final String EMAIL_HOST = "emailHost";
    public static final String EMAIL_TO = "emailTo";
    public static final String EMAIL_SUBJECT = "emailSubject";
    public static final String MESSAGE_BODY = "messageBody";
    public static final String ATTACHMENT_PATH = "attachmentPath";
    public static final String XML_DEST_PATH = "xmlDestPath";
    public static final String FULLBUILD_XML_LOC_IN_JAR = "fullBuildXmlLocInJar";
    public static final String INCREMENTAL_XML_LOC_IN_JAR = "incrementalBuildXmlLocInJar";

    // Constants related to PDF report generation
    public static final String LOGO_PATH = "logoPath";
    public static final String REPORT_PATH = "reportPath";
    public static final String SUCCESS_STATUS = "Success";
    public static final String FAILED_STATUS = "Failed";
    public static final String ACTION_REQUIRED_STATUS = "Action Required";
    
    

    private AutomationSoftwareUpdateConstants() {
        throw new UnsupportedOperationException(AutomationSoftwareUpdateConstants.class.getName());
    }



}
