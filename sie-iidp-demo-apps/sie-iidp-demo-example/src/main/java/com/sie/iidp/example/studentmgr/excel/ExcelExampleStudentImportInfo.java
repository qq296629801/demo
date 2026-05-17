package com.sie.iidp.example.studentmgr.excel;


import lombok.Data;

import java.util.function.Supplier;

@Data
public class ExcelExampleStudentImportInfo<T extends IExcelExampleStudent> {
    private final String sheetName;
    private final Supplier<T> entitySupplier;

    public ExcelExampleStudentImportInfo(String sheetName, Supplier<T> entitySupplier) {
        this.sheetName = sheetName;
        this.entitySupplier = entitySupplier;
    }
}
