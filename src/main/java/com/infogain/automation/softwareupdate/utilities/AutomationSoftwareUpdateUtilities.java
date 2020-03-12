package com.infogain.automation.softwareupdate.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for creating the class through reflection in automation server installer also it
 * will have utility method
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class AutomationSoftwareUpdateUtilities {
    private static final Logger logger = LogManager.getLogger(AutomationSoftwareUpdateUtilities.class);

    /**
     * This method create the class using reflection
     * 
     * @param fullClassName
     * @return Object
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public Object createClass(String fullClassName)
                    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        logger.traceEntry(
                        "createClass method of AutomationSoftwareUpdateUtilities class that is creating the object of {}",
                        fullClassName);
        try {
            logger.traceExit();
            return Class.forName(fullClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new InstantiationException(
                            fullClassName + AutomationSoftwareUpdateConstants.EOL + exception.getMessage());
        }

    }

    /**
     * This method gets the current local date and time
     * 
     * @return current local date and time in String format
     * @since 11-Oct-2019
     */
    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }
}
