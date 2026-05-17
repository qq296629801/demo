package com.sie.iidp.example.vocation.model;

import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.DataType;
import com.sie.snest.sdk.annotation.Dict;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.orm.Option;
import com.sie.snest.sdk.annotation.orm.Selection;
import com.sie.snest.sdk.annotation.validate.Validate;

import java.util.Date;
import java.util.Map;

@Model(tableName = "example_vocation", name = "example_vocation", description = "休假管理", displayName = "休假管理", isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleVocation extends BaseModel<ExampleVocation> {

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

}
