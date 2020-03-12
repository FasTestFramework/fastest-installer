package com.infogain.automation.installer.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;
import com.infogain.automation.softwareupdate.utilities.AutomationSoftwareUpdateUtilities;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Implement application performance monitoring.<br/>
 * Description - This is the Action class used for installing automation server as a windows service
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class AutomationServerInstallAction extends AutomationSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceInstallActionStatus;
    private AutomationSoftwareUpdateUtilities automationSoftwareUtility;
    private AutomationPhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(AutomationServerInstallAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        //set winsw location to installer location 
        winswExeLoc =(String) getProps().get(AutomationSoftwareUpdateConstants.WINSW_EXE_LOC);
        serviceId = (String) getProps().get(AutomationSoftwareUpdateConstants.SERVICE_ID);
        installationPhase = this.getAutomationSoftwareUpdatePhase();
        automationSoftwareUtility = new AutomationSoftwareUpdateUtilities();
    }

    /**
     * This method execute the Install Action
     * 
     * @return int
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of AutomationServerInstallAction class");
        init();
        logger.debug("checking the state of service before installing automation server");
        if (getServiceState() == null) {
            String installCommand = winswExeLoc + " install";

            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Installing automation server as windows service at {} ",
                            automationSoftwareUtility.getCurrentTime());
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
                    winServiceInstallActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceInstallActionStatus != 0) {
                    throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while installing automation server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new AutomationInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        } else {
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "Automation server is already installed on the system", null);
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
        logger.traceEntry("getServiceState method of AutomationServerInstallAction class");
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
     * This method cleans up the intermediate file/folders created while performing Install action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method undo the Install action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoAutomationSoftwareUpdateAction() {
        if (getServiceState() != null) {
            logger.info("Service exists proceeding to uninstall the service");
            AutomationUninstallAction automationUninstallAction = new AutomationUninstallAction();
            automationUninstallAction.setAutomationSoftwareUpdatePhase(installationPhase);
            automationUninstallAction.setProps(getProps());
            automationUninstallAction.execute();
        } else {
            logger.debug("Service does not exist Uninstall not required");
        }

        return 0;
   }


    /**
     * This method perform the validation of the Install action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of AutomationServerInstallAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation != null && serviceStateValidation.contains("STOPPED")) {
            logger.info("Automation server has been successsfully installed as a windows service");
            return logger.traceExit(true);
        } else {
            logger.info("Could not install Automation server");
            return logger.traceExit(false);
        }
    }


}
