package com.sie.iidp.example.unitfmk.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sie.iidp.example.consts.ExcelConstant;
import com.sie.iidp.example.consts.ExportConst;
import com.sie.iidp.example.consts.ImportConst;
import com.sie.iidp.example.consts.MethodConst;
import com.sie.iidp.example.excel.ExcelExampleImportInfo;
import com.sie.iidp.example.excel.IExcelExample;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.constant.AttributesConstant;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.model.ModelMeta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.orm.Shard;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.common.ExportData;
import com.sie.snest.sdk.common.ExportInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Example-单位
 *
 * @author zhoubin
 * @date 2024年03月20日 19:20
 */
@StaticVar
@Getter
@Setter
@Slf4j
@Model(tableName = "example_unit_fmk", name = "example_unit_fmk", description = "Example-单位框架", displayName = "Example-单位框架", isLogicDelete = Bool.False, isAutoLog = Bool.True, isShard = Bool.False, shard = @Shard(shardType = "tables", shardPoint = "create_at:MONTH",
        shardValues = "202212"))
public class ExampleUnitFmk extends BaseModel implements IExcelExample {
    @Validate.NotBlank(message = "单位编码不能为空，请配置编码生成规则")
    @Property(displayName = "编码", columnName = "unit_code", length = 60)
    private String unitCode;

    @Property(displayName = "省编码", columnName = "province_code", length = 60)
    private String provinceCode;

    @Property(displayName = "市编码", columnName = "city_code", length = 60)
    private String cityCode;


    @Validate.Size(max = 60)
    @Validate.NotBlank(message = "单位名称不能为空")
    @Property(displayName = "名称", columnName = "unit_name", length = 60)
    private String unitName;

    @Validate.Size(max = 10)
    @Validate.NotBlank(message = "类型不能为空")
    @Property(displayName = "单位类型")
    @Selection(values = {
            @Option(label = "时间", value = "0"),
            @Option(label = "长度", value = "1"),
            @Option(label = "重量", value = "2"),
    })
    private String unitType;

    @Property(columnName = "effect_day", displayName = "生效日期", dataType = DataType.DATE, dateFormat = "yyyy-MM-dd")
    private Date effectDay;

    @Property(displayName = "启用状态", defaultValue = "0", length = 10)
    @Selection(values = {
            @Option(label = "启用", value = "1"),
            @Option(label = "禁用", value = "0")
    })
    @Validate.NotBlank(message = "启用状态不能为空")
    private String isEnable;

    /**
     * 字段常量
     */
    public static final String FILED_ID = "id";
    public static final String FILED_IS_ENABLE = "isEnable";

    /**
     * 其他常量
     */
    public static final String UNIT_IMPORT = "单位导入";


