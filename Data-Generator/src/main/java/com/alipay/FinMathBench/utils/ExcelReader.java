/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author luojing.wp
 * @version ExcelReader.java, v 0.1 2025年11月10日 下午5:48 luojing.wp
 */
public class ExcelReader {
    /**
     * Read xlsx file
     *
     * @param filePathStr
     * @return
     * @throws IOException
     */
    public static List<LinkedHashMap<String, String>> readFirstSheetWithHeader(String filePathStr) throws IOException {
        return readFirstSheetWithHeader(filePathStr, 0);
    }

    /**
     * Read xlsx file
     *
     * @param filePathStr
     * @param sheetIdx    Sheet index
     * @return
     * @throws IOException
     */
    public static List<LinkedHashMap<String, String>> readFirstSheetWithHeader(String filePathStr, int sheetIdx) throws IOException {
        List<LinkedHashMap<String, String>> answer = new ArrayList<>();
        InputStream ins = null;
        try {
            ins = ExcelReader.class.getClassLoader().getResource(filePathStr).openStream();
        } catch (Exception e) {
            if (ins == null) {
                try {
                    ins = new FileInputStream(filePathStr);
                } catch (Exception e2) {
                    // ignore
                }
            }
        }
        Workbook workbook = WorkbookFactory.create(ins);
        Sheet sheet = workbook.getSheetAt(sheetIdx);
        if (sheet.getLastRowNum() <= 1) {
            return answer;
        }
        Row titles = sheet.getRow(0);
        LinkedHashMap<Integer, String> titleValIdx = new LinkedHashMap<>();
        for (int j = 0; j < titles.getLastCellNum(); j++) {
            Cell cell = titles.getCell(j);
            titleValIdx.put(j, cell.toString());
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            LinkedHashMap<String, String> rowValList = new LinkedHashMap<>();
            answer.add(rowValList);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    rowValList.put(titleValIdx.get(j), "");
                } else {
                    cell.setCellType(CellType.STRING);
                    rowValList.put(titleValIdx.get(j), cell.toString());
                }
            }
        }
        return answer;
    }
}
