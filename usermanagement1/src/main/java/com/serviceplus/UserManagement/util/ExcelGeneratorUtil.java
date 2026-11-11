package com.serviceplus.UserManagement.util;

import com.serviceplus.UserManagement.Dto.ProvisionedUserResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;

public class ExcelGeneratorUtil {

    private static final Logger Logger = LogManager.getLogger("ExcelGeneratorUtil");

    private ExcelGeneratorUtil() {
    }

    public static String generateExcel(String sheetName, String filePath, String reportTitle, List<String> headers, List<Object[]> rows) throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);
        XSSFCellStyle style = createHeaderStyle(workbook);

        createTitleSection(sheet, reportTitle, style);

        Row headerRow = sheet.createRow(4);

        for (int i = 0; i < headers.size(); i++) {

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 6000);
        }

        int rowIndex = 5;

        for (Object[] rowData : rows) {

            Row row = sheet.createRow(rowIndex++);

            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                setCellValue(cell, rowData[i]);
            }
        }

        writeWorkbook(workbook, filePath);
        Logger.info("Excel generated successfully path={}", filePath);

        return filePath;
    }

    private static void createTitleSection(XSSFSheet sheet, String reportTitle, XSSFCellStyle style) {

        Row row1 = sheet.createRow(0);

        Row row2 = sheet.createRow(1);

        Cell cell1 = row1.createCell(0);

        Cell cell2 = row2.createCell(0);

        cell1.setCellValue(reportTitle);
        cell2.setCellValue("Generated");
        cell1.setCellStyle(style);
        cell2.setCellStyle(style);
    }

    private static void setCellValue(Cell cell, Object value) {

        if (isNull(value)) {
            cell.setCellValue("");
            return;
        }

        switch (value) {
            case Integer i -> cell.setCellValue(i);
            case Long l -> cell.setCellValue(l);
            case Double v -> cell.setCellValue(v);
            case Boolean b -> cell.setCellValue(b);
            default -> cell.setCellValue(value.toString());
        }
    }

    private static void createHeaderRow(XSSFSheet sheet, String[] headers, XSSFCellStyle style) {

        Row row = sheet.createRow(4);

        for (int i = 0; i < headers.length; i++) {

            Cell cell = row.createCell(i);

            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);

            sheet.setColumnWidth(i, 6000);
        }
    }

    private static XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {

        XSSFCellStyle style = workbook.createCellStyle();

        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(Boolean.TRUE);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }

    private static void writeWorkbook(XSSFWorkbook workbook, String filePath) throws IOException {

        File file = new File(filePath);

        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
            out.flush();
        }
    }

    public static class RowContext<T> {

        private final Row row;
        private final int index;
        private final Sheet sheet;

        public RowContext(Row row, int index, Sheet sheet) {
            this.row = row;
            this.index = index;
            this.sheet = sheet;
        }

        public Row getRow() {
            return row;
        }

        public int getIndex() {
            return index;
        }

        public Sheet getSheet() {
            return sheet;
        }
    }
}
