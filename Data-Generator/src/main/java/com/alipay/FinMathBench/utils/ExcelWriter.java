/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author luojing.wp
 * @version ExcelWriter.java, v 0.1 2025年11月10日 下午5:49 luojing.wp
 */
public class ExcelWriter {
    /**
     * Write table data and automatically add headers
     *
     * @param filePathStr
     * @param lines
     * @return
     * @throws IOException
     */
    public static boolean appendMapAutoTitle(String filePathStr, List<LinkedHashMap<String, String>> lines) throws IOException {
        if (CollectionUtils.isEmpty(lines)) {
            return false;
        }
        LinkedHashMap<String, String> line = lines.get(0);
        LinkedHashMap<String, String> title = new LinkedHashMap<>();
        line.forEach((k, v) -> title.put(k, k));
        lines.add(0, title);
        return appendMap(filePathStr, lines, 0);
    }

    public static boolean appendMap(String filePathStr, List<LinkedHashMap<String, String>> lines) throws IOException {
        return appendMap(filePathStr, lines, 0);
    }

    /**
     * Save xlsx file
     *
     * @param filePathStr
     * @param lines
     * @return
     * @throws IOException
     */
    public static boolean appendMap(String filePathStr, List<LinkedHashMap<String, String>> lines, int sheetIdx) throws IOException {
        File file = new File(filePathStr);
        Workbook workbook = !file.exists() ? new XSSFWorkbook() : WorkbookFactory.create(new FileInputStream(file));
        int sheetCnt = workbook.getNumberOfSheets();
        if (sheetCnt <= sheetIdx + 1) {
            for (int i = sheetCnt; i < sheetIdx + 1; i++) {
                workbook.createSheet();
            }
        }
        Sheet sheet = workbook.getSheetAt(sheetIdx);
        int rowCnt = sheet.getLastRowNum() + 1;
        for (int i = 0; i < lines.size(); i++) {
            Row row = sheet.createRow(rowCnt + i);
            LinkedHashMap<String, String> line = lines.get(i);
            int j = 0;
            for (Map.Entry<String, String> stringStringEntry : line.entrySet()) {
                Cell cell = row.createCell(j, CellType.STRING);
                try {
                    cell.setCellValue(stringStringEntry.getValue());
                } catch (Exception e) {
                    // ignore
                } finally {
                    if (i == 0 && rowCnt == 1) {
                        sheet.autoSizeColumn(j);
                    }
                    j++;
                }
            }
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.flush();
        workbook.close();
        return true;
    }
}
