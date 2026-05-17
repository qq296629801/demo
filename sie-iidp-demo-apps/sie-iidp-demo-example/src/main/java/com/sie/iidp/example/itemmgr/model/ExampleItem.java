package com.sie.iidp.example.itemmgr.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sie.iidp.example.consts.ExportConst;
import com.sie.iidp.example.consts.ImportConst;
import com.sie.iidp.example.consts.MethodConst;
import com.sie.iidp.example.excel.ExcelExampleImportInfo;
import com.sie.iidp.example.excel.IExcelExample;
import com.sie.meta.plugin.Getter;
import com.sie.meta.plugin.Setter;
import com.sie.meta.plugin.StaticVar;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.model.ModelMeta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.OneToMany;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.orm.Shard;
import com.sie.snest.sdk.annotation.validate.Validate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Example-物料
 *
 * @author zhoubin
 * @date 2024年03月20日 19:20
 */
@Getter
@Setter
@Slf4j
@StaticVar
@Model(tableName = "example_item", name = "example_item", description = "Example-物料", displayName = "Example-物料", isLogicDelete = Bool.False, isAutoLog = Bool.True,
        isShard = Bool.False, shard = @Shard(shardType = "tables", shardPoint = "type:LIST",
        shardValues = "one,two,three"),isPrint=Bool.True)
public class ExampleItem extends BaseModel implements IExcelExample {
    @Validate.NotBlank(message = "物料编码不能为空")
    @Property(displayName = "编码", columnName = "unit_code", length = 60)
    private String itemCode;

    @Validate.Size(max = 60)
    @Validate.NotBlank(message = "物料名称不能为空")
    @Property(displayName = "名称", columnName = "item_name", length = 60)
    private String itemName;

    @Validate.Size(max = 20)
    @Validate.NotBlank(message = "单位不能为空")
    @Selection(model = "example_unit", properties = {"id", "unitName"})
    @Property(displayName = "单位")
    private String unitId;

    @Property(displayName = "启用状态", defaultValue = "0", length = 10)
    @Selection(values = {
            @Option(label = "启用", value = "1"),
            @Option(label = "禁用", value = "0")
    })
    @Validate.NotBlank(message = "启用状态不能为空")
    private String isEnable;

    /**
     * 物料扩展属性
     */
    @OneToMany
    private List<ExampleItemAttribute> itemAttributeList;

    /**
     * 物料基本资料
     */
    @OneToMany
    private List<ExampleItemBasicData> itemBasicDataList;

    /**
     * 物料分类
     */
    @OneToMany
    private List<ExampleItemCate> itemCateList;

    /**
     * 物料打印设置
     */
    @OneToMany
    private List<ExampleItemPrintConfig> itemPrintConfigList;

    /**
     * 字段常量
     */
    public static final String FILED_ID = "id";

    /**
     * 其他常量
     */
    public static final String ITEM_IMPORT = "物料导入";

    /**
     * 导出物料信息
     *
     * @param rs
     * @param filter
     * @param limit
     * @param offset
     * @param order
     * @throws Exception
     */
    @MethodService(description = "excel导出")
    public void excelExport(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
        // 导出物料信息
        exportItem(rs, filter, limit, offset, order);
    }

    /**
     * 导出物料信息
     *
     * @param rs
     * @param filter
     * @param limit
     * @param offset
     * @param order
     */
    private void exportItem(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
        getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
        Map<String, List<Map<String, Object>>> exportDataList = new LinkedHashMap<>();
        // 定义要导出的列
        String[] handleProperties = new String[]{"itemCode", "itemName", "create_user",
                "create_date", "update_user", "update_date"};
        List<ExampleItem> itemList = this.search(filter, Arrays.asList(handleProperties), limit, offset, order);
        // 数据判空
        if (CollUtil.isEmpty(itemList)) {
            throw new ValidationException("没有符合数据");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        // 生成需要导出的信息
        for (ExampleItem item : itemList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("物料编码", item.getItemName());
            map.put("物料名称", item.getItemName());
            result.add(map);
        }
        exportDataList.put("物料导出", result);
        // 设置导出文件名
        String fileName = URLDecoder.decode("物料-", String.valueOf(StandardCharsets.UTF_8)) + DateUtil.format(
                DateUtil.date(), ExportConst.YYYY_MM_DD_HH_MM_SS) + ExportConst.XLSX;
        // 导出文件
        rs.getMeta().get(ExportConst.BASE_EXCEL).call(ExportConst.FILE_EXPORT, exportDataList, fileName);
    }

    /**
     * excel导入
     *
     * @param rs
     * @param fileId
     * @return
     * @throws Exception
     */
    @MethodService(description = "excel导入")
    public boolean excelImport(RecordSet rs, String fileId) throws Exception {
        return isHasImportData(rs, fileId);
    }

    /**
     * 物料信息导入
     *
     * @param rs
     * @param fileId
     * @return
     */
    private boolean isHasImportData(RecordSet rs, String fileId) throws Exception {
        ExcelExampleImportInfo<ExampleItem> itemInfo = new ExcelExampleImportInfo<>(ITEM_IMPORT, ExampleItem::new);
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
            valMap.put(ExampleItem.F_IS_ENABLE, statusFlag);
            recordSet.callSuper(null, MethodConst.UPDATE, valMap);
        }
        return true;
    }

}
