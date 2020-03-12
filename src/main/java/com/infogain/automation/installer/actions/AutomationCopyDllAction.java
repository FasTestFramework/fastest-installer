/**
 * 
 */
package com.infogain.automation.installer.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for copying device dll to provided destination
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 29-Oct-2019
 */
public class AutomationCopyDllAction extends AutomationCopyAction {

	 private static final Logger logger = LogManager.getLogger(AutomationCopyDllAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 29, 2019
     * @version 0.0.1
     */
    @Override
    protected void init() {
    	logger.info("Calling init method of AutomationCopyDllAction class");
        super.init();
        source = (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_SRC_PATH);
        destination = (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_DEST_PATH);
    }

}
