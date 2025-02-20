package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.VideoRecorder;
import utils.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Hooks {
    private final DriverManager driverManager;
    private static final String EXCEL_REPORT_PATH = "target/test-report.xlsx";
    private static Workbook workbook;
    private static Sheet sheet;
    private static int rowNum = 1;
    
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
        initExcelReport();
    }
    
    private void initExcelReport() {
        try {
            // Create new workbook if it doesn't exist
            File excelFile = new File(EXCEL_REPORT_PATH);
            if (!excelFile.exists()) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Test Results");
                
                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Test Name");
                headerRow.createCell(1).setCellValue("Status");
                headerRow.createCell(2).setCellValue("Duration (ms)");
                headerRow.createCell(3).setCellValue("Timestamp");
                headerRow.createCell(4).setCellValue("Error Message");
                
                // Auto-size columns
                for (int i = 0; i < 5; i++) {
                    sheet.autoSizeColumn(i);
                }
                
                // Save workbook
                try (FileOutputStream fileOut = new FileOutputStream(EXCEL_REPORT_PATH)) {
                    workbook.write(fileOut);
                }
            } else {
                // Load existing workbook
                try (FileInputStream fileIn = new FileInputStream(EXCEL_REPORT_PATH)) {
                    workbook = new XSSFWorkbook(fileIn);
                    sheet = workbook.getSheet("Test Results");
                    rowNum = sheet.getLastRowNum() + 1;
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to initialize Excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Before
    public void beforeScenario(Scenario scenario) {
        String testName = scenario.getName().replaceAll("\\s+", "_");
        
        // Add scenario info to Allure report
        Allure.epic("Intrasense E2E Tests");
        Allure.feature(scenario.getId().split(";")[0].replace("-", " "));
        Allure.story(scenario.getName());
        
        // Start video recording
        VideoRecorder.startRecording(driverManager.getDriver(), testName);
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            // Stop video recording and get the file
            File videoFile = VideoRecorder.stopRecording();
            
            if (videoFile != null && videoFile.exists()) {
                // Attach video to Allure report
                try (InputStream videoStream = new FileInputStream(videoFile)) {
                    Allure.addAttachment(
                        "Test Recording",
                        "video/avi",
                        videoStream,
                        "avi"
                    );
                    System.out.println("[Hooks] Video attached successfully: " + videoFile.getAbsolutePath());
                } catch (IOException e) {
                    System.out.println("[Hooks] Failed to attach video: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Take screenshot if scenario fails
            if (scenario.isFailed()) {
                byte[] screenshot = ((TakesScreenshot) driverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
            }
            
            // Update Excel report
            updateExcelReport(scenario);
            
        } finally {
            // Quit driver
            driverManager.quitDriver();
        }
    }
    
    private void updateExcelReport(Scenario scenario) {
        try {
            // Create new row
            Row row = sheet.createRow(rowNum++);
            
            // Add test details
            row.createCell(0).setCellValue(scenario.getName());
            row.createCell(1).setCellValue(scenario.isFailed() ? "FAILED" : "PASSED");
            row.createCell(2).setCellValue(scenario.getId());
            row.createCell(3).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // Add error message if test failed
            if (scenario.isFailed()) {
                String errorMessage = scenario.getStatus().toString();
                row.createCell(4).setCellValue(errorMessage);
            }
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Save workbook
            try (FileOutputStream fileOut = new FileOutputStream(EXCEL_REPORT_PATH)) {
                workbook.write(fileOut);
            }
            
            System.out.println("[Hooks] Excel report updated successfully");
            
        } catch (Exception e) {
            System.out.println("[Hooks] Failed to update Excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 