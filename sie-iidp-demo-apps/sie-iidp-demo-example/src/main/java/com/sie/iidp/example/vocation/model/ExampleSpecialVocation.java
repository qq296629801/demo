package com.sie.iidp.example.vocation.model;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.sie.iidp.example.unitmgr.model.ExampleUnit;
import com.sie.snest.engine.constant.AttributesConstant;
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.Dict;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;
import com.sie.snest.sdk.common.ExportData;
import com.sie.snest.sdk.common.ExportInfo;
import lombok.Data;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.util.*;

@Model(tableName = "example_special_vocation", name = "example_special_vocation", description = "特殊休假管理", displayName = "特殊休假管理", isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleSpecialVocation extends BaseModel<ExampleSpecialVocation> {

    @Property(displayName = "休假标题")
    private String title;

    @Property(displayName = "备注")
    private String description;

    @Property(displayName = "联系方式")
    private String contact;

    @Validate.NotBlank(message = "开始时间不能为空")
    @Property(displayName = "开始时间", dataType = DataType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Validate.NotBlank(message = "结束时间不能为空")
    @Property(displayName = "结束时间", dataType = DataType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Property(displayName = "休假类型")
    @Selection(values = {
            @Option(label = "带薪年假", value = "1"),
            @Option(label = "工龄假", value = "2"),
            @Option(label = "产假", value = "3"),
            @Option(label = "陪产假", value = "4"),
            @Option(label = "病假", value = "5")
    })
    private String vocationType;

    @Property(displayName = "审批状态")
    @Dict(typeCode = "vocationStatus", multiple = false)
    private String status;

    @Property(displayName = "工资系数", computeMethod = "computeSalaryRatio")
    private double salaryRatio;

    public String getTitle() {
        return getStr("title");
    }

    public ExampleSpecialVocation setTitle(String title) {
        this.set("title", title);
        return this;
    }

    public String getDescription() {
        return getStr("description");
    }

    public ExampleSpecialVocation setDescription(String description) {
        this.set("description", description);
        return this;
    }

    public String getContact() {
        return getStr("contact");
    }

    public ExampleSpecialVocation setContact(String contact) {
        this.set("contact", contact);
        return this;
    }

    public String getStartTime() {
        return getStr("startTime");
    }

    public ExampleSpecialVocation setStartTime(Date startTime) {
        this.set("startTime", startTime);
        return this;
    }

    public String getEndTime() {
        return getStr("endTime");
    }

    public ExampleSpecialVocation setEndTime(Date endTime) {
        this.set("endTime", endTime);
        return this;
    }

    public String getVocationType() {
        return getStr("vocationType");
    }

    public ExampleSpecialVocation setVocationType(String vocationType) {
        this.set("vocationType", vocationType);
        return this;
    }

    public double getSalaryRatio() {
        return getDouble("salaryRatio");
    }

    public ExampleSpecialVocation setSalaryRatio(double salaryRatio) {
        this.set("salaryRatio", salaryRatio);
        return this;
    }

    public double getStatus() {
        return getDouble("status");
    }

    public ExampleSpecialVocation setStatus(String status) {
        this.set("status", status);
        return this;
    }

    @MethodService(description = "计算工资系数")
    public double computeSalaryRatio(Map<String, Object> valMap) {
        double salaryRatio = 0;
        if (valMap!=null && valMap.get("vocationType")!=null) {
            String expiryDateStr = String.valueOf(valMap.get("vocationType"));
            switch (expiryDateStr) {
                case "5":
                    salaryRatio = 0.5;
                    break;
                default:
                    salaryRatio = 1;
                    break;
            }
        }
        return salaryRatio;
    }

    @MethodService(description = "excel导出")
    public ExportInfo excelExport(RecordSet rs, Filter filter, List<String> properties) {
        ExportInfo exportInfo = new ExportInfo();

        ExportData exportData = new ExportData();
        List<Map<String, Object>> result = new ArrayList<>();

        this.getMeta().getArguments().put(AttributesConstant.useDisplayForModel, false);
        List<ExampleSpecialVocation> vocations = this.search(filter, properties, 1000000, 0, null);
        for (ExampleSpecialVocation vocation : vocations) {
            LinkedHashMap<String, Object> unitEntity = new LinkedHashMap<>();
            unitEntity.put("标题 Title", vocation.getTitle());
            unitEntity.put("请假类型 Vocation Type", vocation.getVocationType());
            unitEntity.put("工资系数 Salary Ratio", vocation.getSalaryRatio());
            unitEntity.put("描述 Description", vocation.getDescription());
            unitEntity.put("起始日期 Start", vocation.getStartTime());
            unitEntity.put("结束日期 End", vocation.getEndTime());
            String startTime = String.valueOf(vocation.getStartTime());
            String endTime = String.valueOf(vocation.getEndTime());
            DateTime startTimeDate = DateUtil.parse(startTime, "yyyy-MM-dd HH:mm:ss");
            DateTime endTimeDate = DateUtil.parse(endTime, "yyyy-MM-dd HH:mm:ss");

            unitEntity.put("持续时间（小时） Duration(H)", DateUtil.between(startTimeDate, endTimeDate, DateUnit.HOUR));
            unitEntity.put("审批状态 Status", vocation.getStatus());

            for(int i=0;i<50;i++){
                unitEntity.put("字段"+(i+1), "一二三四五六七八九十");
            }

            result.add(unitEntity);
        }

        exportInfo.setBussName("特殊假期管理");
        exportData.putData(exportInfo.getBussName(), result);
        exportInfo.setExportData(exportData);

        return exportInfo;
    }

}
