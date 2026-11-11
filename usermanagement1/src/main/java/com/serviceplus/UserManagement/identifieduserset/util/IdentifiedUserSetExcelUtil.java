package com.serviceplus.UserManagement.identifieduserset.util;

import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.identifieduserset.enums.ExcelRowStatus;
import com.serviceplus.UserManagement.identifieduserset.enums.UserAccountStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IdentifiedUserSetExcelUtil {

    public static class ExcelUserRecord {
        private int rowNumber;
        private String email;
        private String mobile;
        private String signNo;
        private String providerUserId;
        private String membershipRole;

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getSignNo() {
            return signNo;
        }

        public void setSignNo(String signNo) {
            this.signNo = signNo;
        }

        public String getProviderUserId() {
            return providerUserId;
        }

        public void setProviderUserId(String providerUserId) {
            this.providerUserId = providerUserId;
        }

        public String getMembershipRole() {
            return membershipRole;
        }

        public void setMembershipRole(String membershipRole) {
            this.membershipRole = membershipRole;
        }
    }

    public static class ExcelRowResult {
        private int rowNumber;
        private String email;
        private String signNo;
        private ExcelRowStatus status; // SUCCESS, WARNING, FAILED
        private String message;

        public ExcelRowResult() {}

        public ExcelRowResult(int rowNumber, String email, String signNo, ExcelRowStatus status, String message) {
            this.rowNumber = rowNumber;
            this.email = email;
            this.signNo = signNo;
            this.status = status;
            this.message = message;
        }

        public ExcelRowResult(int rowNumber, String email, String signNo, String status, String message) {
            this(rowNumber, email, signNo, ExcelRowStatus.fromString(status), message);
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSignNo() {
            return signNo;
        }

        public void setSignNo(String signNo) {
            this.signNo = signNo;
        }

        public ExcelRowStatus getStatus() {
            return status;
        }

        public void setStatus(ExcelRowStatus status) {
            this.status = status;
        }

        public void setStatus(String status) {
            this.status = ExcelRowStatus.fromString(status);
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ExcelMemberExportRow {
        private Long userId;
        private String userName;
        private String signNo;
        private String email;
        private String mobile;
        private String providerId;
        private String role;
        private Boolean active;
        private Date addedOn;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getFullName() {
            return userName;
        }

        public void setFullName(String fullName) {
            this.userName = fullName;
        }

        public String getSignNo() {
            return signNo;
        }

        public void setSignNo(String signNo) {
            this.signNo = signNo;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getSsoProviderId() {
            return providerId;
        }

        public void setSsoProviderId(String ssoProviderId) {
            this.providerId = ssoProviderId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getMembershipRole() {
            return role;
        }

        public void setMembershipRole(String membershipRole) {
            this.role = membershipRole;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Date getAddedOn() {
            return addedOn;
        }

        public void setAddedOn(Date addedOn) {
            this.addedOn = addedOn;
        }
    }

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SPRuntimeError("Please provide a valid non-empty Excel file", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidExcelMimeType(contentType)) {
            throw new SPRuntimeError("Invalid file type. Only Excel (.xlsx) files with valid MIME type are supported", HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean isValidExcelMimeType(String contentType) {
        String ct = contentType.toLowerCase().trim();
        return ct.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || ct.equals("application/vnd.ms-excel")
                || ct.equals("application/x-excel")
                || ct.equals("application/excel")
                || ct.equals("application/octet-stream")
                || ct.equals("application/wps-office.xlsx");
    }

    public static List<ExcelUserRecord> parseExcel(InputStream inputStream) {
        List<ExcelUserRecord> records = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new SPRuntimeError("Excel file is empty or does not contain data rows", HttpStatus.BAD_REQUEST);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new SPRuntimeError("Excel file must contain a valid header row", HttpStatus.BAD_REQUEST);
            }

            int emailCol = -1;
            int mobileCol = -1;
            int signNoCol = -1;
            int providerUserIdCol = -1;
            int roleCol = -1;

            for (Cell cell : headerRow) {
                String headerVal = getCellValueAsString(cell).trim().toLowerCase();
                if (headerVal.contains("email")) {
                    emailCol = cell.getColumnIndex();
                } else if (headerVal.contains("identifier") || headerVal.contains("sign") || headerVal.contains("username") || headerVal.contains("user_id")) {
                    signNoCol = cell.getColumnIndex();
                } else if (headerVal.contains("mobile") || headerVal.contains("phone")) {
                    mobileCol = cell.getColumnIndex();
                } else if (headerVal.contains("provider")) {
                    providerUserIdCol = cell.getColumnIndex();
                } else if (headerVal.contains("role") || headerVal.contains("designation")) {
                    roleCol = cell.getColumnIndex();
                }
            }

            if (emailCol == -1) {
                throw new SPRuntimeError("Invalid Excel structure: Mandatory 'Email' column not found in header", HttpStatus.BAD_REQUEST);
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                ExcelUserRecord record = new ExcelUserRecord();
                record.setRowNumber(r + 1); // 1-based row index in Excel

                record.setEmail(getCellValueAsString(row.getCell(emailCol)).trim());
                if (signNoCol != -1) {
                    record.setSignNo(getCellValueAsString(row.getCell(signNoCol)).trim());
                }
                if (mobileCol != -1) {
                    record.setMobile(getCellValueAsString(row.getCell(mobileCol)).trim());
                }
                if (providerUserIdCol != -1) {
                    record.setProviderUserId(getCellValueAsString(row.getCell(providerUserIdCol)).trim());
                }
                if (roleCol != -1) {
                    record.setMembershipRole(getCellValueAsString(row.getCell(roleCol)).trim());
                }

                records.add(record);
            }

        } catch (SPRuntimeError e) {
            throw e;
        } catch (Exception e) {
            throw new SPRuntimeError("Failed to parse Excel file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return records;
    }

    public static byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Identified_Users_Template");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Email", "User Identifier"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 8000);
            }

            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("user@example.com");
            sampleRow.createCell(1).setCellValue("SP_OFFICIAL_001");

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new SPRuntimeError("Failed to generate Excel template", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static byte[] generateResultExcel(List<ExcelRowResult> results) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Upload_Results");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Row Number", "Email", "Sign No", "Status", "Remarks / Reason"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 7000);
            }

            int rowIdx = 1;
            for (ExcelRowResult res : results) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(res.getRowNumber());
                row.createCell(1).setCellValue(res.getEmail() != null ? res.getEmail() : "");
                row.createCell(2).setCellValue(res.getSignNo() != null ? res.getSignNo() : "");
                row.createCell(3).setCellValue(res.getStatus() != null ? res.getStatus().name() : "");
                row.createCell(4).setCellValue(res.getMessage() != null ? res.getMessage() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new SPRuntimeError("Failed to generate upload result Excel: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static byte[] generateMembersExportExcel(List<ExcelMemberExportRow> members, String userSetName) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String sheetName = userSetName != null && !userSetName.isEmpty() ? userSetName.replaceAll("[^a-zA-Z0-9_ ]", "_") : "Members";
            if (sheetName.length() > 30) {
                sheetName = sheetName.substring(0, 30);
            }
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"User ID", "User Name", "Email", "Mobile", "Provider ID", "Role", "Active Status", "Added On"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowIdx = 1;
            for (ExcelMemberExportRow m : members) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getUserId() != null ? String.valueOf(m.getUserId()) : "");
                row.createCell(1).setCellValue(m.getUserName() != null ? m.getUserName() : (m.getFullName() != null ? m.getFullName() : ""));
                row.createCell(2).setCellValue(m.getEmail() != null ? m.getEmail() : "");
                row.createCell(3).setCellValue(m.getMobile() != null ? m.getMobile() : "");
                row.createCell(4).setCellValue(m.getProviderId() != null ? m.getProviderId() : (m.getSsoProviderId() != null ? m.getSsoProviderId() : ""));
                row.createCell(5).setCellValue(m.getRole() != null ? m.getRole() : (m.getMembershipRole() != null ? m.getMembershipRole() : ""));
                row.createCell(6).setCellValue(Boolean.TRUE.equals(m.getActive()) ? "ACTIVE" : "INACTIVE");
                row.createCell(7).setCellValue(m.getAddedOn() != null ? sdf.format(m.getAddedOn()) : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new SPRuntimeError("Failed to generate members export Excel: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
