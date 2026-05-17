package com.sie.iidp.example.studentmgr.model;

import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.ModelRef;
import com.sie.snest.sdk.annotation.orm.Select;
import com.sie.snest.sdk.annotation.orm.Function;

import java.math.BigDecimal;

/**
 * @Author syh
 * @Date 2024/4/7 15:26
 */
@ModelRef(name = "example_student_ref" , function=@Function(select = @Select(sum = "studentLevel as sumStudentLevel",
        count = "1 as stuCode"),groupBy = "name"),
        dst = "sie-iidp-demo-example.example_class")
public class ExampleStudentRef extends BaseModel<ExampleStudentRef> {
    public final static String MODEL_NAME="example_student_ref";
    private BigDecimal sumStudentLevel;
    private String name;

    public BigDecimal getSumStudentLevel() {
        return getBigDecimal("sumStudentLevel");
    }

    public ExampleStudentRef setSumStudentLevel(BigDecimal sumStudentLevel) {
        this.set("sumStudentLevel", sumStudentLevel);
        return this;
    }

    public String getName() {
        return getStr("name");
    }

    public ExampleStudentRef setName(String name) {
        this.set("name", name);
        return this;
    }
}
