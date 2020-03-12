package com.infogain.automation.installer.actions;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;
import com.infogain.automation.installer.exception.AutomationInstallerException;
import com.infogain.automation.softwareupdate.constants.AutomationPhasesErrorCode;
import com.infogain.automation.softwareupdate.constants.AutomationSoftwareUpdateConstants;
import com.infogain.automation.softwareupdate.foundation.ActionStatusInfo;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

public class AutomationPDFGeneration extends AutomationSoftwareUpdateAction {

    private OutputStream pdfOutputStream;
    private Map<String, List<ActionStatusInfo>> reportTables;
    private String finalStatus;
    private String startTime;
    private String endTime;
    private String logoPath;
    private String pdfReportPath;
    private static final Logger logger = LogManager.getLogger(AutomationPDFGeneration.class);



    @Override
    public int cleanUp() {
        logger.info("Calls the cleanUp method of AutomationPDFGeneration class");
        AutomationDeleteAction deleteAction = new AutomationDeleteAction();
        deleteAction.setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode.CLEANUP);
        logger.debug("Creating the object of properties object and setting the value");
        Properties properties = new Properties();
        properties.setProperty(AutomationSoftwareUpdateConstants.DELETION_DIR_PATH, pdfReportPath);
        properties.setProperty(AutomationSoftwareUpdateConstants.CREATE_BACKUP, "false");
        deleteAction.setProps(properties);
        logger.debug("Calling  the execute method of delete action");
        deleteAction.execute();
        return 0;
    }

    private void init() {
        setLogoPath((String) getProps().get(AutomationSoftwareUpdateConstants.LOGO_PATH));
        setPdfReportPath((String) getProps().get(AutomationSoftwareUpdateConstants.REPORT_PATH));
        setAutomationSoftwareUpdatePhase(AutomationPhasesErrorCode.REPORTINGPHASE);
    }

    @Override
    public int execute() {
        logger.info("AutomationPDFGeneration : calling init method ");
        init();
        logger.info("AutomationPDFGeneration : generating PDF Report");
        try {
            Document document = createPDFDocument(pdfReportPath);
            document.open();
            // inserting Logo
            insertLogo(document);
            // Adding Title
            insertTitle(document);
            // Adding Startime and Endtime
            insertTime(document);
            insertNewLine(document);
            // Adding Tables
            insertTable(document);

            // Add final status to the document
            Font finalStatusFont = FontFactory.getFont(FontFactory.HELVETICA, 15, Color.BLACK);
            Chunk finalStatusChunk = new Chunk("Final Status of Automation Server Installation: ", finalStatusFont);
            document.add(finalStatusChunk);
            document.add(insertColoredChunk(finalStatus, 15));
            // Close document and outputStream.
            document.close();
            pdfOutputStream.close();
        } catch (IOException | DocumentException exception) {
            logger.debug(ExceptionUtils.getStackTrace(exception));
            throw new AutomationInstallerException(getAutomationSoftwareUpdatePhase().getErrorCode(),
                            exception.getMessage(), exception.getCause());
        }
        return 0;
    }


    private void insertLogo(Document document) throws IOException, DocumentException {
        Paragraph logoParagraph = new Paragraph();
        Image img = Image.getInstance(getLogoPath());
        img.scaleToFit(100, 80);
        img.setAlignment(1);
        logoParagraph.add(img);
        document.add(logoParagraph);
    }

    private void insertTitle(Document document) throws DocumentException {
        String title = "Automation Server Installation Report";
        Paragraph titleParagraph = new Paragraph();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Chunk titleChunk = new Chunk(title, font);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.add(titleChunk);
        document.add(titleParagraph);
        document.add(new Chunk("\n"));

    }

    private void insertTime(Document document) throws DocumentException {
        Font timefont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Chunk startTimec = new Chunk("Start Time : " + startTime, timefont);
        Chunk endTimec = new Chunk("End Time  : " + endTime, timefont);
        document.add(startTimec);
        document.add(new Chunk("\n", timefont));
        document.add(endTimec);
        document.add(new Chunk("\n", timefont));
    }

    private void insertTable(Document document) throws DocumentException {
        Set<String> phaseSet = reportTables.keySet();
        for (String phase : phaseSet) {
            Font tablePhaseFont = FontFactory.getFont(FontFactory.HELVETICA, 15, Color.BLACK);
            Font tableHeadersFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Color.BLACK);
            Font tableContentFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Chunk phaseNameChunk = new Chunk(phase, tablePhaseFont);
            document.add(phaseNameChunk);
            document.add(new Chunk("\n"));
            PdfPTable actionInfoTable = new PdfPTable(3);
            float[] widths = {45f, 35f, 20f};
            actionInfoTable.setWidths(widths);
            actionInfoTable.setWidthPercentage(100);

            // Creating heading cells
            PdfPCell actionNameHeadingCell = new PdfPCell(new Paragraph(new Chunk("NAME", tableHeadersFont)));
            PdfPCell actionDescriptionHeadingCell =
                            new PdfPCell(new Paragraph(new Chunk("DESCRIPTION", tableHeadersFont)));
            PdfPCell actionResultHeadingCell = new PdfPCell(new Paragraph(new Chunk("RESULT", tableHeadersFont)));
            // Adding heading cells to table
            actionInfoTable.addCell(actionNameHeadingCell);
            actionInfoTable.addCell(actionDescriptionHeadingCell);
            actionInfoTable.addCell(actionResultHeadingCell);
            actionInfoTable.setHeaderRows(1);
            List<ActionStatusInfo> actionsInfoList = reportTables.get(phase);
            for (ActionStatusInfo actionInfo : actionsInfoList) {
                PdfPCell actionNameContentCell =
                                new PdfPCell(new Paragraph(new Chunk(actionInfo.getActionName(), tableContentFont)));
                actionNameContentCell.setPadding(10);
                // actionNameContentCell.set
                PdfPCell actionDescriptionContentCell = new PdfPCell(
                                new Paragraph(new Chunk(actionInfo.getActionDescription(), tableContentFont)));
                Chunk actionStatusChunk;
                if (actionInfo.getActionStatus()) {
                    actionStatusChunk = insertColoredChunk(AutomationSoftwareUpdateConstants.SUCCESS_STATUS, 12);

                } else {
                    actionStatusChunk = insertColoredChunk(AutomationSoftwareUpdateConstants.FAILED_STATUS, 12);
                }

                PdfPCell actionResultContentColumn = new PdfPCell(new Paragraph(actionStatusChunk));
                actionInfoTable.addCell(actionNameContentCell);
                actionInfoTable.addCell(actionDescriptionContentCell);
                actionInfoTable.addCell(actionResultContentColumn);
                tableContentFont.setColor(Color.BLACK);
            }
            document.add(new Chunk("\n"));
            document.add(actionInfoTable);
            document.add(new Chunk("\n"));
        }
    }

    private Document createPDFDocument(String path) throws DocumentException, IOException {
        String pathToDirectory = path.substring(0, path.lastIndexOf('\\'));
        File directory = new File(pathToDirectory);
        directory.mkdir();
        File file = new File(path);
        pdfOutputStream = new FileOutputStream(file);
        Document pdfReport = new Document();
        PdfWriter.getInstance(pdfReport, pdfOutputStream);
        return pdfReport;
    }

    private Chunk insertColoredChunk(String status, int size) {
        Chunk coloredChunk;
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA);
        statusFont.setSize(size);
        if (status.equals(AutomationSoftwareUpdateConstants.SUCCESS_STATUS)) {
            statusFont.setColor(Color.GREEN);
            coloredChunk = new Chunk(AutomationSoftwareUpdateConstants.SUCCESS_STATUS, statusFont);
        } else if (status.equals(AutomationSoftwareUpdateConstants.ACTION_REQUIRED_STATUS)) {
            statusFont.setColor(Color.ORANGE);
            coloredChunk = new Chunk(AutomationSoftwareUpdateConstants.ACTION_REQUIRED_STATUS, statusFont);
        } else {
            statusFont.setColor(Color.RED);
            coloredChunk = new Chunk(AutomationSoftwareUpdateConstants.FAILED_STATUS, statusFont);
        }
        return coloredChunk;
    }

    private void insertNewLine(Document document) throws DocumentException {
        Paragraph straightLine = new Paragraph();
        LineSeparator line = new LineSeparator(1.2f, 100, Color.black, Element.ALIGN_CENTER, -0.5f);
        Chunk newLineChunk = new Chunk(line);
        straightLine.add(newLineChunk);
        document.add(straightLine);
        document.add(new Chunk("\n"));
    }

    @Override
    public boolean validateAction() {
        return false;
    }

    @Override
    public int undoAutomationSoftwareUpdateAction() {
        return 0;
    }

    public Map<String, List<ActionStatusInfo>> getReportTables() {
        return reportTables;
    }

    public void setReportTables(Map<String, List<ActionStatusInfo>> reportTables) {
        this.reportTables = reportTables;
    }

    public String getfinalStatus() {
        return finalStatus;
    }

    public void setfinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getPdfReportPath() {
        return pdfReportPath;
    }

    public void setPdfReportPath(String pdfReportPath) {
        this.pdfReportPath = pdfReportPath;
    }

}