    /**
     * 启用禁用
     *
     * @param rs
     * @param statusFlag
     * @return
     */
    @MethodService(description = "启用禁用")
    public boolean enableDisable(RecordSet rs, String statusFlag) {
        String[] ids = rs.getIds();
        RecordSet recordSet = (RecordSet) rs.callSuper(null, MethodConst.FIND, Filter.in(FILED_ID, Arrays.asList(ids)), null, null, null);
        if (recordSet.any()) {
            Map<String, Object> valMap = new HashMap<>();
            valMap.put(FILED_IS_ENABLE, statusFlag);
            recordSet.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }

    /*@MethodService(description = "excel导出")
    public void excelExport(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
        // 导出单位信息
        exportUnit(rs, filter, limit, offset, order);
    }*/

    /**
     * 导出单位信息
     *
     * @param rs
     * @param filter
     * @param limit
     * @param offset
     * @param order
     * @throws Exception
     */
    private void exportUnit(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
        getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
        Map<String, List<Map<String, Object>>> exportDataList = new LinkedHashMap<>();
        // 定义要导出的列
        String[] handleProperties = new String[]{"unitCode", "unitName", "unitType", "effectDay", "create_user",
                "create_date", "update_user", "update_date"};
        List<ExampleUnitFmk> unitList = this.search(filter, Arrays.asList(handleProperties), limit, offset, order);
        // 数据判空
        if (CollUtil.isEmpty(unitList)) {
            throw new Exception("没有符合数据");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        // 生成需要导出的信息
        for (ExampleUnitFmk unit : unitList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("编码", unit.getUnitCode());
            map.put("名称", unit.getUnitName());
            result.add(map);
        }
        exportDataList.put("单位导出", result);
        // 设置导出文件名
        String fileName = URLDecoder.decode("单位-", String.valueOf(StandardCharsets.UTF_8)) + DateUtil.format(
                DateUtil.date(), ExportConst.YYYY_MM_DD_HH_MM_SS) + ExportConst.XLSX;
        // 导出文件
        rs.getMeta().get(ExportConst.BASE_EXCEL).call(ExportConst.FILE_EXPORT, exportDataList, fileName);
    }


    /**
     * Excel导入
     *
     * @param data
     * @return
     * @throws ValidationException
     */
    @MethodService(name = "verification")
    public  Map<String, Map<String, List<Map<String, Object>>>> verification(Map<String, List<Map<String, Object>>> data) throws ValidationException {
        if(data.isEmpty()){
            throw new ValidationException("没有任何可导入数据");
        }
        Map<String, Map<String, List<Map<String, Object>>>> result = new HashMap<>();
        List<Map<String, Object>> sheet1 = data.get("Sheet1");
        Map<String, List<Map<String, Object>>> sheet1Map = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        for(Map<String,Object> rowData : sheet1){
            String unitName = (String) rowData.get("名称");
            String unitCode = (String) rowData.get("编码");
            ExampleUnitFmk exampleUnit = new ExampleUnitFmk();
            if(StringUtils.isBlank(unitCode) || unitCode.length() > 6 || StringUtils.isBlank(unitName)){
                exampleUnit.put(ExcelConstant.EXCEL_IMPORT_ERROR_MSG,"编码或者名称不能为空，且编码长度不能超过6位");
            }
            exampleUnit.setUnitName(unitName);
            exampleUnit.setUnitCode(unitCode);
            dataList.add(exampleUnit);

        }
        sheet1Map.put(ExcelConstant.EXCEL_IMPORT_HEADER_LIST,getHeader());
        sheet1Map.put(ExcelConstant.EXCEL_IMPORT_DATA_LIST,dataList);
        result.put("Sheet1",sheet1Map);
        return result;
    }

    private List<Map<String, Object>> getHeader(){
        List<Map<String, Object>> headerList = new ArrayList<>();
        Map map1 = new HashMap();
        map1.put(ExcelConstant.EXCEL_IMPORT_FIELD,"unitCode");
        map1.put(ExcelConstant.EXCEL_IMPORT_TITLE,"编码");
        Map map2 = new HashMap();
        map2.put(ExcelConstant.EXCEL_IMPORT_FIELD,"unitName");
        map2.put(ExcelConstant.EXCEL_IMPORT_TITLE,"名称");
        Map map4 = new HashMap();
        map4.put(ExcelConstant.EXCEL_IMPORT_FIELD, ExcelConstant.EXCEL_IMPORT_ERROR_MSG);
        map4.put(ExcelConstant.EXCEL_IMPORT_TITLE, ExcelConstant.EXCEL_IMPORT_ERROR_MSG_TEXT);
        headerList.add(map1);
        headerList.add(map2);
        headerList.add(map4);
        return headerList;
    }


    @MethodService(description = "excel导入")
    public void excelImport(Map<String, List<Map<String, Object>>> data){
        data.keySet().stream().forEach(key->{
            List<Map<String, Object>> sheetData = data.get(key);
            this.getMeta().get("example_unit").call("create",sheetData);
        });
    }


    /**
     * 单位信息导入
     *
     * @param rs
     * @param fileId
     * @return
     */
    private boolean isHasImportData(RecordSet rs, String fileId) throws Exception {
        ExcelExampleImportInfo<ExampleUnitFmk> itemInfo = new ExcelExampleImportInfo<>(UNIT_IMPORT, ExampleUnitFmk::new);
        List<ExcelExampleImportInfo<?>> infoList = new ArrayList<>();
        infoList.add(itemInfo);
        Meta meta = rs.getMeta();
        String modelName = meta.getModelName();
        RecordSet recordSet = meta.get(modelName);
        if (ObjectUtil.isNull(recordSet)) {
            throw new Exception(String.format("文件导入异常，RecordSet %s 空", modelName));
        }
        ModelMeta modelMeta = recordSet.getModel();
        if (ObjectUtil.isNull(modelMeta)) {
            throw new Exception(String.format("文件导入异常，modelMeta %s 空", modelName));
        }
        Map<String, List<Map<String, Object>>> fileMap = (Map<String, List<Map<String, Object>>>) rs.getMeta().get(ImportConst.BASE_EXCEL).call(ImportConst.FILE_IMPORT, fileId);
        boolean hasImportData = false;
        for (ExcelExampleImportInfo importInfo : infoList) {
            String sheetName = importInfo.getSheetName();
            List<Map<String, Object>> rawDataList = fileMap.get(sheetName);
            if (!CollectionUtils.isEmpty(rawDataList)) {
                hasImportData = true;
            }
            IExcelExample baseImport = (IExcelExample) importInfo.getEntitySupplier().get();
            baseImport.doExcelImport(rawDataList);
        }
        return hasImportData;
    }

    /**
     * 导入
     *
     * @param rawDataList
     * @return
     * @throws Exception
     */
    @Override
    public boolean doExcelImport(List<Map<String, Object>> rawDataList) throws Exception {
        if (CollUtil.isNotEmpty(rawDataList)) {
            BaseContextHandler.getMeta().get(this.getModelName()).create(rawDataList);
        }
        return true;
    }

    @MethodService(description = "excel导出")
    public ExportInfo excelExport(RecordSet rs, Filter filter, List<String> properties) {
        ExportInfo exportInfo = new ExportInfo();

        ExportData exportData = new ExportData();
        List<Map<String, Object>> result = new ArrayList<>();

        this.getMeta().getArguments().put(AttributesConstant.useDisplayForModel, false);
        List<Map<String, Object>> unitList = this.search(filter, properties, 0, 0, null);
        /*for (ExampleUnitFmk unit : unitList) {
            LinkedHashMap<String, Object> unitEntity = new LinkedHashMap<>();
            unitEntity.put("Name", unit.getUnitName());
            unitEntity.put("Code", unit.getUnitCode());
            result.add(unitEntity);
        }*/

        exportInfo.setFileName("调用方自定义文件名.xls");
        exportInfo.setBussName("调用方自定义业务名");
        exportData.putData(exportInfo.getBussName(), unitList);
        exportInfo.setExportData(exportData);

        return exportInfo;
    }
}
