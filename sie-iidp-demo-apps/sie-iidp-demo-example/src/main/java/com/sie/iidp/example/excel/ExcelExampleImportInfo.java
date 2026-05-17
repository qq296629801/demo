package com.sie.iidp.example.excel;


import lombok.Data;

import java.util.function.Supplier;

@Data
public class ExcelExampleImportInfo<T extends IExcelExample> {
    private final String sheetName;
    private final Supplier<T> entitySupplier;

    public ExcelExampleImportInfo(String sheetName, Supplier<T> entitySupplier) {
        this.sheetName = sheetName;
        this.entitySupplier = entitySupplier;
    }
}
