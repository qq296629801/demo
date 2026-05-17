package com.sie.iidp.example.text.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.sie.iidp.example.consts.ExportConst;
import com.sie.iidp.example.consts.ModelConst;
import com.sie.iidp.example.utils.dto.ExampleBaseOptions;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author syh
 * @Date 2024/4/8 10:16
 */
@Model(tableName = "example_text", name = "example_text", description = "Example文本编辑器", displayName = "Example文本编辑器",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleText extends BaseModel<ExampleText> {

    @Property(displayName = "文本")
    @Selection(method = "selectText")
    private String text1;

    @Property(displayName = "文本只读")
    private String text1ReadOnly;

    @Property(displayName = "文本Linq可见")
    private String textLinqSee;

    @Property(displayName = "文本Linq只读")
    private String textLinqReadOnly;

    @Property(displayName = "显示值")
    private String displayValue;

    @Validate.NotBlank(message = "文本不能为空")
    @Property(displayName = "文本")
    @Selection(method = "selectText")
    private String text2;
    @Property(displayName = "密码")
    private String password;

    @Property(displayName = "密码只读")
    private String passwordReadOnly;

    @Validate.NotBlank(message = "必填密码不能为空")
    @Property(displayName = "必填密码")
    private String mustPassword;

    @Property(displayName = "密码Linq可见")
    private String passwordLinqSee;

    @Property(displayName = "密码Linq只读")
    private String passwordLinqReadOnly;

    @Property(displayName = "大文本",length = 500)
    private String bigText;

    @Property(displayName = "大文本只读")
    private String bigTextReadOnly;

    @Property(displayName = "大文本Linq可见")
    private String bigTextLinqSee;

    @Validate.NotBlank(message = "必填大文本不能为空")
    @Property(displayName = "必填大文本",length = 500)
    private String mustBigText;

    @Property(displayName = "大文本Linq只读")
    private String bigTextLinqReadOnly;

    @Property(displayName = "只读")
    private Boolean readOnly;

    @Property(displayName = "可见")
    private Boolean visible;

    /**
     * 获取单位
     *
     * @return
     */
    @MethodService(description = "selectText")
    public List<ExampleBaseOptions> selectText() {
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

    /**
     * 导出文本编辑器信息
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
        String[] handleProperties = new String[]{"text1", "displayValue", "text2", "password", "mustPassword","bigText","mustBigText","create_user",
                "create_date", "update_user", "update_date"};
        List<ExampleText> exampleTextList = this.search(filter, Arrays.asList(handleProperties), limit, offset, order);
        // 数据判空
        if (CollUtil.isEmpty(exampleTextList)) {
            throw new Exception("没有符合数据");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        // 生成需要导出的信息
        for (ExampleText exampleText : exampleTextList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("文本", exampleText.getText1());
            map.put("显示值", exampleText.getDisplayValue());
            map.put("文本", exampleText.getText2());
            map.put("密码", exampleText.getPassword());
            map.put("必填密码", exampleText.getMustPassword());
            map.put("大文本", exampleText.getBigText());
            map.put("必填大文本", exampleText.getMustBigText());
            result.add(map);
        }
        exportDataList.put("文本编辑器导出", result);
        // 设置导出文件名
        String fileName = URLDecoder.decode("文本编辑器-", String.valueOf(StandardCharsets.UTF_8)) + DateUtil.format(
                DateUtil.date(), ExportConst.YYYY_MM_DD_HH_MM_SS) + ExportConst.XLSX;
        // 导出文件
        rs.getMeta().get(ExportConst.BASE_EXCEL).call(ExportConst.FILE_EXPORT, exportDataList, fileName);
    }

    public String getText1() {
        return getStr("text1");
    }

    public ExampleText setText1(String text1) {
        this.set("text1", text1);
        return this;
    }

    public String getDisplayValue() {
        return getStr("displayValue");
    }

    public ExampleText setDisplayValue(String displayValue) {
        this.set("displayValue", displayValue);
        return this;
    }

    public String getText2() {
        return getStr("text2");
    }

    public ExampleText setText2(String text2) {
        this.set("text2", text2);
        return this;
    }

    public String getPassword() {
        return getStr("password");
    }

    public ExampleText setPassword(String password) {
        this.set("password", password);
        return this;
    }

    public String getMustPassword() {
        return getStr("mustPassword");
    }

    public ExampleText setMustPassword(String mustPassword) {
        this.set("mustPassword", mustPassword);
        return this;
    }

    public String getBigText() {
        return getStr("bigText");
    }

    public ExampleText setBigText(String bigText) {
        this.set("bigText", bigText);
        return this;
    }

    public String getMustBigText() {
        return getStr("mustBigText");
    }

    public ExampleText setMustBigText(String mustBigText) {
        this.set("mustBigText", mustBigText);
        return this;
    }
}
