package com.infogain.automation.installer.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
 * Feature - Automation Services - Implement application performance
 * monitoring.<br/>
 * Description - This is the Action class used for checking state of windows service
 * as a windows service
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class AutomationStatusAction extends AutomationSoftwareUpdateAction{

    private String serviceId;
    private String serviceStatus;
    private AutomationPhasesErrorCode installationPhase;
    private int processStatus;
    private static final Logger logger = LogManager.getLogger(AutomationStatusAction.class);
    
    public String getServiceStatus() {
        return serviceStatus;
    }

    public AutomationPhasesErrorCode getInstallationPhase() {
        return installationPhase;
    }

    public void setInstallationPhase(AutomationPhasesErrorCode installationPhase) {
        this.installationPhase = installationPhase;
    }

    /**
     * This method initialize the instance variable by reading the value from XML
     * file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        serviceId = (String) getProps().get(AutomationSoftwareUpdateConstants.SERVICE_ID);
        installationPhase=this.getAutomationSoftwareUpdatePhase();
    }

    /**
     * This method execute action to check the state of service
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationServerServiceStatusAction class");
        init();
        String checkStatusCommand = "sc query "+serviceId+" | FIND \"STATE\"";

        ProcessBuilder builder = new ProcessBuilder();
        logger.info("Checking status of windows service at {} ",getCurrentTime());
        builder.command("cmd.exe", "/c", checkStatusCommand);

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
                serviceStatus = line;
                processStatus = p.waitFor();
            }
            Thread.sleep(15000);
            if (processStatus != 0) {
                throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                "An exception has occured while checking the status of windows service", null);
            }
            p.destroy();
        } catch (Exception exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new AutomationInstallerException(
                    installationPhase.getErrorCode(), exception.getMessage(), exception.getCause());
        }
        return logger.traceExit( 0);
    }

    /**
     * This method cleans up the intermediate file/folders created while performing
     * AutomationServerServiceStatusAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method undo the AutomationServerServiceStatusAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method perform the validation of AutomationServerServiceStatusAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationServerServiceStatusAction class");
        if (processStatus == 0) {
            logger.info("State of windows service successfully fetched");
            return logger.traceExit( true);
        } else {
            logger.info("Could check the state of the windows service");
            return logger.traceExit( false);
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
