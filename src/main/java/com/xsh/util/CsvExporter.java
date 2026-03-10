package com.xsh.util;

import javafx.collections.ObservableList;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {

    public static boolean exportToCsv(ObservableList<ObservableList<StringProperty>> data, 
                                       List<String> columns, File file) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < columns.size(); i++) {
                writer.write(escapeCsvField(columns.get(i)));
                if (i < columns.size() - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");

            for (ObservableList<StringProperty> row : data) {
                for (int i = 0; i < row.size(); i++) {
                    String value = row.get(i) != null ? row.get(i).getValue() : "";
                    writer.write(escapeCsvField(value));
                    if (i < row.size() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
