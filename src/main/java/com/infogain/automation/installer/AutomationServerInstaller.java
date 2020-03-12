package com.infogain.automation.installer;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.actions.AutomationEmailNotificationAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationInstallerPropertyConstants;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;
import com.infogain.automation.softwareupdate.foundation.AutomationProject;
import com.infogain.automation.softwareupdate.foundation.AutomationProperty;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the main class used for reading the XML file and for the startup task of automation server
 * installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationServerInstaller {

    private String xmlPath;
    private String configPropertyPath;
    private String installationPath;
    private AutomationSoftwareUpdateProject automationSoftwareUpdateProject;
    private static final Logger logger = LogManager.getLogger(AutomationServerInstaller.class);

    /**
     * @param xmlPath
     * @param argConfigPropertyPath
     * @param argnstallationPath
     */
    public AutomationServerInstaller(String xmlPath, String argConfigPropertyPath, String argnstallationPath) {
        this.xmlPath = xmlPath;
        this.configPropertyPath = argConfigPropertyPath;
        this.installationPath = argnstallationPath;
    }

    /**
     * @param xmlPath
     * @param argConfigPropertyPath
     */
    public AutomationServerInstaller(String xmlPath, String argConfigPropertyPath) {
        this.xmlPath = xmlPath;
        this.configPropertyPath = argConfigPropertyPath;
    }

    /**
     * This method read the Xml file present at a predefined location
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param configPropertyPath2
     */
    private void loadInstallationProject() {
        logger.traceEntry("loadInstallationProject method of AutomationServerInstaller class");
        AutomationProject automationProject = null;
        File fileXml = new File(xmlPath);
        try {
            logger.debug("Reading XML file and Unmarshalling it");
            if (fileXml.exists()) {
                JAXBContext context = JAXBContext.newInstance(AutomationProject.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                automationProject = (AutomationProject) unmarshaller.unmarshal(fileXml);
                automationSoftwareUpdateProject = new AutomationSoftwareUpdateProject(automationProject,
                                configPropertyPath, installationPath);
            } else {
                logger.error("Installer script is not present at provided location, {}",
                                AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorMessage());
                triggerFailureMail(this.configPropertyPath);
            }
        } catch (JAXBException jaxbException) {
            logger.error("Exception occured while parsing {}", fileXml);
            throw new AutomationInstallerException(
                            AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorCode(),
                            jaxbException.getMessage(), jaxbException.getCause());
        }
        logger.trace("Exit loadInstallationProject()");
    }

    /**
     * This method called by the main method having all the startup logic
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param configPropertyPath
     */
    public void startAutomationUpdate() {
        logger.traceEntry("startAutomationUpdate method of AutomationServerInstaller class");
        try {
            loadInstallationProject();
            if (automationSoftwareUpdateProject != null) {
                logger.debug("calls the execute method of automationSoftwareUpdateProject when automationSoftwareUpdateProject object is not null ");
                automationSoftwareUpdateProject.execute();
            } else {
                logger.warn("No actions defined");
            }
        } catch (AutomationInstallerException automationInstallerException) {
            logger.error(ExceptionUtils.getStackTrace(automationInstallerException));
            throw new AutomationInstallerException(
                            AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorCode(),
                            automationInstallerException.getMessage(),
                            automationInstallerException.getCause());
        }

        logger.trace("Exit startAutomationUpdate()");
    }

    /**
     * This method triggers failure in case o errorneous scenario
     * 
     * @return void
     * @since Oct 18, 2019
     * @version 0.0.1
     * @param configPath
     */
    private static void triggerFailureMail(String configPath) {
        logger.traceEntry("triggerFailureMail method of AutomationServerInstaller class");
        AutomationEmailNotificationAction automationEmailNotificationAction = new AutomationEmailNotificationAction();
        AutomationSoftwareLoadProperties automationSoftwareLoadProperties =
                        new AutomationSoftwareLoadProperties(configPath);
        AutomationSoftwareActionLoader propertyResolver = new AutomationSoftwareActionLoader();
        
        List<AutomationProperty> property = new ArrayList<>();
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.EMAIL_FROM,
                        automationSoftwareLoadProperties.getProperty()
                                        .getProperty(AutomationInstallerPropertyConstants.EMAIL_FROM)));
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.PORT, automationSoftwareLoadProperties
                        .getProperty().getProperty(AutomationInstallerPropertyConstants.PORT)));
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.EMAIL_HOST,
                        automationSoftwareLoadProperties.getProperty()
                                        .getProperty(AutomationInstallerPropertyConstants.EMAIL_HOST)));
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.EMAIL_TO, automationSoftwareLoadProperties
                        .getProperty().getProperty(AutomationInstallerPropertyConstants.EMAIL_TO)));
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.ATTACHMENT_PATH,
                        automationSoftwareLoadProperties.getProperty()
                                        .getProperty(AutomationInstallerPropertyConstants.ATTACHMENT_PATH)));
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.EMAIL_SUBJECT, automationSoftwareLoadProperties
                        .getProperty().getProperty(AutomationInstallerPropertyConstants.EMAIL_SUBJECT_FAILURE)));
        property.add(new AutomationProperty(AutomationSoftwareUpdateConstants.MESSAGE_BODY, automationSoftwareLoadProperties
                        .getProperty().getProperty(AutomationInstallerPropertyConstants.MESSAGE_BODY_FAILURE)));
        
        Properties properties = propertyResolver.buildPropertiesForAction(property,
                        automationSoftwareLoadProperties.getProperty());
        automationEmailNotificationAction.setProps(properties);
        logger.debug("Calling  the execute method of email action");
        automationEmailNotificationAction.execute();
        System.exit(AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorCode());
        logger.trace("Exit triggerFailureMail()");
    }

    /**
     * This method validates the Input arguments and returns the result
     * 
     * @param args
     * @return Arguments are correct or not
     */
    private static boolean isInputParametersValidationFailed(String[] args) {
        boolean isInputParameterValidationFailed = false;
        if (args.length < 2 || ((args[0].isEmpty() && args[0] == null) && (args[1].isEmpty() && args[1] == null))) {
            isInputParameterValidationFailed = true;
        }
        return isInputParameterValidationFailed;
    }

    public static void main(String... args) {
        try {
            if (isInputParametersValidationFailed(args)) {
                logger.error("Invalid Arguments, {}",
                                AutomationPhasesErrorCode.AUTOMATION_INSTALLER_FAILURE.getErrorMessage());
                triggerFailureMail(args[1]);
            } else {
                AutomationServerInstaller automationServerInstaller =
                                args.length == 3 ? new AutomationServerInstaller(args[0], args[1], args[2])
                                                : new AutomationServerInstaller(args[0], args[1]);
                automationServerInstaller.startAutomationUpdate();
            }
        } catch (AutomationInstallerException automationInstallerException) {
            logger.error(ExceptionUtils.getStackTrace(automationInstallerException));
            if (!((automationInstallerException.getCause() instanceof SocketException)
                            || (automationInstallerException.getCause() instanceof MessagingException)
                            || (automationInstallerException.getCause() instanceof UnknownHostException)
                            || (automationInstallerException.getCause() instanceof ConnectException))) {
                triggerFailureMail(args[1]);
            }
        }
    }
}
