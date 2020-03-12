package com.infogain.automation.installer.actions;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

public class AutomationSearchAndCopyFileAction extends AutomationCopyAction {

    private String fileName;
    private String fileExtension;
    private String sourceLocation;
    private static final Logger logger = LogManager.getLogger(AutomationSearchAndCopyFileAction.class);

    @Override
    protected void init() {
        super.init();
        fileName = (String) getProps().get(AutomationSoftwareUpdateConstants.FILE_SEARCH_TERM);
        fileExtension = (String) getProps().get(AutomationSoftwareUpdateConstants.FILE_SEARCH_EXTENSION);
        sourceLocation = (String) getProps().get(AutomationSoftwareUpdateConstants.FILE_SRC_PATH);
        source = getFileLocation();
    }

    /**
     * This method return the file full path
     * 
     * @return
     * @since Oct 16, 2019
     * @version 0.0.1
     */
    private String getFileLocation() {

        File files = new File(sourceLocation);
        if (files.isDirectory()) {
            File[] fileList = files.listFiles();
            for (File file : fileList) {
                if (file.getName().contains(fileName) && file.getName().contains(fileExtension)) {
                    sourceLocation = sourceLocation.concat(file.getName());
                    break;
                }
            }
        } else {
            logger.error("Location is not a directory {}", sourceLocation);
            throw new AutomationInstallerException(installationPhase.getErrorCode(),
                            "Location is not a directory" + sourceLocation, null);
        }
        return sourceLocation;
    }
}
