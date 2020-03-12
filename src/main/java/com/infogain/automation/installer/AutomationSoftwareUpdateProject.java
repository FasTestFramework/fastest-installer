package com.infogain.automation.installer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.actions.AutomationEmailNotificationAction;
import com.infogain.automation.installer.actions.AutomationInstallationInitializationAction;
import com.infogain.automation.installer.actions.AutomationInstallationVerificationAction;
import com.infogain.automation.installer.actions.AutomationPDFGeneration;
import com.infogain.automation.softwareupdate.constants.AutomationInstallerPropertyConstants;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;
import com.infogain.automation.softwareupdate.foundation.ActionStatusInfo;
import com.infogain.automation.softwareupdate.foundation.AutomationProject;
import com.infogain.automation.softwareupdate.foundation.AutomationProperty;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for executing the actions automation server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationSoftwareUpdateProject {

    private String projectName;
    private String projectDescription;
    private String installationPath;
    private String configPropertyPath;
    private List<AutomationSoftwareUpdateAction> automationSoftwareUpdateActions = new ArrayList<>();
    private AutomationSoftwareUpdate automationSoftwareUpdate;
    private AutomationSoftwareActionLoader automationSoftwareActionLoader;
    private AutomationSoftwareLoadProperties automationSoftwareLoadProperties;
    private AutomationPDFGeneration automationPDFGeneration;
    private static final Logger logger = LogManager.getLogger(AutomationSoftwareUpdateProject.class);

    public AutomationSoftwareUpdateProject(final AutomationProject project, String configPropertyPath,
                    String argInstallationPath) {
        automationSoftwareUpdate = new AutomationSoftwareUpdate();
        automationSoftwareActionLoader = new AutomationSoftwareActionLoader();
        automationSoftwareLoadProperties = new AutomationSoftwareLoadProperties(configPropertyPath);
        init(project, argInstallationPath);
        this.configPropertyPath = configPropertyPath;
    }

    public void init(AutomationProject project, String argInstallationPath) {
        setProjectName(project.getName());
        setProjectDescription(project.getDescription());
        setAutomationSoftwareUpdateActions(automationSoftwareActionLoader.buildActions(project.getActions(),
                        automationSoftwareLoadProperties, argInstallationPath));
        setInstallationPath(argInstallationPath);
    }

    /**
     * This method execute the action that need to be performed
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public void execute() {
        logger.traceEntry("execute method of AutomationSoftwareUpdateProject class");
        int errorCode;
        boolean isInitialization = false;

        // variables for generating report
        String startTime = getCurrentTime();
        String previousPhase = null;
        Map<String, List<ActionStatusInfo>> reportTables = new LinkedHashMap<>();
        String lastPhase = "";
        String currentPhase = "";

        Iterator<AutomationSoftwareUpdateAction> iterator = automationSoftwareUpdateActions.iterator();
        while (iterator.hasNext()) {
            AutomationSoftwareUpdateAction actionToBePerformed = iterator.next();
            if (actionToBePerformed.isActive() && automationSoftwareUpdate.isPerformFurtherActions()) {
                logger.debug("Calls the Execute method of action manger for each action defined in the xml");
                if (actionToBePerformed instanceof AutomationInstallationInitializationAction) {
                    ((AutomationInstallationInitializationAction) actionToBePerformed)
                                    .setConfigFileLocation(configPropertyPath);
                    isInitialization = true;
                }
                boolean actionStatus = automationSoftwareUpdate.executeAction(actionToBePerformed);
                logger.info("action status" + actionToBePerformed.getName() + " status code "
                                + automationSoftwareUpdate.getStatusCode());
                currentPhase = actionToBePerformed.getAutomationSoftwareUpdatePhase().name();
                ActionStatusInfo pdfReportDTO = new ActionStatusInfo(actionToBePerformed.getName(),
                                actionToBePerformed.getDescription(), actionStatus);
                if (currentPhase.equals(previousPhase)) {
                    reportTables.get(currentPhase).add(pdfReportDTO);
                } else {
                    List<ActionStatusInfo> actionsList = new ArrayList<>();
                    actionsList.add(pdfReportDTO);
                    reportTables.put(currentPhase, actionsList);
                    previousPhase = currentPhase;
                }
            }
            lastPhase = currentPhase;
        }
        errorCode = automationSoftwareUpdate.getStatusCode();
//        AutomationInstallationVerificationAction automationInstallationVerificationAction =
//                        new AutomationInstallationVerificationAction();
//        if (errorCode == 0) {
//            errorCode = automationInstallationVerificationAction.execute();
//            ActionStatusInfo pdfReportDTO = new ActionStatusInfo("AutomationInstallationVerificationAction",
//                            "Verify if automation server is running", errorCode == 0);
//            reportTables.get(lastPhase).add(pdfReportDTO);
//        }
        String finalStatus = getFinalStatus(automationSoftwareUpdate.isActionRequired(), errorCode, isInitialization);
        List<ActionStatusInfo> cleanUpActions = automationSoftwareUpdate.cleanUp();
        if (!cleanUpActions.isEmpty()) {
            reportTables.put(AutomationPhasesErrorCode.CLEANUP.name(), cleanUpActions);
        }
        automationPDFGeneration = new AutomationPDFGeneration();
        if (!isInitialization) {
            generatePDFReport(reportTables, finalStatus, startTime);
        }
        triggerEmailAction(isInitialization);
        logger.trace("Exit execute()");
        System.exit(errorCode);
    }

    /**
     * This method sets properties for email action and executes it.
     * 
     * @return void
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    private void triggerEmailAction(boolean isInitialization) {
        logger.traceEntry("triggerEmailAction method of AutomationSoftwareUpdateProject class");
        int errorCode = automationSoftwareUpdate.getStatusCode();
        boolean actionRequired = automationSoftwareUpdate.isActionRequired();
        String serverInformation = null;
        AutomationEmailNotificationAction automationEmailNotificationAction = new AutomationEmailNotificationAction();
        automationEmailNotificationAction
                        .setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode.REPORTINGPHASE);

        // Creating the Properties Object and setting the values for email action

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
        Properties properties = automationSoftwareActionLoader.buildPropertiesForAction(property,
                        automationSoftwareLoadProperties.getProperty());
        if (!isInitialization) {
            AutomationInstallationVerificationAction automationInstallationVerificationAction =
                            new AutomationInstallationVerificationAction();
            if (errorCode == 0) {
                errorCode = automationInstallationVerificationAction.execute();
            }
            serverInformation = automationInstallationVerificationAction.getServerInformation();
        }
        String temporaryMessageBody = null;
        if (serverInformation != null) {
            temporaryMessageBody = formatServerResponse(serverInformation);
        }
        // Conditionally setting up email subject and body depending upon automation server installation status
        if (actionRequired) {
            // Setting subject and body for action required mail
            properties.setProperty(AutomationSoftwareUpdateConstants.EMAIL_SUBJECT, automationSoftwareLoadProperties
                            .getProperty()
                            .getProperty(AutomationInstallerPropertyConstants.EMAIL_SUBJECT_ACTION_REQUIRED));
            properties.setProperty(AutomationSoftwareUpdateConstants.MESSAGE_BODY,
                            automationSoftwareLoadProperties.getProperty().getProperty(
                                            AutomationInstallerPropertyConstants.MESSAGE_BODY_ACTION_REQUIRED));
        } else if (errorCode > 0) {
            // Setting subject and body for failure mail
            properties.setProperty(AutomationSoftwareUpdateConstants.EMAIL_SUBJECT,
                            automationSoftwareLoadProperties.getProperty().getProperty(
                                            AutomationInstallerPropertyConstants.EMAIL_SUBJECT_FAILURE));
            properties.setProperty(AutomationSoftwareUpdateConstants.MESSAGE_BODY,
                            automationSoftwareLoadProperties.getProperty().getProperty(
                                            AutomationInstallerPropertyConstants.MESSAGE_BODY_FAILURE));
        } else if (errorCode == 0 && !isInitialization) {
            // Setting subject and body for success mail
            properties.setProperty(AutomationSoftwareUpdateConstants.EMAIL_SUBJECT,
                            automationSoftwareLoadProperties.getProperty().getProperty(
                                            AutomationInstallerPropertyConstants.EMAIL_SUBJECT_SUCCESS));
            properties.setProperty(AutomationSoftwareUpdateConstants.MESSAGE_BODY,
                            automationSoftwareLoadProperties.getProperty().getProperty(
                                            AutomationInstallerPropertyConstants.MESSAGE_BODY_SUCCESS)
                                            + temporaryMessageBody);
        }

        // added error code level 2 and 3 for Initialization
        if (!((errorCode == 0 || errorCode == 2 || errorCode == 3) && isInitialization)) {
            automationEmailNotificationAction.setProps(properties);
            // set installation path
            automationEmailNotificationAction.setInstallationPath(getInstallationPath());
            logger.debug("Calling  the execute method of email action");
            automationEmailNotificationAction.execute();
        }
        if (!isInitialization) {
            automationPDFGeneration.cleanUp();
        }

        logger.trace("Exit triggerEmailAction()");
    }

    /**
     * This method formats server response set by AutomationInstallationVerificationAction
     * 
     * @return String
     * @since Oct 17, 2019
     * @version 0.0.1
     */
    private String formatServerResponse(String serverInformation) {
        logger.traceEntry("formatServerResponse method of AutomationSoftwareUpdateProject class");
        String[] temporaryTokens;
        String[] parts;
        StringBuilder formattedData = new StringBuilder();

        String temperaryFormattedResponse = serverInformation.substring(serverInformation.lastIndexOf('{') + 1);
        String formattedResponse = temperaryFormattedResponse.substring(temperaryFormattedResponse.lastIndexOf('{') + 1)
                        .substring(0, temperaryFormattedResponse.indexOf('}'));
        parts = formattedResponse.split(",");
        formattedData.append(
                        "<br><br><table style='border:1px solid; border-collapse:collapse'><tr><th style='border:1px solid'>IP Address</th><th style='border:1px solid'>Host Name</th><th style='border:1px solid'>Build Version</th></tr><tr>");
        for (String token : parts) {
            temporaryTokens = token.split("=");
            formattedData.append("<td style='border:1px solid'>" + temporaryTokens[1] + "</td>");

        }
        formattedData.append("</tr></table> <br><br>Thanks");
        return logger.traceExit(formattedData.toString());
    }

    private void generatePDFReport(Map<String, List<ActionStatusInfo>> reportTables, String finalStatus,
                    String startTime) {
        Properties properties = new Properties();
        properties.setProperty(AutomationSoftwareUpdateConstants.LOGO_PATH, automationSoftwareLoadProperties
                        .getProperty().getProperty(AutomationInstallerPropertyConstants.LOGO_PATH));
        properties.setProperty(AutomationSoftwareUpdateConstants.REPORT_PATH, automationSoftwareLoadProperties
                        .getProperty().getProperty(AutomationInstallerPropertyConstants.REPORT_PATH));
        automationPDFGeneration.setInstallationPath(getInstallationPath());
        automationPDFGeneration.setProps(properties);
        automationPDFGeneration.setfinalStatus(finalStatus);
        automationPDFGeneration.setStartTime(startTime);
        automationPDFGeneration.setEndTime(getCurrentTime());
        automationPDFGeneration.setReportTables(reportTables);
        automationPDFGeneration.execute();
    }

    private String getFinalStatus(boolean isActionRequired, int errorCode, boolean isInitialization) {
        String finalStatus = AutomationSoftwareUpdateConstants.FAILED_STATUS;
        if (isActionRequired) {
            finalStatus = AutomationSoftwareUpdateConstants.ACTION_REQUIRED_STATUS;
        } else if (isInitialization && (errorCode == 2 || errorCode == 3)) {
            // for initialization step if the error code is 2 or 3 mark the final status as success
            finalStatus = AutomationSoftwareUpdateConstants.SUCCESS_STATUS;
        } else if (errorCode == 0) {
            finalStatus = AutomationSoftwareUpdateConstants.SUCCESS_STATUS;
        }
        return finalStatus;
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.format(formatter);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public List<AutomationSoftwareUpdateAction> getAutomationSoftwareUpdateActions() {
        return automationSoftwareUpdateActions;
    }

    public void setAutomationSoftwareUpdateActions(
                    List<AutomationSoftwareUpdateAction> automationSoftwareUpdateActions) {
        this.automationSoftwareUpdateActions = automationSoftwareUpdateActions;
    }

    public String getInstallationPath() {
        return installationPath;
    }

    public void setInstallationPath(String installationPath) {
        this.installationPath = installationPath;
    }

}
