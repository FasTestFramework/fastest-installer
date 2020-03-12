package com.infogain.automation.installer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.foundation.ActionStatusInfo;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This class is used to execute every action defined
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationSoftwareUpdate {

    private int actionStackPointer = -1;
    private List<AutomationSoftwareUpdateAction> actionStack = new LinkedList<>();
    private boolean performFurtherActions = true;
    private int statusCode;
    private boolean actionRequired = false;
    private static final Logger logger = LogManager.getLogger(AutomationSoftwareUpdate.class);

    /**
     * This method is used to execute the Action
     * 
     * @param action
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public boolean executeAction(AutomationSoftwareUpdateAction action) {
        logger.traceEntry("executeAction method of AutomationSoftwareUpdate class");
        deleteElementsAfterPointer(actionStackPointer);
        boolean actionStatus = false;
        statusCode = 0;
        try {
            logger.info("Calls the execute method of particular action based on value of performFurtherActions");
            if (performFurtherActions) {
                statusCode = action.execute();
                // if action executed successfully increment the action pointer
                actionStackPointer++;
                actionStack.add(actionStackPointer, action);
                logger.info("Calls the validation method of particular action if execute method exeutes successfully");
                actionStatus = validateAction();
                if (!actionStatus) {
                    if (action.getRetryCount() > 0) {
                        actionStatus = redoAutomationSoftwareUpdateActon();
                    } else {
                        undoAutomationSoftwareUpdateActon();
                    }
                }

            }
        } catch (AutomationInstallerException automationInstallerException) {
            logger.debug("Calling the undo action");
            actionStackPointer++;
            actionStack.add(actionStackPointer, action);
            statusCode = automationInstallerException.getErrorCode();
            try {
                undoAutomationSoftwareUpdateActon();
            } catch (AutomationInstallerException psie) {
                logger.debug("Exception while undoing the action");
                statusCode = psie.getErrorCode();
                setActionRequired(true);
            }
        }
        this.setStatusCode(statusCode);
        logger.trace("Exit executeAction()");
        return actionStatus;
    }

    /**
     * This method used to remove the action from the stack based on the value of undo pointer
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void deleteElementsAfterPointer(int undoRedoPointer) {
        if (actionStack.isEmpty())
            return;
        for (int i = actionStack.size() - 1; i > undoRedoPointer; i--) {
            actionStack.remove(i);
        }
    }

    /**
     * This method undo the Action
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public void undoAutomationSoftwareUpdateActon() {
        logger.traceEntry("undoAutomationSoftwareUpdateActon method of AutomationSoftwareUpdate class");
        AutomationSoftwareUpdateAction action = actionStack.get(actionStackPointer);

        if (action.isRollback() && action.isExitOnFail()) {
            logger.debug("setting the setPerform Further Action action.isExitOnFail()");
            setPerformFurtherActions(!action.isExitOnFail());
            while (!actionStack.isEmpty()) {
                action = actionStack.get(actionStackPointer);
                action.undoAutomationSoftwareUpdateAction();
                actionStack.remove(actionStackPointer);
                actionStackPointer--;
            }
        } else if (action.isRollback() && !action.isExitOnFail()) {
            logger.debug("setting the setPerform Further Action");
            setPerformFurtherActions(!action.isExitOnFail());
            actionRequired = true;
            action = actionStack.get(actionStackPointer);
            action.undoAutomationSoftwareUpdateAction();
            actionStack.remove(actionStackPointer);
            actionStackPointer--;
        } else if (!action.isRollback() && action.isExitOnFail()) {
            logger.debug("setting the setPerform Further Action to action.isExitOnFail() value");
            setPerformFurtherActions(!action.isExitOnFail());
            actionStack.remove(actionStackPointer);
            actionStackPointer--;
            while (!actionStack.isEmpty()) {
                action = actionStack.get(actionStackPointer);
                action.undoAutomationSoftwareUpdateAction();
                actionStack.remove(actionStackPointer);
                actionStackPointer--;
            }
        } else {
            actionRequired = true;
        }
        logger.trace("Exit undoAutomationSoftwareUpdateActon()");
    }

    /**
     * This method retries the action execution until number of retries provided
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public boolean redoAutomationSoftwareUpdateActon() {

        logger.traceEntry("redoAutomationSoftwareUpdateActon method of AutomationSoftwareUpdate class");
        AutomationSoftwareUpdateAction action = actionStack.get(actionStackPointer);
        // fetch the value of retry count for the particular action and calls the execute method
        int retryCount = action.getRetryCount();
        action.setRetryCount(--retryCount);
        action.undoAutomationSoftwareUpdateAction();
        action.execute();
        // if execute method executed successfully calls the validation method of particular action
        boolean actionStatus = validateAction();
        if (!actionStatus) {
            if (action.getRetryCount() > 0) {
                redoAutomationSoftwareUpdateActon();
            } else {
                undoAutomationSoftwareUpdateActon();
            }
        }
        logger.trace("Exit redoAutomationSoftwareUpdateActon()");
        return actionStatus;
    }

    /**
     * This method validate the action performed
     * 
     * @return boolean
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    public boolean validateAction() {
        return actionStack.get(actionStackPointer).validateAction();
    }

    /**
     * This method clean up the action performed and returns List of {@link ActionStatusInfo}
     * 
     * @return {@code List<ActionStatusInfo>} List of {@link ActionStatusInfo}
     * @since Oct 9, 2019
     */
    public List<ActionStatusInfo> cleanUp() {
        boolean isCleanUpSuccess = true;
        List<ActionStatusInfo> cleanUpActions = new ArrayList<>();
        logger.info("Calls the cleanUp method of AutomationSoftwareUpdate class");
        AutomationSoftwareUpdateAction automationSoftwareUpdateAction;
        while (!actionStack.isEmpty()) {
            automationSoftwareUpdateAction = actionStack.get(actionStackPointer);
            try {
                automationSoftwareUpdateAction.cleanUp();
            } catch (AutomationInstallerException automationInstallerException) {
                logger.error(ExceptionUtils.getStackTrace(automationInstallerException));
                isCleanUpSuccess = false;
            }
            ActionStatusInfo actionStatusInfo = new ActionStatusInfo(automationSoftwareUpdateAction.getName(),
                            automationSoftwareUpdateAction.getDescription(), isCleanUpSuccess);
            cleanUpActions.add(actionStatusInfo);
            actionStack.remove(actionStackPointer);
            actionStackPointer--;
        }
        return cleanUpActions;
    }


    public boolean isPerformFurtherActions() {
        return performFurtherActions;
    }

    public void setPerformFurtherActions(boolean performFurtherActions) {
        this.performFurtherActions = performFurtherActions;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public int getActionStackPointer() {
        return actionStackPointer;
    }

    public void setActionStackPointer(int actionStackPointer) {
        this.actionStackPointer = actionStackPointer;
    }

    public List<AutomationSoftwareUpdateAction> getActionStack() {
        return actionStack;
    }

    public void setActionStack(List<AutomationSoftwareUpdateAction> actionStack) {
        this.actionStack = actionStack;
    }



}
