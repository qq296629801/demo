package com.sie.iidp.example.validate.model;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sie.iidp.example.consts.ModelConst;
import com.sie.iidp.example.text.model.ExampleText;
import com.sie.iidp.example.utils.dto.ExampleBaseOptions;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author syh
 * @Date 2024/4/8 10:16
 */
@Model(tableName = "example_validate", name = "example_validate", description = "Example-Name属性查询验证", displayName = "Example文本编辑器",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleValidate extends BaseModel<ExampleValidate> {
    @Validate.NotBlank(message = "编码不能为空")
    @Property(displayName = "编码")
    private String code;
    @Property(displayName = "名称")
    private String name;

    @Property(displayName = "数量", defaultValue = "0")
    private Integer qty;

    @Validate.NotBlank(message = "物料不能为空")
    @Property(displayName = "物料")
    @Selection(method = "selectItem")
    private String itemName;

    @Property(displayName = "单位")
    @Selection(method = "selectUnit")
    private String unitName;

    @Property(displayName = "可空枚举测试")
    @Selection(values = {
            @Option(label = "test1", value = "1"),
            @Option(label = "test2", value = "2"),
            @Option(label = "test3", value = "3")
    })
    private String enumerate;

    @Property(displayName = "日期", dataType = DataType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss", defaultValue="2024-04-08 00:00:00")
    private Date date;

    /**
     * 导出
     *
     * @param rs         元模型
     * @param filter     查询条件
     * @param properties 值
     * @param limit      分几页
     * @param offset     一页几条数据
     * @param order
     * @throws Exception
     */
    @MethodService(description = "excel导出")
    public void excelExport(RecordSet rs, Filter filter, List<String> properties, Integer limit, Integer offset, String order) throws Exception {
        getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
        Map<String, List<Map<String, Object>>> exportDataList = new LinkedHashMap<>();
        String[] handleProperties = new String[]{"code", "name", "qty", "itemName", "unitName", "enumerate","date", "create_user", "create_date", "update_user", "update_date"};
        List<ExampleValidate> exampleValidateList = this.search(filter, Arrays.asList(handleProperties), limit, offset, order);
        if (CollectionUtils.isEmpty(exampleValidateList)) {
            throw new Exception("没有符合数据");
        }
        for (Map<String, Object> map : exampleValidateList) {
            JSONObject itemName = JSONObject.parseObject(JSON.toJSONString(((ExampleText) map).get("itemName")));
            map.put("itemName", ObjectUtils.isEmpty(itemName) ? null : itemName.getString("name"));
            JSONObject unitName = JSONObject.parseObject(JSON.toJSONString(((ExampleText) map).get("unitName")));
            map.put("unitName", ObjectUtils.isEmpty(unitName) ? null : unitName.getString("name"));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExampleValidate exampleValidate : exampleValidateList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("编码", exampleValidate.getCode());
            map.put("名称", exampleValidate.getName());
            map.put("数量", exampleValidate.getQty());
            map.put("物料", exampleValidate.getItemName());
            map.put("单位", exampleValidate.getUnitName());
            map.put("可空枚举测试", exampleValidate.getEnumerate());
            map.put("日期", exampleValidate.getDate());
            map.put("创建人", exampleValidate.getCreate_user());
            map.put("创建时间", exampleValidate.getCreate_date());
            map.put("修改人", exampleValidate.getUpdate_user());
            map.put("修改时间", exampleValidate.getUpdate_date());
            if ("1".equals(exampleValidate.getEnumerate())) {
                map.put("可空枚举测试", "test1");
            } else if ("2".equals(exampleValidate.getEnumerate())) {
                map.put("可空枚举测试", "test2");
            } else {
                map.put("可空枚举测试", "test3");
            }
            result.add(map);
        }
        exportDataList.put("Example-Name属性查询验证导出", result);
        String fileName = URLEncodeUtil.encode("Example-Name属性查询验证-", StandardCharsets.UTF_8) + DateUtil.format(
                DateUtil.date(), "yyyyMMddHHmmss") + ".xlsx";
        rs.getMeta().get("base_excel").call("fileExport", exportDataList, fileName);
    }

    /**
     * 获取物料
     *
     * @return
     */
    @MethodService(description = "selectItem")
    public List<ExampleBaseOptions> selectItem() {
        RecordSet resEnterprise = getMeta().get(ModelConst.EXAMPLE_ITEM);
        List<Map<String, Object>> search = resEnterprise.search(null, Collections.singletonList("*"), 0, 0, "");
        List<ExampleBaseOptions> list = new ArrayList<>();
        search.stream().forEach(item -> {
            ExampleBaseOptions exampleBaseOptions = new ExampleBaseOptions();
            exampleBaseOptions.setId(item.get("id").toString());
            exampleBaseOptions.setCode(item.get("itemCode").toString());
            exampleBaseOptions.setName(item.get("itemName").toString());
            exampleBaseOptions.setlabel(item.get("itemName").toString());
            exampleBaseOptions.setValue(item.get("itemName").toString());
            list.add(exampleBaseOptions);
        });
        return list;
    }

    /**
     * 获取单位
     *
     * @return
     */
    @MethodService(description = "selectUnit")
    public List<ExampleBaseOptions> selectUnit() {
        RecordSet resEnterprise = getMeta().get(ModelConst.EXAMPLE_UNIT);
        List<Map<String, Object>> search = resEnterprise.search(null, Collections.singletonList("*"), 0, 0, "");
        List<ExampleBaseOptions> list = new ArrayList<>();
        search.stream().forEach(item -> {
            ExampleBaseOptions exampleBaseOptions = new ExampleBaseOptions();
            exampleBaseOptions.setId(item.get("id").toString());
            exampleBaseOptions.setCode(item.get("unitCode").toString());
            exampleBaseOptions.setName(item.get("unitName").toString());
            exampleBaseOptions.setlabel(item.get("unitName").toString());
            exampleBaseOptions.setValue(item.get("unitName").toString());
            list.add(exampleBaseOptions);
        });
        return list;
    }

    public String getCode() {
        return getStr("code");
    }

    public ExampleValidate setCode(String code) {
        this.set("code", code);
        return this;
    }

    public String getName() {
        return getStr("name");
    }

    public ExampleValidate setName(String name) {
        this.set("name", name);
        return this;
    }

    public Integer getQty() {
        return getInt("qty");
    }

    public ExampleValidate setQty(Integer qty) {
        this.set("qty", qty);
        return this;
    }

    public String getItemName() {
        return getStr("itemName");
    }

    public ExampleValidate setItemName(String itemName) {
        this.set("itemName", itemName);
        return this;
    }

    public String getUnitName() {
        return getStr("unitName");
    }

    public ExampleValidate setUnitName(String unitName) {
        this.set("unitName", unitName);
        return this;
    }

    public String getEnumerate() {
        return getStr("enumerate");
    }

    public ExampleValidate setEnumerate(String enumerate) {
        this.set("enumerate", enumerate);
        return this;
    }

    public Date getDate() {
        return getDate("date");
    }

    public ExampleValidate setDate(Date date) {
        this.set("date", date);
        return this;
    }
}
