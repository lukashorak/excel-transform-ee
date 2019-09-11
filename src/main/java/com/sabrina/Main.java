package com.sabrina;


import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {

    class DataRow{
        public String type;
        public String symbol;
        public String mcap;
    }
    public static void main(String[] args) throws IOException {
        Main m = new Main();
        m.run();
    }

    File dir;
    String dateFrom;
    String dateTo;

    private void run() throws IOException {
        System.out.println("Running");

        this.readDates();

        File dir = new File("SL\\in\\");
        File dirOut = new File("SL\\out\\");
        if (dir.isDirectory()){
            File[] list = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xls");
                }
            });


            List<DataRow> listDataRow = new ArrayList<>();
            for (File f: list){
                try {

                    String type = f.getName().split("_")[0];
                    listDataRow.addAll(this.readFile(f, type));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(" --> "+listDataRow.size());
            
            List<String> keys = listDataRow.stream().map(f -> {
                return f.type;
            }).collect(Collectors.toList());
            
            for (String k:keys){


                List<DataRow> listToFile = listDataRow.stream().filter(f -> {
                    return Objects.equals(f.type, k);
                }).collect(Collectors.toList());

                this.writeToFile(dirOut, k, listToFile);
            }
        }
    }


    public void writeToFile(File dir, String key, List<DataRow> listToFile){

        File fOut = new File(dir, key+".csv");
        try {
            FileWriter writer = new FileWriter(fOut, false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            for (DataRow r:listToFile){
                bufferedWriter.write(r.type);
                bufferedWriter.write(";");
                bufferedWriter.write(this.dateFrom);
                bufferedWriter.write(";");
                bufferedWriter.write(this.dateTo);
                bufferedWriter.write(";");
                bufferedWriter.write(r.symbol);
                bufferedWriter.write(";");
                bufferedWriter.write(r.mcap);
                bufferedWriter.newLine();
            }



            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readDates() throws IOException {
        File file = new File("SL\\in\\Dates.txt");


        BufferedReader br = new BufferedReader(new FileReader(file));

        String line1 = br.readLine();
        String line2 = br.readLine();

        this.dateFrom = line1.split(":")[1];
        this.dateTo = line2.split(":")[1];

        System.out.println(this.dateFrom + " -> " +this.dateTo);

    }


    public List<DataRow> readFile(File file, String type) throws IOException {

        List<DataRow> list = new ArrayList<>();
        try {

            FileInputStream fis = new FileInputStream(file);

            //Get the workbook instance for XLS file
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            //Get first sheet from the workbook
            HSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();

                DataRow dataRow = new DataRow();
                dataRow.type = type;

                //For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                int p = 0;
                while(cellIterator.hasNext()) {
                    p++;

                    Cell cell = cellIterator.next();

                    String v = "";
                    switch(cell.getCellType()) {
                        case Cell.CELL_TYPE_BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t\t");
                            v = ""+cell.getBooleanCellValue();
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t\t");
                            v = ""+cell.getNumericCellValue();
                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + "\t\t");
                            v = ""+cell.getStringCellValue();
                            break;
                    }
                    if (p==1){
                        dataRow.symbol = v;
                    }else if (p==10 && cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
                        dataRow.mcap = v;
                    }
                }
                if (dataRow.mcap != null && dataRow.mcap != ""){
                    list.add(dataRow);
                }

                System.out.println("");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  list;
    }
}
