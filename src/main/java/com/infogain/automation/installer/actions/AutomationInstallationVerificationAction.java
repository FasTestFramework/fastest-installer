package com.infogain.automation.installer.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for verifying automation server installation
 * 
 * @author Divyanshu Varshney [5209377]
 * @version 0.0.1
 * @since 17-Oct-2019
 */
public class AutomationInstallationVerificationAction extends AutomationSoftwareUpdateAction {

    private String serverInformation;
   
    private static final Logger logger = LogManager.getLogger(AutomationInstallationVerificationAction.class);

    
    /**
     * This method execute the verification action for automation server installation
     * 
     * @return int
     * @since Oct 17, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
    
        logger.traceEntry("execute method of AutomationInstallationVerificationAction class");
        try {
        	Thread.sleep(35000);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Object> serverInfoResponse =
                            restTemplate.exchange("http://localhost:8080/automations/fedexoffice/v1/serverinfo",
                                            HttpMethod.GET, requestEntity, Object.class);
            setServerInformation(serverInfoResponse.getBody().toString());
           

        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return logger.traceExit( AutomationPhasesErrorCode.VALIDATIONPHASE.getErrorCode());
        }
        return logger.traceExit( 0);

    }

    
    public String getServerInformation() {
        return serverInformation;
    }


    public void setServerInformation(String serverInformation) {
        this.serverInformation = serverInformation;
    }


    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    @Override
    public boolean validateAction() {
        return false;
    }

    @Override
    public int cleanUp() {
        return 0;
    }

}
