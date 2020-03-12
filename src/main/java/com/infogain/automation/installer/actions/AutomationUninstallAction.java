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
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br>
 * 
 * Theme - Core Retail Automation Services<br>
 * Feature - Automation Services - Implement application performance
 * monitoring.<br>
 * Description - This is the Action class used for uninstalling the automation
 * server
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class AutomationUninstallAction extends AutomationSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceUninstallActionStatus;
    private AutomationPhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(AutomationUninstallAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML
     * file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        winswExeLoc = (String) getProps().get(AutomationSoftwareUpdateConstants.WINSW_EXE_LOC);
        serviceId = (String) getProps().get(AutomationSoftwareUpdateConstants.SERVICE_ID);
        installationPhase=this.getAutomationSoftwareUpdatePhase();
    }

    /**
     * This method execute the Uninstall Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationServerUninstallAction class");
        init();

        logger.debug("checking the state of service before uninstalling automation server");
        String serviceState = getServiceState();
        if (serviceState == null) {
            throw new AutomationInstallerException(installationPhase.getErrorCode(), "Service doesnt exist",
                            null);
        } else if (serviceState.contains("RUNNING")) {
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "Automation server is in running state, please stop the service and try again", null);
        } else if (serviceState.contains("STOPPED")) {
            String installCommand = winswExeLoc + " uninstall";
            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Uninstalling automation server at {}", getCurrentTime());
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
                    winServiceUninstallActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceUninstallActionStatus != 0) {
                    throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while uninstalling automation server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new AutomationInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        }
        return logger.traceExit( 0);
    }
    
    /**
     * This method fetches the current state of the windows service
     * 
     * @return
     * @since 11-Oct-2019
     */
    public String getServiceState() {
        logger.traceEntry("getServiceState method of AutomationServerUninstallAction class");
        AutomationStatusAction automationStatusAction = new AutomationStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        automationStatusAction.setProps(properties);
        automationStatusAction.setInstallationPhase(installationPhase);
        automationStatusAction.execute();
        return logger.traceExit( automationStatusAction.getServiceStatus());
    }

    /**
     * This method undo the Uninstall Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method cleans up the intermediate file/folders created while performing
     * Uninstall action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method perform the validation of the Uninstall action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationServerUninstallAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation == null) {
            logger.info("Automation server has been successsfully uninstalled");
            return logger.traceExit( true);
        } else {
            logger.info("Could not uninstall Automation server");
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
