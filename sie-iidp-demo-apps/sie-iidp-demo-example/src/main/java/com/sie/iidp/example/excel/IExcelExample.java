package com.sie.iidp.example.excel;

import java.util.List;
import java.util.Map;

public interface IExcelExample {
    boolean doExcelImport(List<Map<String, Object>> rawDataList) throws Exception;
}
