package com.sie.iidp.example.studentscore.model;

import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.model.Bool;
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;

import java.util.Map;

/**
 * Example-人
 *
 * @author cxh
 * @date 2024年04月25日 15:15
 */
@Model(tableName = "example_personal", name = "student_personal", description = "Student人", displayName = "Student人",
        isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class StudentPersonal extends BaseModel<StudentPersonal> {

    @Property(columnName = "color", displayName = "颜色")
    private String color;

    @Property(columnName = "age", displayName = "年龄")
    private Integer age;

    @MethodService(description = "导出")
    public Map<String, Object> export(RecordSet rs) {
        return null;
    }

}
