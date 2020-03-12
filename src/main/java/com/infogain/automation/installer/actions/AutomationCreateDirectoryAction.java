package com.infogain.automation.installer.actions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

public class AutomationCreateDirectoryAction extends AutomationSoftwareUpdateAction {

	String destination;
	private static final Logger logger = LogManager.getLogger(AutomationCreateDirectoryAction.class);
	protected AutomationPhasesErrorCode installationPhase;
	protected boolean wasDestinationDirectoryCreated;
	protected File destFile;
	protected AutomationDeleteAction deleteAction;

	/**
	 * This method initialize the instance variable by reading the value from XML
	 * file
	 * 
	 * @since Oct 4, 2019
	 * @version 0.0.1
	 */
	protected void init() {

		destination = (String) getProps().get(AutomationSoftwareUpdateConstants.CREATE_DEST_PATH);
	}

	@Override
	public int cleanUp() {
		return 0;
	}

	@Override
	public int execute() {
		logger.info("Calls the createDirectory method of AutomationCopyAction class");
		init();
		installationPhase = this.getAutomationSoftwareUpdatePhase();
		try {
			logger.debug("Creating the {} Directory", destination);
			FileUtils.forceMkdir(new File(destination));
			return 0;
		} catch (IOException ioException) {
			logger.error(ExceptionUtils.getStackTrace(ioException));
			throw new AutomationInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
					ioException.getCause());
		}
	}

	@Override
	public int undoAutomationSoftwareUpdateAction() {
		try {
			File file = new File(destination);
			FileUtils.cleanDirectory(file);
			FileUtils.deleteDirectory(file);
		} catch (IOException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}

		return 0;
	}

	@Override
	public boolean validateAction() {
		return new File(destination).exists();
	}

}
