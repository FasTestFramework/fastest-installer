package com.infogain.automation.installer.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

public class AutomationReadEnviromentVariableAction extends AutomationSoftwareUpdateAction {
    private String enviromentVariable;
    private String enviromentVariableValue;
    private AutomationPhasesErrorCode installationPhase;

    @Override
    public int cleanUp() {
        return 0;
    }

    private void init() {
        enviromentVariable = getProps()
                        .getProperty(AutomationSoftwareUpdateConstants.AUTOMATION_INSTALLER_ENVIROMENT_VARIABLE);
        installationPhase = getAutomationSoftwareUpdatePhase();
    }

    @Override
    public int execute() {
        init();
        Process process = null;
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {

            processBuilder.redirectErrorStream(true);
            String[] commands = {"cmd.exe", "/c", "echo %" + enviromentVariable + "%"};
            processBuilder.command(commands);
            process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String commandOutput = null;
            while ((commandOutput = reader.readLine()) != null) {
                enviromentVariableValue = commandOutput;
            }
            if (StringUtils.isBlank(enviromentVariableValue)) {
                throw new AutomationInstallerException(installationPhase.getErrorCode(),
                                "An exception has occured while starting automation server", null);
            }
        } catch (IOException ioException) {
            throw new AutomationInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return 0;
    }

    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    @Override
    public boolean validateAction() {
        return false;
    }

    public String getEnviromentVariable() {
        return enviromentVariable;
    }

    public void setEnviromentVariable(String enviromentVariable) {
        this.enviromentVariable = enviromentVariable;
    }

    public String getEnviromentVariableValue() {
        return enviromentVariableValue;
    }

    public void setEnviromentVariableValue(String enviromentVariableValue) {
        this.enviromentVariableValue = enviromentVariableValue;
    }


}
