package com.bingo.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
@Service
public class FileIOService {

    private static final String BINGO_FOLDER_PREFIX = "Bingo_";
    static String bingoFolderName;

    public void writeCallsToCsv(String bingoFolderName, List<Integer> calls) {
        File file = new File(bingoFolderName + "/" + bingoFolderName + "-calls.csv");
        FileWriter csvWriter;
        try {
            csvWriter = new FileWriter(file);

            csvWriter.append("Calls");
            csvWriter.append("\n");
            int i = 1;
            for (Integer rowData : calls) {
                String data = String.valueOf(rowData);
                String callNo = "call #" + i + ": ";
                csvWriter.append(String.join(",", callNo));
                csvWriter.append(String.join(",", data));
                csvWriter.append(",\n");
                i++;
            }
            csvWriter.flush();
            csvWriter.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
    }

    public void createUserFolder(String gameId, String bingoFolderName, String userEmail) {
        String userFolderName = bingoFolderName + "\\" + userEmail + "_" + gameId;
        Path userFolderPath = Paths.get(userFolderName);

        if (!Files.exists(userFolderPath)) {
            try {
                Files.createDirectory(userFolderPath);
            } catch (IOException e1) {
                System.out.println("Directory could not be created : " + userFolderPath);
            }
            System.out.println("Directory created: " + userFolderName);
        } else {
            System.out.println("Directory already exists");
        }
    }

    public String createBingoGameFolder(String gameId) {
        String bingoFolderName = BINGO_FOLDER_PREFIX + gameId;

        Path path = Paths.get(bingoFolderName);

        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Directory created: " + bingoFolderName);
        } else {
            System.out.println("Directory already exists");
        }
        FileIOService.bingoFolderName = bingoFolderName;
        return bingoFolderName;
    }

    @SuppressWarnings("deprecation")
    public List<String> readEmailsFromExcel(String excelFilePath) {
        List<String> emails = new ArrayList<>();
        XSSFWorkbook workbook = null;

        try {
            FileInputStream excelFile = new FileInputStream(new File(excelFilePath));
            workbook = new XSSFWorkbook(excelFile);
            XSSFSheet datatypeSheet = workbook.getSheetAt(0);

            int index = 0;
            for (Row row : datatypeSheet) {
                for (Cell cell : row) {
                    switch (cell.getCellTypeEnum()) {
                        case STRING:
                            if (index != 0) {
                                emails.add(cell.getStringCellValue().trim());
                            }
                            break;
                        default:
                            break;

                    }
                    index++;
                }
                System.out.println();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.out.println("workbook could not be closed.");

            }
        }
        return emails;
    }

    public String getUserSlipPdfName(String gameId, String userEmail) {
        return bingoFolderName + "\\" + userEmail + "_" + gameId + "\\" + userEmail + "_" + gameId + "_slips.pdf";
    }

}
