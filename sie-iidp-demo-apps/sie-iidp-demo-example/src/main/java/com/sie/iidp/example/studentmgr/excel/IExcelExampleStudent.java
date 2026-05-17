package com.sie.iidp.example.studentmgr.excel;

import java.util.List;
import java.util.Map;

public interface IExcelExampleStudent {
    boolean doExcelImport(List<Map<String, Object>> rawDataList) throws Exception;
}
