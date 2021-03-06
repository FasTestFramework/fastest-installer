package com.infogain.automation.installer.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

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
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Implement application performance monitoring.<br/>
 * Description - This is the Action class used for starting the automation server
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class AutomationServerStartAction extends AutomationSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceStartActionStatus;
    private AutomationPhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(AutomationServerStartAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        // update the location of winsw to installation folder
        winswExeLoc = (String) getProps().get(AutomationSoftwareUpdateConstants.WINSW_EXE_LOC);
        serviceId = (String) getProps().get(AutomationSoftwareUpdateConstants.SERVICE_ID);
        installationPhase = this.getAutomationSoftwareUpdatePhase();
    }

    /**
     * This method execute the Start Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationServerStartAction class");
        init();

        logger.debug("checking the state of service before starting automation server");
        String serviceState = getServiceState();
        if (serviceState != null && serviceState.contains("STOPPED")) {
            String installCommand = winswExeLoc + " start";
            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Starting automation server at {} ", getCurrentTime());
            builder.command("cmd.exe", "/c", installCommand);

            builder.redirectErrorStream(true);
            Process p;
            try {
                p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    logger.info(line);
                    winServiceStartActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceStartActionStatus != 0) {
                    throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while starting automation server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new AutomationInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        }
        return logger.traceExit(0);
    }

    /**
     * This method fetches the current state of the windows service
     * 
     * @return
     * @since 11-Oct-2019
     */
    public String getServiceState() {
        logger.traceEntry("getServiceState method of AutomationServerStartAction class");
        AutomationStatusAction automationStatusAction =
                        new AutomationStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        automationStatusAction.setProps(properties);
        automationStatusAction.setInstallationPhase(installationPhase);
        automationStatusAction.execute();
        return logger.traceExit(automationStatusAction.getServiceStatus());
    }

    /**
     * This method cleans up the intermediate file/folders created while performing Start action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method undo the Start Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method perform the validation of the Start action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationServerStartAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation != null && serviceStateValidation.contains("RUNNING")) {
            logger.info("Automation server has been started successsfully");
            return logger.traceExit(true);
        } else {
            logger.info("Could not start Automation server");
            return logger.traceExit(false);
        }
    }

    /**
     * This method gets the current local date and time
     * 
     * @return current local date and time in String format
     * @since 11-Oct-2019
     */
    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }
}
