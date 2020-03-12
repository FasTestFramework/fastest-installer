package com.infogain.automation.installer.actions;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Automation Services<br/>
 * Feature - Automation Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for sending email notification to provided group when automation server
 * installation is successful or failed
 * 
 * @author Divyanshu Varshney [5209377]
 * @version 0.0.1
 * @since 9-Oct-2019
 */
public class AutomationEmailNotificationAction extends AutomationSoftwareUpdateAction {

    private String emailFrom;
    private int emailPort;
    private String emailHost;
    private String emailTo;
    private String attachmentPaths;
    private String emailSubject;
    private String messageBody;
    private AutomationPhasesErrorCode installationPhase;
    private static final Logger logger = LogManager.getLogger(AutomationEmailNotificationAction.class);

    /**
     * This method initialize the instance variable through by reading the value from XML file
     * 
     * @return void
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    private void init() {

        this.emailFrom = (String) getProps().get(AutomationSoftwareUpdateConstants.EMAIL_FROM);
        this.emailPort = Integer.parseInt((String) getProps().get(AutomationSoftwareUpdateConstants.PORT));
        this.emailHost = (String) getProps().get(AutomationSoftwareUpdateConstants.EMAIL_HOST);
        this.emailTo = (String) getProps().get(AutomationSoftwareUpdateConstants.EMAIL_TO);
        this.emailSubject = (String) getProps().get(AutomationSoftwareUpdateConstants.EMAIL_SUBJECT);
        this.messageBody = ((String) getProps().get(AutomationSoftwareUpdateConstants.MESSAGE_BODY));
        this.attachmentPaths = (String) getProps().get(AutomationSoftwareUpdateConstants.ATTACHMENT_PATH);
        installationPhase = this.getAutomationSoftwareUpdatePhase();

    }

    /**
     * This method execute the email notification action
     * 
     * @return int
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        org.apache.logging.log4j.message.Message messageLogger =
                        logger.traceEntry("execute method of AutomationEmailNotificationAction class");
        init();
        Properties emailProps = new Properties();
        emailProps.put("mail.smtp.host", this.emailHost);
        emailProps.put("mail.smtp.port", String.valueOf(this.emailPort));
        emailProps.put("mail.smtp.auth", "true");
        emailProps.put("mail.smtp.starttls.enable", "true");
        //emailProps.put("mail.debug", "true");
        // Setting up a mail session
        Session session = Session.getInstance(emailProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("frameworkfastest@gmail.com", "Titans@123");
            }
        });

        try {
            InternetAddress[] receiversAddress = InternetAddress.parse(this.emailTo, true);

            InetAddress addr = InetAddress.getLocalHost();
            String machineName = addr.getHostName();

            // Setting up message properties
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.emailFrom));
            message.setRecipients(Message.RecipientType.TO, receiversAddress);
            Multipart multipart = new MimeMultipart();

            List<String> fileNames = getAttachmentsName(this.attachmentPaths);
            for (String fName : fileNames) {
                File file = new File(fName);
                if (file.exists()) {
                    addAttachment(multipart, fName);
                }
            }
            BodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(this.messageBody, "text/html");
            multipart.addBodyPart(htmlBodyPart);
            message.setContent(multipart);
            this.emailSubject = this.emailSubject + machineName;
            message.setSubject(this.emailSubject);
            logger.debug("Sending email notification");
            Transport.send(message);
        } catch (MessagingException messagingException) {
            logger.error(messagingException.getMessage());
            throw new AutomationInstallerException(installationPhase.getErrorCode(), messagingException.getMessage(),
                            messagingException.getCause());
        } catch (UnknownHostException unknownHostException) {
            logger.error(unknownHostException.getMessage());
            throw new AutomationInstallerException(installationPhase.getErrorCode(), unknownHostException.getMessage(),
                            unknownHostException.getCause());
        }

        return logger.traceExit(messageLogger, 0);
    }

    /**
     * This method adds attachment in the email to be sent for installation process
     * 
     * @param multipart
     * @param fileName
     * @return void
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    private static void addAttachment(Multipart multipart, String fileName) throws MessagingException {
        logger.traceEntry("addAttachment method of AutomationEmailNotificationAction class");
        DataSource source = new FileDataSource(fileName);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName.substring(fileName.lastIndexOf('/')));
        logger.info("Adding attachements in email");
        multipart.addBodyPart(messageBodyPart);
        logger.trace("Exit addAttachment()");
    }

    /**
     * This method gets filenames for attachment to be added in email for installation process
     * 
     * @param attachementsName
     * @return List<String>
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    private List<String> getAttachmentsName(String attachementsName) {
        logger.traceEntry("getAttachmentsName method of AutomationEmailNotificationAction class");
        logger.info("Fetching filename of attachments");
        List<String> attachments = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(attachementsName, "|");
        while (tokenizer.hasMoreTokens()) {
            attachments.add(tokenizer.nextToken());
        }
        return logger.traceExit(attachments);
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
