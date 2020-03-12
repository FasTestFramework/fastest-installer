package com.infogain.automation.installer.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

public class AutomationFileCopyAction extends AutomationSoftwareUpdateAction {
	private String srcFileLoc;
	private String destFileLoc;
	private AutomationPhasesErrorCode installationPhase;
	private AutomationDeleteAction deleteAction;
	private boolean fileCopied;
	private static final Logger logger = LogManager.getLogger(AutomationFileCopyAction.class);

	/**
	 * This method initialize the instance variable by reading the value from XML
	 * file
	 * 
	 * @since Oct 4, 2019
	 * @version 0.0.1
	 */
	private void init() {
		srcFileLoc = (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_SRC_PATH);
		destFileLoc = (String) getProps().get(AutomationSoftwareUpdateConstants.COPY_DEST_PATH);
		installationPhase = this.getAutomationSoftwareUpdatePhase();
		deleteAction = new AutomationDeleteAction();
	}

	/**
	 * This method execute the file Copy action
	 * 
	 * @since Oct 4, 2019
	 * @version 0.0.1
	 */
	@Override
	public int execute() {
	    logger.traceEntry("execute method of AutomationFileCopyAction class");
		init();
		File srcFile = new File(srcFileLoc);
		File destFile = new File(destFileLoc);
		if (srcFile.exists()) {
			try (InputStream inputStream = new FileInputStream(srcFile);
					OutputStream outputStream = new FileOutputStream(destFile)) {
				byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buf)) > 0) {
					outputStream.write(buf, 0, bytesRead);
				}
				fileCopied = true;
			} catch (IOException ioException) {
				logger.error(ExceptionUtils.getStackTrace(ioException));
				throw new AutomationInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
				                ioException.getCause());
			}
		} else {
			logger.error("Source File {} doesn't exists", srcFile);
			throw new AutomationInstallerException(installationPhase.getErrorCode(), "source file doesn't exists "+srcFile,
					null);
		}
		return logger.traceExit( 0);
	}
	
	/**
	 * This method undoAction the file Copy action
	 * 
	 * @since Oct 4, 2019
	 * @version 0.0.1
	 */

	@Override
	public int undoAutomationSoftwareUpdateAction() {
        logger.traceEntry("undoAutomationSoftwareUpdateAction method of AutomationFileCopyAction class");
		if (fileCopied) {
			deleteAction.setAutomationSoftwareUpdatePhase(installationPhase);
			Properties properties = new Properties();
			File destFile = new File(destFileLoc);
			if (destFile.exists()) {
				logger.debug("calling delete action if destination file exists");
				properties.setProperty(AutomationSoftwareUpdateConstants.DELETION_DIR_PATH, destFileLoc);
				properties.setProperty(AutomationSoftwareUpdateConstants.CREATE_BACKUP, "false");
				deleteAction.setProps(properties);
				deleteAction.execute();
			} else {
				logger.error("destiantion File {} doesn't exists", destFile);
			}
		}
		return logger.traceExit( 0);
	}
	/**
	 * This method validateAction the file Copy action
	 * 
	 * @since Oct 4, 2019
	 * @version 0.0.1
	 */
	@Override
	public boolean validateAction() {
	    logger.traceEntry("validateAction method of AutomationFileCopyAction class");
		File destFile = new File(destFileLoc);
		return logger.traceExit( destFile.exists());
	}

	@Override
	public int cleanUp() {

		return 0;
	}

}
