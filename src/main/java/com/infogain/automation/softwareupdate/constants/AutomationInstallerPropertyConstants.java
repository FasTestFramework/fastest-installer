package com.infogain.automation.softwareupdate.constants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the final class containing constants to be used for installer config property file
 * 
 * @author Divyanshu Varshney [5209377]
 * @version 0.0.1
 * @since 18-Oct-2019
 */
public final class AutomationInstallerPropertyConstants {

    public static final String FILE_FOLDER_BACKUP_LIST = "fastest.installer.fileToBeBackuped";
    public static final String EXECUTABLE_JAR_LOCATION = "fastest.installer.ExecutableJarFileLocation";
    public static final String EMAIL_FROM = "fastest.installer.email.emailFrom";
    public static final String PORT = "fastest.installer.email.port";
    public static final String EMAIL_HOST = "fastest.installer.email.emailHost";
    public static final String EMAIL_TO = "fastest.installer.email.emailTo";
    public static final String EMAIL_SUBJECT_SUCCESS = "fastest.installer.email.emailSubjectSuccess";
    public static final String EMAIL_SUBJECT_FAILURE = "fastest.installer.email.emailSubjectFailure";
    public static final String EMAIL_SUBJECT_ACTION_REQUIRED = "fastest.installer.email.emailSubjectActionRequired";
    public static final String MESSAGE_BODY_SUCCESS = "fastest.installer.email.messageBodySuccess";
    public static final String MESSAGE_BODY_FAILURE = "fastest.installer.email.messageBodyFailure";
    public static final String MESSAGE_BODY_ACTION_REQUIRED = "fastest.installer.email.messageBodyActionRequired";
    public static final String ATTACHMENT_PATH = "fastest.installer.email.attachmentPath";
    public static final String LOGO_PATH = "fastest.installer.report.logoPath";
    public static final String REPORT_PATH = "fastest.installer.report.pdf.path";
    public static final String INSTALLATION_LOCATION = "fastest.installer.installationLocation";

    private AutomationInstallerPropertyConstants() {
        throw new UnsupportedOperationException(AutomationInstallerPropertyConstants.class.getName());
    }



}
