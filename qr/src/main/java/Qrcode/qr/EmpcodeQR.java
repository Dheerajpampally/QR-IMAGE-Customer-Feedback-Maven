package Qrcode.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class EmpcodeQR {

    private static final Logger logger = LogManager.getLogger(EmpcodeQR.class);
    private static final Logger rejectedLogger = LogManager.getLogger("RejectedRecordsLogger");

    public static void main(String[] args) {
        String csvFilePath = "E:\\QRCODE-EMPLOYEE\\EmployeeQR\\EXCEL\\EMPQR.csv";
        String outputDirectory = "E:\\QRCODE-EMPLOYEE\\TEST_QR\\";

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
            String[] nextRecord;

            // Skip header row
            csvReader.readNext();

            while ((nextRecord = csvReader.readNext()) != null) {
                // Validate the row has the correct number of columns
                if (nextRecord.length < 2) {
                    logger.warn("Skipping invalid row: " + String.join(",", nextRecord));
                    rejectedLogger.error("Invalid row: " + String.join(",", nextRecord));
                    continue;
                }

                // Trim data to remove any leading or trailing spaces and illegal characters
                String companyCode = "01";
                String employeeCode = nextRecord[0].trim().replaceAll("[^a-zA-Z0-9]", ""); // employeeCode contains only alphanumeric characters
                String employeeName = nextRecord[1].trim().replaceAll("[^a-zA-Z\\s]", "");

                try {
                    // Construct the URL with query parameters
                    String url = "https://test-www.unimoni.in/customer-feedback-emp.html?company_code=" + companyCode +
                            "&employee_code=" + employeeCode +
                            "&employee_name=" + employeeName;     

                    // Set the file name as employeeCode.jpeg
                    String fileName = employeeCode + ".jpeg";
                    String filePath = outputDirectory + fileName;

                    // Generate QR code
                    generateQRCodeImage(url, 350, 350, filePath);
                } catch (Exception e) {
                    logger.error("Error generating QR Code for record: " + String.join(",", nextRecord), e);
                    rejectedLogger.error("Failed record: " + String.join(",", nextRecord), e);
                }
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file", e);
        } catch (InvalidPathException e) {
            logger.error("Invalid file path specified", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
        }
    }

    public static void generateQRCodeImage(String url, int width, int height, String filePath) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "JPEG", path);
            logger.info("QR Code generated and saved to " + filePath);
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR Code", e);
            throw new RuntimeException("Error generating QR Code", e);
        }
    }
}
